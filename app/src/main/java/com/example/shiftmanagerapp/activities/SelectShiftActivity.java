package com.example.shiftmanagerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shiftmanagerapp.R;
import com.example.shiftmanagerapp.adapters.ShiftAdapter;
import com.example.shiftmanagerapp.models.Shift;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class SelectShiftActivity extends AppCompatActivity {

    private RecyclerView shiftsRecyclerView;
    private ShiftAdapter shiftAdapter;
    private List<Shift> shiftList;
    private FirebaseDatabase database;
    private DatabaseReference shiftsRef, usersRef;
    private FirebaseAuth auth;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_shift);

        database = FirebaseDatabase.getInstance();
        shiftsRef = database.getReference("shifts");
        usersRef = database.getReference("users");
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "No user logged in. Please log in.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        shiftsRecyclerView = findViewById(R.id.shiftsRecyclerView);
        btnBack = findViewById(R.id.btnBack); // Initialize btnBack
        shiftsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        shiftList = new ArrayList<>();
        shiftAdapter = new ShiftAdapter(shiftList, this::applyForShift);
        shiftsRecyclerView.setAdapter(shiftAdapter);

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(SelectShiftActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        loadAvailableShifts();
    }

    private void loadAvailableShifts() {
        shiftsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                shiftList.clear();
                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String date = dateSnapshot.getKey();
                    for (DataSnapshot shiftSnapshot : dateSnapshot.getChildren()) {
                        Shift shift = shiftSnapshot.getValue(Shift.class);
                        shift.setDate(date);
                        shift.setShift_type(shiftSnapshot.getKey());
                        if (shift.getStatus() == 0) shiftList.add(shift);
                    }
                }
                shiftAdapter.notifyDataSetChanged();
                if (shiftList.isEmpty()) Toast.makeText(SelectShiftActivity.this, "No available shifts", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SelectShiftActivity.this, "Error loading shifts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyForShift(Shift shift) {
        String userId = auth.getCurrentUser().getUid();
        if (userId == null || shift == null) {
            Toast.makeText(this, "Please log in or select a valid shift", Toast.LENGTH_LONG).show();
            return;
        }

        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String workerRole = snapshot.child("role").getValue(String.class);
                if (workerRole == null) {
                    Log.e("Firebase", "Role not found for userId: " + userId);
                    Toast.makeText(SelectShiftActivity.this, "Worker role not found. Please contact admin.", Toast.LENGTH_SHORT).show();
                    return;
                }

                int currentCount = countEmployeesByRole(shift, workerRole);
                int maxForRole = getMaxForRole(shift, workerRole);

                if (currentCount < maxForRole) {
                    DatabaseReference shiftRef = shiftsRef.child(shift.getDate()).child(shift.getShift_type());
                    shiftRef.runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                            List<String> approvedEmployees = mutableData.child("approved_employees").getValue() != null ?
                                    mutableData.child("approved_employees").getValue(new GenericTypeIndicator<List<String>>() {}) :
                                    new ArrayList<>();
                            if (!approvedEmployees.contains(userId)) {
                                approvedEmployees.add(userId);
                                mutableData.child("approved_employees").setValue(approvedEmployees);
                            }

                            int currentRoleCount = getCurrentRoleCount(mutableData, workerRole);
                            if (currentRoleCount > 0) {
                                setRoleCount(mutableData, workerRole, currentRoleCount - 1);
                            }

                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                if (error != null) {
                                    Toast.makeText(SelectShiftActivity.this, "Failed to apply: " + error.getMessage(), Toast.LENGTH_LONG).show();
                                } else if (committed) {
                                    shift.setAssigned_employees(snapshot.child("approved_employees").getValue(new GenericTypeIndicator<List<String>>() {}));
                                    updateShiftInList(shift);
                                    Toast.makeText(SelectShiftActivity.this, "Applied for " + shift.getShift_type() + " on " + shift.getDate(), Toast.LENGTH_SHORT).show();

                                    //TODO CALENDAR SEND FOR MAIL
                                    Intent broadcastIntent = new Intent("com.example.shiftmanagerapp.REFRESH_CALENDAR");
                                    sendBroadcast(broadcastIntent);

                                    Intent homeIntent = new Intent(SelectShiftActivity.this, HomeActivity.class);
                                    startActivity(homeIntent);
                                    finish();
                                }
                            });
                        }
                    });
                } else {
                    Toast.makeText(SelectShiftActivity.this, "No space for " + workerRole + " in this shift", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SelectShiftActivity.this, "Error fetching worker data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateShiftInList(Shift shift) {
        int index = shiftList.indexOf(shift);
        if (index != -1) {
            shiftList.set(index, shift);
            shiftAdapter.notifyItemChanged(index);
        }
    }

    private int countEmployeesByRole(Shift shift, String role) {
        List<String> approvedEmployees = shift.getAssigned_employees();
        if (approvedEmployees == null) return 0;
        int count = 0;
        for (String empId : approvedEmployees) {
            DataSnapshot snapshot = usersRef.child(empId).get().getResult();
            if (snapshot != null && snapshot.exists() && role.equals(snapshot.child("role").getValue(String.class))) {
                count++;
            }
        }
        return count;
    }

    private int getMaxForRole(Shift shift, String role) {
        switch (role) {
            case "Waiter": return shift.getWaiters();
            case "Bartender": return shift.getBarmen();
            case "Shift Manager": return shift.getAdmins();
            case "Cook": return shift.getCooks();
            default: return 0;
        }
    }

    private int getCurrentRoleCount(MutableData mutableData, String role) {
        switch (role) {
            case "Waiter": return mutableData.child("waiters").getValue(Integer.class) != null ? mutableData.child("waiters").getValue(Integer.class) : 0;
            case "Bartender": return mutableData.child("barmen").getValue(Integer.class) != null ? mutableData.child("barmen").getValue(Integer.class) : 0;
            case "Shift Manager": return mutableData.child("admins").getValue(Integer.class) != null ? mutableData.child("admins").getValue(Integer.class) : 0;
            case "Cook": return mutableData.child("cooks").getValue(Integer.class) != null ? mutableData.child("cooks").getValue(Integer.class) : 0;
            default: return 0;
        }
    }

    private void setRoleCount(MutableData mutableData, String role, int value) {
        switch (role) {
            case "Waiter": mutableData.child("waiters").setValue(value); break;
            case "Bartender": mutableData.child("barmen").setValue(value); break;
            case "Shift Manager": mutableData.child("admins").setValue(value); break;
            case "Cook": mutableData.child("cooks").setValue(value); break;
        }
    }
}