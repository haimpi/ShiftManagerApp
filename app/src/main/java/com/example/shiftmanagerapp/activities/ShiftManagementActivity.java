package com.example.shiftmanagerapp.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shiftmanagerapp.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ShiftManagementActivity extends AppCompatActivity {

    private Button btnSelectStartDate, btnSelectEndDate, btnSave, btnBack;
    private Button btnSelectWaiterCount, btnSelectBarmenCount, btnSelectAdminCount, btnSelectShefCount;
    private TextView txtStartDate, txtEndDate;
    private RadioGroup radioGroupShift;
    private String selectedShift = "Not Selected";
    private int waiterCount = 0, barmenCount = 0, adminCount = 0, chefCount = 0;
    private Calendar startDateCalendar, endDateCalendar;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // Changed to hyphen
    private DatabaseReference shiftsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift_management);

        shiftsRef = FirebaseDatabase.getInstance().getReference("shifts");

        btnSelectStartDate = findViewById(R.id.btnSelectStartDate);
        btnSelectEndDate = findViewById(R.id.btnSelectEndDate);
        txtStartDate = findViewById(R.id.txtStartDate);
        txtEndDate = findViewById(R.id.txtEndDate);
        radioGroupShift = findViewById(R.id.radioGroupShift);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        btnSelectWaiterCount = findViewById(R.id.btnSelectWaiterCount);
        btnSelectBarmenCount = findViewById(R.id.btnSelectBarmenCount);
        btnSelectAdminCount = findViewById(R.id.btnSelectAdminCount);
        btnSelectShefCount = findViewById(R.id.btnSelectShefCount);

        startDateCalendar = Calendar.getInstance();
        endDateCalendar = Calendar.getInstance();

        btnSelectStartDate.setOnClickListener(v -> showDatePickerDialog(true));
        btnSelectEndDate.setOnClickListener(v -> showDatePickerDialog(false));

        radioGroupShift.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                RadioButton selectedButton = findViewById(checkedId);
                selectedShift = selectedButton.getText().toString();
            }
        });

        btnSelectWaiterCount.setOnClickListener(v -> showNumberPickerDialog("Waiter", btnSelectWaiterCount));
        btnSelectBarmenCount.setOnClickListener(v -> showNumberPickerDialog("Bartender", btnSelectBarmenCount));
        btnSelectAdminCount.setOnClickListener(v -> showNumberPickerDialog("Shift Manager", btnSelectAdminCount));
        btnSelectShefCount.setOnClickListener(v -> showNumberPickerDialog("Cook", btnSelectShefCount));

        btnSave.setOnClickListener(v -> saveData());
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ShiftManagementActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void showNumberPickerDialog(String role, Button button) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select quantity for " + role);
        final String[] numbers = {"0", "1", "2", "3", "4", "5", "6", "7"};

        builder.setItems(numbers, (dialog, which) -> {
            int selectedNumber = Integer.parseInt(numbers[which]);
            button.setText(role + ": " + selectedNumber);
            switch (role) {
                case "Waiter": waiterCount = selectedNumber; break;
                case "Bartender": barmenCount = selectedNumber; break;
                case "Shift Manager": adminCount = selectedNumber; break;
                case "Cook": chefCount = selectedNumber; break;
            }
        });
        builder.show();
    }

    private void showDatePickerDialog(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    if (isStartDate) {
                        startDateCalendar.set(year, month, dayOfMonth);
                        txtStartDate.setText(dateFormat.format(selectedDate.getTime()));
                    } else {
                        if (selectedDate.before(startDateCalendar)) {
                            txtEndDate.setText("Invalid date");
                            Toast.makeText(this, "End date cannot be before start date", Toast.LENGTH_SHORT).show();
                        } else {
                            endDateCalendar.set(year, month, dayOfMonth);
                            txtEndDate.setText(dateFormat.format(selectedDate.getTime()));
                        }
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        if (isStartDate) {
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        } else {
            datePickerDialog.getDatePicker().setMinDate(startDateCalendar.getTimeInMillis());
            datePickerDialog.getDatePicker().setMaxDate(startDateCalendar.getTimeInMillis() + (14 * 24 * 60 * 60 * 1000));
        }

        datePickerDialog.show();
    }

    private void saveData() {
        if (selectedShift.equals("Not Selected")) {
            Toast.makeText(this, "Please select a shift", Toast.LENGTH_SHORT).show();
            return;
        }
        if (txtStartDate.getText().toString().equals("Not Selected")) {
            Toast.makeText(this, "Please select start date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (txtEndDate.getText().toString().equals("Not Selected") || txtEndDate.getText().toString().equals("Invalid date")) {
            Toast.makeText(this, "Please select valid end date", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> selectedDates = new ArrayList<>();
        Calendar tempDate = (Calendar) startDateCalendar.clone();
        while (!tempDate.after(endDateCalendar)) {
            selectedDates.add(dateFormat.format(tempDate.getTime()));
            tempDate.add(Calendar.DAY_OF_MONTH, 1);
        }

        for (String date : selectedDates) {
            if (selectedShift.equals("Both Shifts")) {
                Map<String, Object> morningShiftData = new HashMap<>();
                morningShiftData.put("date", date);
                morningShiftData.put("shift_type", "Morning Shift");
                morningShiftData.put("waiters", waiterCount);
                morningShiftData.put("barmen", barmenCount);
                morningShiftData.put("admins", adminCount);
                morningShiftData.put("cooks", chefCount);
                morningShiftData.put("status", 0);
                morningShiftData.put("assigned_employees", new ArrayList<String>());

                Map<String, Object> eveningShiftData = new HashMap<>();
                eveningShiftData.put("date", date);
                eveningShiftData.put("shift_type", "Evening Shift");
                eveningShiftData.put("waiters", waiterCount);
                eveningShiftData.put("barmen", barmenCount);
                eveningShiftData.put("admins", adminCount);
                eveningShiftData.put("cooks", chefCount);
                eveningShiftData.put("status", 0);
                eveningShiftData.put("assigned_employees", new ArrayList<String>());

                shiftsRef.child(date).child("Morning Shift").setValue(morningShiftData)
                        .addOnSuccessListener(aVoid -> Log.d("Firebase", "Morning shift saved for " + date))
                        .addOnFailureListener(e -> Log.e("Firebase", "Failed to save morning shift: " + e.getMessage()));

                shiftsRef.child(date).child("Evening Shift").setValue(eveningShiftData)
                        .addOnSuccessListener(aVoid -> Log.d("Firebase", "Evening shift saved for " + date))
                        .addOnFailureListener(e -> Log.e("Firebase", "Failed to save evening shift: " + e.getMessage()));
            } else {
                Map<String, Object> shiftData = new HashMap<>();
                shiftData.put("date", date);
                shiftData.put("shift_type", selectedShift);
                shiftData.put("waiters", waiterCount);
                shiftData.put("barmen", barmenCount);
                shiftData.put("admins", adminCount);
                shiftData.put("cooks", chefCount);
                shiftData.put("status", 0);
                shiftData.put("assigned_employees", new ArrayList<String>());

                shiftsRef.child(date).child(selectedShift).setValue(shiftData)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Firebase", "Shift saved for " + date);
                            Toast.makeText(ShiftManagementActivity.this, "Shift saved successfully", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firebase", "Failed to save shift: " + e.getMessage());
                            Toast.makeText(ShiftManagementActivity.this, "Failed to save shift: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        }

        resetFields();
    }

    private void resetFields() {
        txtStartDate.setText("Not Selected");
        txtEndDate.setText("Not Selected");
        selectedShift = "Not Selected";
        waiterCount = 0;
        barmenCount = 0;
        adminCount = 0;
        chefCount = 0;

        radioGroupShift.clearCheck();
        btnSelectWaiterCount.setText("Select Quantity");
        btnSelectBarmenCount.setText("Select Quantity");
        btnSelectAdminCount.setText("Select Quantity");
        btnSelectShefCount.setText("Select Quantity");
    }
}