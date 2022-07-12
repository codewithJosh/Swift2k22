package com.codewithjosh.Swift2k22;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.codewithjosh.Swift2k22.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    Button btnLogin;
    Button navRegister;
    EditText etUserName;
    EditText etPassword;
    String userName;
    String password;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser firebaseUser;
    ProgressDialog pd;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        initInstances();
        initSharedPref();
        buildButtons();

    }

    private void initViews() {

        etUserName = findViewById(R.id.et_user_name);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        navRegister = findViewById(R.id.nav_register);

    }

    private void initInstances() {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void initSharedPref() {

        editor = getSharedPreferences("user", MODE_PRIVATE).edit();

    }

    private void buildButtons() {

        navRegister.setOnClickListener(v ->
        {

            startActivity(new Intent(this, RegisterActivity.class));
            finish();

        });

        btnLogin.setOnClickListener(v ->
        {

            getString();

            if (validate(v)) checkUserName();

            else pd.dismiss();

        });

    }

    private void getString() {

        userName = etUserName.getText().toString().toLowerCase();
        password = etPassword.getText().toString();

    }

    private boolean validate(final View v) {

        pd = new ProgressDialog(this);
        pd.setMessage("Logging in");
        pd.show();

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        if (getCurrentFocus() != null) getCurrentFocus().clearFocus();

        if (!isConnected())
            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

        else if (userName.isEmpty() || password.isEmpty())
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();

        else if (password.length() < 6)
            Toast.makeText(this, "Password Must be at least 6 characters", Toast.LENGTH_SHORT).show();

        else return true;

        return false;

    }

    private boolean isConnected() {

        final ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void checkUserName() {

        firebaseFirestore
                .collection("Users")
                .whereEqualTo("user_name", userName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                {

                    if (queryDocumentSnapshots != null) {

                        if (!queryDocumentSnapshots.isEmpty())

                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {

                                final UserModel user = snapshot.toObject(UserModel.class);
                                onLogin(user);

                            }

                        else {

                            pd.dismiss();
                            Toast.makeText(this, "User Doesn't Exist!", Toast.LENGTH_SHORT).show();

                        }

                    }

                });

    }

    private void onLogin(final UserModel user) {

        final String email = user.getUser_email();

        if (email != null)

            firebaseAuth
                    .signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult ->
                    {

                        firebaseUser = authResult.getUser();

                        if (firebaseUser != null) {

                            final String userId = firebaseUser.getUid();

                            editor.putString("user_id", userId);
                            editor.apply();

                            Toast.makeText(this, "Welcome, You've Successfully Login!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(this, HomeActivity.class));
                            finish();

                        }

                    }).addOnFailureListener(e ->
                    {

                        pd.dismiss();

                        final String _e = e.toString().toLowerCase();

                        if (_e.contains("the password is invalid or the user does not have a password"))
                            Toast.makeText(this, "Incorrect Password!", Toast.LENGTH_SHORT).show();

                        else if (_e.contains("a network error (such as timeout, interrupted connection or unreachable host) has occurred"))
                            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

                        else
                            Toast.makeText(this, "Please Contact Your Service Provider", Toast.LENGTH_SHORT).show();

                    });

    }

}