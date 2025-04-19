package com.example.quickvendadmin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.TRANSPARENT); // Or any color you prefer
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        // Set the splash screen layout
        setContentView(R.layout.activity_main);

        // Show splash screen for 3 seconds before proceeding
        new Handler().postDelayed(() -> {
            // Get current user
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null) {
                // User is logged in → go to HomeActivity
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
            } else {
                // Not logged in → go to LoginActivity
                startActivity(new Intent(MainActivity.this, VendorLoginActivity.class));
            }

            finish(); // Prevent going back to MainActivity
        }, 2000); // 3000 ms = 3 sec
    }
}
