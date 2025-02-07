package com.example.shiftmanagerapp;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // אתחול Firebase
        FirebaseApp.initializeApp(this);

        // בדיקה אם Firebase מחובר
        if (FirebaseApp.getApps(this).size() > 0) {
            Log.d("Firebase", "🔥 Firebase מחובר בהצלחה!");
        } else {
            Log.e("Firebase", "❌ שגיאה בחיבור ל-Firebase!");
        }
    }
}
