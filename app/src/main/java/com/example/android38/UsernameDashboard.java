package com.example.android38;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class UsernameDashboard extends AppCompatActivity {

    private EditText usernameEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);

        usernameEditText = findViewById(R.id.usernameEditText);
        loginButton = findViewById(R.id.loginButton);
    }

    public void onLoginButtonClick(View view) {
        String username = usernameEditText.getText().toString().trim();

        if (!username.isEmpty()) {
            Intent intent;

            // For simplicity, let's assume "admin" directs to the admin dashboard
            if (username.equalsIgnoreCase("admin")) {
                intent = new Intent(this, AdminDashboardActivity.class);
            } else {
                intent = new Intent(this, UserDashboardActivity.class);
            }

            startActivity(intent);
        }
    }
}
