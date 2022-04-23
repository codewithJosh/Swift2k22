package com.codewithjosh.Swift2k22;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    EditText _username, _password;
    Button _onLogin;
    TextView _onSignUp;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    ProgressDialog _pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        _username = findViewById(R.id.username);
        _password = findViewById(R.id.password);
        _onLogin = findViewById(R.id.on_login);
        _onSignUp = findViewById(R.id.on_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        _onSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });

        _onLogin.setOnClickListener(v -> {
            _pd = new ProgressDialog(LoginActivity.this);
            _pd.setMessage("Logging in");
            _pd.show();

            String str_username = _username.getText().toString().toLowerCase();
            String str_password = _password.getText().toString();

            if (str_username.isEmpty() || str_password.isEmpty()) {
                _pd.dismiss();
                Toast.makeText(LoginActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
            }
            else if (str_password.length() < 6) {
                _pd.dismiss();
                Toast.makeText(LoginActivity.this, "Password Must be at least 6 characters", Toast.LENGTH_SHORT).show();
            }
            else _onLogin(str_username, str_password);

        });

    }

    private void _onLogin(String _username, String _password) {

        firebaseFirestore
                .collection("Users")
                .whereEqualTo("user_name", _username)
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        if (task.getResult().isEmpty()) {

                            _pd.dismiss();
                            Toast.makeText(LoginActivity.this, "User Doesn't Exist!", Toast.LENGTH_SHORT).show();
                        }
                        else {

                            for (QueryDocumentSnapshot snapshot : task.getResult()) {

                                final String _email = snapshot.get("user_email").toString();

                                firebaseAuth.signInWithEmailAndPassword(_email, _password)
                                        .addOnCompleteListener(LoginActivity.this, _task -> {

                                            if (_task.isSuccessful()) {

                                                _pd.dismiss();
                                                Toast.makeText(LoginActivity.this, "Welcome, You've Successfully Login!", Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                                finish();
                                            }

                                        }).addOnFailureListener(e -> {

                                    if (e.toString().contains("The password is invalid or the user does not have a password")) {
                                        _pd.dismiss();
                                        Toast.makeText(LoginActivity.this, "Incorrect Password!", Toast.LENGTH_SHORT).show();
                                    }
                                    else if (e.toString().contains("A network error (such as timeout, interrupted connection or unreachable host) has occurred")) {
                                        _pd.dismiss();
                                        Toast.makeText(LoginActivity.this, "Internet Connection Lost!", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        _pd.dismiss();
                                        Toast.makeText(LoginActivity.this, "Please Contact Your Service Provider", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                    }
                });

    }

}