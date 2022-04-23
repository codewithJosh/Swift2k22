package com.codewithjosh.Swift2k22;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText _username, _password;
    Button _onLogin;
    TextView _onSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        _username = findViewById(R.id.username);
        _password = findViewById(R.id.password);
        _onLogin = findViewById(R.id.on_login);
        _onSignUp = findViewById(R.id.on_sign_up);

        _onSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });

    }

}