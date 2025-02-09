package com.example.shiftmanagerapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddWorkerActivity extends AppCompatActivity {

    private EditText editWorkerName, editWorkerRole, editWorkerShift;
    private Button btnSaveWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_worker);

        editWorkerName = findViewById(R.id.editWorkerName);
        editWorkerRole = findViewById(R.id.editWorkerRole);
        editWorkerShift = findViewById(R.id.editWorkerShift);
        btnSaveWorker = findViewById(R.id.btnSaveWorker);

        btnSaveWorker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String workerName = editWorkerName.getText().toString();
                String workerRole = editWorkerRole.getText().toString();
                String workerShift = editWorkerShift.getText().toString();

                if (workerName.isEmpty() || workerRole.isEmpty() || workerShift.isEmpty()) {
                    Toast.makeText(AddWorkerActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    //TODO SAVE ON FIREBASE
                    Toast.makeText(AddWorkerActivity.this, "Worker saved: " + workerName, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
