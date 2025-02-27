package com.example.shiftmanagerapp.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.example.shiftmanagerapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    private CalendarView calendarView;
    private TextView tvWorkerName;
    private ImageView btnSubmitShift;
    private ImageView btnLogout;
    private ImageView btnAdminPanel;
    private LinearLayout adminPanelLayout;
    private List<Date> shiftDates = new ArrayList<>();
    private Map<String, List<String>> userShiftsByDate = new HashMap<>(); // Map to store shifts by date
    private List<EventDay> events = new ArrayList<>();
    private DatabaseReference shiftsRef;
    private static final String TAG = "HomeActivity";

    private BroadcastReceiver calendarRefreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("com.example.shiftmanagerapp.REFRESH_CALENDAR")) {
                loadShiftEvents();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        calendarView = findViewById(R.id.calendarView);
        tvWorkerName = findViewById(R.id.tvWorkerName);
        btnSubmitShift = findViewById(R.id.btn_submit_shift);
        btnLogout = findViewById(R.id.btn_logout);
        btnAdminPanel = findViewById(R.id.btn_admin_panel);
        adminPanelLayout = findViewById(R.id.adminPanelLayout);
        shiftsRef = FirebaseDatabase.getInstance().getReference("shifts");

        adminPanelLayout.setVisibility(View.GONE);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            fetchUserRole(userId);
            String email = currentUser.getEmail();
            tvWorkerName.setText(email != null ? email : "Unnamed Worker");
        } else {
            Log.e(TAG, "No user is currently signed in.");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        calendarView.setMinimumDate(today);

        Calendar maxDate = Calendar.getInstance();
        maxDate.setTime(today.getTime());
        maxDate.add(Calendar.DAY_OF_YEAR, 14);
        calendarView.setMaximumDate(maxDate);

        btnSubmitShift.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            Intent intent = new Intent(this, SelectShiftActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            logout();
        });

        btnAdminPanel.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            showAdminDialog();
        });

        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Calendar clickedDay = eventDay.getCalendar();
                clickedDay.set(Calendar.HOUR_OF_DAY, 0);
                clickedDay.set(Calendar.MINUTE, 0);
                clickedDay.set(Calendar.SECOND, 0);
                clickedDay.set(Calendar.MILLISECOND, 0);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String clickedDateStr = sdf.format(clickedDay.getTime());

                // Display shifts for the clicked date via Toast
                List<String> shifts = userShiftsByDate.get(clickedDateStr);
                StringBuilder message = new StringBuilder("Shifts on " + clickedDateStr + ":\n");
                if (shifts != null && !shifts.isEmpty()) {
                    for (String shift : shifts) {
                        if ("morning shift".equalsIgnoreCase(shift) || "evening shift".equalsIgnoreCase(shift)) {
                            message.append(shift).append("\n");
                        }
                    }
                    Toast.makeText(HomeActivity.this, message.toString(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(HomeActivity.this, "No morning or evening shifts on " + clickedDateStr, Toast.LENGTH_LONG).show();
                }
            }
        });

        loadShiftEvents();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(calendarRefreshReceiver, new IntentFilter("com.example.shiftmanagerapp.REFRESH_CALENDAR"), RECEIVER_NOT_EXPORTED);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(calendarRefreshReceiver);
    }

    private void fetchUserRole(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String role = dataSnapshot.child("role").getValue(String.class);
                    boolean isAdmin = "admin".equalsIgnoreCase(role);
                    Log.d(TAG, "Fetched userId: " + userId + ", role: " + role + ", isAdmin: " + isAdmin);

                    adminPanelLayout.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                } else {
                    Log.e(TAG, "User data not found in Firebase for userId: " + userId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read user role: " + databaseError.getMessage());
            }
        });
    }

    private void showAdminDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Admin Options");

        String[] adminOptions = {"Manage Shifts", "Add New Worker"};

        builder.setItems(adminOptions, (dialog, which) -> {
            Intent intent;
            switch (which) {
                case 0: // Shift Overview
                    intent = new Intent(this, ShiftManagementActivity.class);
                    startActivity(intent);
                    break;
                case 1: // Manage Shifts
                    intent = new Intent(this, AddWorkerActivity.class);
                    startActivity(intent);
                    break;
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadShiftEvents() {
        events.clear();
        shiftDates.clear();
        userShiftsByDate.clear();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            shiftsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                        String dateStr = dateSnapshot.getKey();
                        List<String> shiftsForDate = new ArrayList<>();

                        for (DataSnapshot shiftSnapshot : dateSnapshot.getChildren()) {
                            DataSnapshot approvedSnapshot = shiftSnapshot.child("approved_employees");
                            for (DataSnapshot empId : approvedSnapshot.getChildren()) {
                                String id = empId.getValue(String.class);
                                if (id != null && id.equals(userId)) {
                                    try {
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                        Date shiftDate = sdf.parse(dateStr);
                                        if (shiftDate != null) {
                                            Calendar calendar = Calendar.getInstance();
                                            calendar.setTime(shiftDate);
                                            calendar.set(Calendar.HOUR_OF_DAY, 0);
                                            calendar.set(Calendar.MINUTE, 0);
                                            calendar.set(Calendar.SECOND, 0);
                                            calendar.set(Calendar.MILLISECOND, 0);
                                            shiftDates.add(shiftDate);
                                            String shiftType = shiftSnapshot.child("shift_type").getValue(String.class);
                                            if (shiftType != null) {
                                                shiftsForDate.add(shiftType);
                                            }
                                            events.add(new EventDay(calendar, R.drawable.ic_shift_dot));
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error parsing date: " + e.getMessage());
                                    }
                                }
                            }
                        }
                        if (!shiftsForDate.isEmpty()) {
                            userShiftsByDate.put(dateStr, shiftsForDate);
                        }
                    }
                    calendarView.setEvents(events);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to load shift events: " + error.getMessage());
                }
            });
        }
    }
}