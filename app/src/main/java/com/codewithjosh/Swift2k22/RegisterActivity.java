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

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText _username, _email, _password, _rePassword;
    Button _onRegister;
    TextView _onSignIn;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    ProgressDialog _pd;

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

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        _onSignIn.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        _onRegister.setOnClickListener(v -> {
            _pd = new ProgressDialog(RegisterActivity.this);
            _pd.setMessage("Signing up");
            _pd.show();

            String str_username = _username.getText().toString().toLowerCase();
            String str_email = _email.getText().toString().toLowerCase();
            String str_password = _password.getText().toString();
            String str_rePassword = _rePassword.getText().toString();

            if (str_username.isEmpty() || str_email.isEmpty()
                    || str_password.isEmpty() || str_rePassword.isEmpty()) {
                _pd.dismiss();
                Toast.makeText(RegisterActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
            }
            else if (!(str_email.contains("@") && str_email.endsWith(".com"))) {
                _pd.dismiss();
                Toast.makeText(RegisterActivity.this, "Please provide a valid email address", Toast.LENGTH_SHORT).show();
            }
            else if (str_password.length() < 6) {
                _pd.dismiss();
                Toast.makeText(RegisterActivity.this, "Password Must be at least 6 characters", Toast.LENGTH_SHORT).show();
            }
            else if (!str_password.equals(str_rePassword)) {
                _pd.dismiss();
                Toast.makeText(RegisterActivity.this, "Password doesn't match", Toast.LENGTH_SHORT).show();
            }
            else _onRegister(str_username, str_email, str_password);

        });

    }

    private void _onRegister(final String _username, String _email, String _password) {

        firebaseFirestore
                .collection("Users")
                .whereEqualTo("user_name", _username)
                .addSnapshotListener((v, e) -> {

                    if (v != null)
                        if (v.isEmpty()) {

                            firebaseAuth.createUserWithEmailAndPassword(_email, _password)
                                    .addOnCompleteListener(RegisterActivity.this, task -> {

                                        if (task.isSuccessful()){

                                            final String _userId = firebaseAuth.getCurrentUser().getUid();

                                            Map<String, Object> _user = new HashMap<>();
                                            _user.put("user_id", _userId);
                                            _user.put("user_name", _username);
                                            _user.put("user_email", _email);
                                            _user.put("user_balance", "250");

                                            firebaseFirestore
                                                    .collection("Users")
                                                    .document(_userId)
                                                    .set(_user)
                                                    .addOnSuccessListener(documentRef -> {

                                                        _pd.dismiss();
                                                        Toast.makeText(RegisterActivity.this, "You're Successfully Added!", Toast.LENGTH_LONG).show();
                                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                                        finish();
                                                    });
                                        }

                                    }).addOnFailureListener(_e -> {

                                if(_e.toString().contains("The email address is already in use by another account")) {
                                    _pd.dismiss();
                                    Toast.makeText(RegisterActivity.this, "Email is Already Exist!", Toast.LENGTH_SHORT).show();
                                }
                                else if (_e.toString().contains("A network error (such as timeout, interrupted connection or unreachable host) has occurred")) {
                                    _pd.dismiss();
                                    Toast.makeText(RegisterActivity.this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    _pd.dismiss();
                                    Toast.makeText(RegisterActivity.this, "Please Contact Your Service Provider", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else {
                            _pd.dismiss();
                            Toast.makeText(RegisterActivity.this, "Username is already taken!", Toast.LENGTH_SHORT).show();
                        }
                });

    }

}