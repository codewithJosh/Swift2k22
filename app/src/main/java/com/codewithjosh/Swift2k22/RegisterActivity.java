package com.codewithjosh.Swift2k22;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    EditText _username, _email, _password, _rePassword;
    Button _onRegister;
    TextView _onSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        _username = findViewById(R.id.username);
        _email = findViewById(R.id.email);
        _password = findViewById(R.id.password);
        _rePassword = findViewById(R.id.rePassword);
        _onRegister = findViewById(R.id.on_register);
        _onSignIn = findViewById(R.id.on_sign_in);

        _onSignIn.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

    }

}