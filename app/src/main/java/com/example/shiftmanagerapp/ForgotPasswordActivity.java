package com.example.shiftmanagerapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText emailReset;
    private Button resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        auth = FirebaseAuth.getInstance();
        emailReset = findViewById(R.id.emailReset);
        resetButton = findViewById(R.id.resetButton);

        resetButton.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = emailReset.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "נא להזין כתובת אימייל", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "קישור לאיפוס סיסמה נשלח למייל", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "שגיאה בשליחת קישור: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
