package com.example.shiftmanagerapp.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.shiftmanagerapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddWorkerActivity extends AppCompatActivity {

    private EditText editWorkerName, editWorkerEmail, editWorkerId, editWorkerPassword;
    private TextView textWorkerDob, btnSaveWorker;
    private Spinner spinnerWorkerRole;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private String selectedDob = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_worker);

        editWorkerName = findViewById(R.id.editWorkerName);
        editWorkerEmail = findViewById(R.id.editWorkerEmail);
        editWorkerId = findViewById(R.id.editWorkerId);
        editWorkerPassword = findViewById(R.id.editWorkerPassword);
        textWorkerDob = findViewById(R.id.textWorkerDob);
        spinnerWorkerRole = findViewById(R.id.spinnerWorkerRole);
        btnSaveWorker = findViewById(R.id.btnSaveWorker);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        String[] roles = {"Select Role", "Shift Manager", "Bartender", "Cooker", "Waiter"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWorkerRole.setAdapter(roleAdapter);
        spinnerWorkerRole.setSelection(0);

        textWorkerDob.setOnClickListener(v -> showDatePicker());
        btnSaveWorker.setOnClickListener(v -> registerWorker());
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    selectedDob = String.format("%d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                    textWorkerDob.setText(String.format("%02d/%02d/%d", dayOfMonth, month1 + 1, year1));
                }, year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void registerWorker() {
        String workerName = editWorkerName.getText().toString().trim();
        String workerEmail = editWorkerEmail.getText().toString().trim();
        String workerId = editWorkerId.getText().toString().trim();
        String workerPassword = editWorkerPassword.getText().toString().trim();
        String workerDob = selectedDob;
        String workerRole = spinnerWorkerRole.getSelectedItem().toString();

        if (workerName.isEmpty() || workerEmail.isEmpty() || workerId.isEmpty() || workerPassword.isEmpty() || workerDob.isEmpty() || workerRole.equals("Select Role")) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(workerEmail).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (workerPassword.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidID(workerId)) {
            Toast.makeText(this, "Invalid ID number", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveWorker.setEnabled(false);

        // check worker id exist
        usersRef.child(workerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(AddWorkerActivity.this, "Worker ID already exists", Toast.LENGTH_SHORT).show();
                    btnSaveWorker.setEnabled(true);
                } else {
                    // Create user in Firebase Authentication
                    auth.createUserWithEmailAndPassword(workerEmail, workerPassword)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = auth.getCurrentUser();
                                    if (user != null) {
                                        saveWorkerToDatabase(workerId, workerEmail, workerName, workerDob, workerRole);
                                    }
                                } else {
                                    Log.e("Firebase", "Error creating user: " + task.getException().getMessage());
                                    Toast.makeText(AddWorkerActivity.this, "Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    btnSaveWorker.setEnabled(true);
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Database error: " + databaseError.getMessage());
                btnSaveWorker.setEnabled(true);
            }
        });
    }

    private void saveWorkerToDatabase(String id, String email, String name, String dob, String role) {
        Map<String, Object> workerData = new HashMap<>();
        workerData.put("name", name);
        workerData.put("email", email);
        workerData.put("dob", dob);
        workerData.put("role", role);
        workerData.put("uid", auth.getCurrentUser().getUid());
        workerData.put("workerId", id);

        usersRef.child(auth.getCurrentUser().getUid()).setValue(workerData) // uid works as a key
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Worker added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Error saving worker: " + e.getMessage());
                    Toast.makeText(this, "Failed to save worker data", Toast.LENGTH_SHORT).show();
                    btnSaveWorker.setEnabled(true);
                });
    }

    private boolean isValidID(String id) {
        return id.matches("\\d{9}"); // 9 digits or less
    }
}