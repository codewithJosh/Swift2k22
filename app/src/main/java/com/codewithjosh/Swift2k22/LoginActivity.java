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

    Button btn_login, nav_register;
    EditText et_user_name, et_password;

    String s_user_name, s_password;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser firebaseUser;

    ProgressDialog pd;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        initInstances();
        initSharedPref();
        buildButtons();

    }

    private void initViews()
    {

        et_user_name = findViewById(R.id.et_user_name);
        et_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btn_login);
        nav_register = findViewById(R.id.nav_register);

    }

    private void initInstances()
    {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void initSharedPref()
    {

        sharedPref = getSharedPreferences("user", MODE_PRIVATE);
        editor = sharedPref.edit();

    }

    private void buildButtons()
    {

        nav_register.setOnClickListener(v ->
        {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

        btn_login.setOnClickListener(v ->
        {

            getString();

            if (validate(v)) checkUserName();

            else pd.dismiss();

        });

    }

    private void getString()
    {

        s_user_name = et_user_name.getText().toString().toLowerCase();
        s_password = et_password.getText().toString();

    }

    private boolean validate(final View v)
    {

        pd = new ProgressDialog(this);
        pd.setMessage("Logging in");
        pd.show();

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        if (getCurrentFocus() != null) getCurrentFocus().clearFocus();

        if (!isConnected())
            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

        else if (s_user_name.isEmpty() || s_password.isEmpty())
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();

        else if (s_password.length() < 6)
            Toast.makeText(this, "Password Must be at least 6 characters", Toast.LENGTH_SHORT).show();

        else return true;

        return false;

    }

    private boolean isConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void checkUserName()
    {

        firebaseFirestore
                .collection("Users")
                .whereEqualTo("user_name", s_user_name)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                {

                    if (queryDocumentSnapshots != null)
                    {

                        if (!queryDocumentSnapshots.isEmpty())

                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots)
                            {

                                final UserModel user = snapshot.toObject(UserModel.class);
                                onLogin(user);

                            }

                        else
                        {

                            pd.dismiss();
                            Toast.makeText(this, "User Doesn't Exist!", Toast.LENGTH_SHORT).show();

                        }

                    }

                });

    }

    private void onLogin(final UserModel user)
    {

        final String s_email = user.getUser_email();

        if (s_email != null)

            firebaseAuth
                    .signInWithEmailAndPassword(s_email, s_password)
                    .addOnSuccessListener(authResult ->
                    {

                        firebaseUser = authResult.getUser();

                        if (firebaseUser != null) {

                            final String s_user_id = firebaseUser.getUid();

                            editor.putString("s_user_id", s_user_id);
                            editor.apply();

                            Toast.makeText(this, "Welcome, You've Successfully Login!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(this, HomeActivity.class));
                            finish();

                        }

                    }).addOnFailureListener(e ->
                    {

                        pd.dismiss();

                        final String s_e = e.toString().toLowerCase();

                        if (s_e.contains("the password is invalid or the user does not have a password"))
                            Toast.makeText(this, "Incorrect Password!", Toast.LENGTH_SHORT).show();

                        else if (s_e.contains("a network error (such as timeout, interrupted connection or unreachable host) has occurred"))
                            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

                        else
                            Toast.makeText(this, "Please Contact Your Service Provider", Toast.LENGTH_SHORT).show();

                    });

    }

    private void _onLogin(String _username, String _password)
    {

        firebaseFirestore
                .collection("Users")
                .whereEqualTo("user_name", _username)
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        if (task.getResult().isEmpty()) {

                            pd.dismiss();
                            Toast.makeText(LoginActivity.this, "User Doesn't Exist!", Toast.LENGTH_SHORT).show();
                        } else {

                            for (QueryDocumentSnapshot snapshot : task.getResult()) {

                                final String _email = snapshot.get("user_email").toString();

                                firebaseAuth.signInWithEmailAndPassword(_email, _password)
                                        .addOnCompleteListener(LoginActivity.this, _task -> {

                                            if (_task.isSuccessful()) {

                                                pd.dismiss();
                                                Toast.makeText(LoginActivity.this, "Welcome, You've Successfully Login!", Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                                finish();
                                            }

                                        }).addOnFailureListener(e -> {

                                    if (e.toString().contains("The password is invalid or the user does not have a password")) {
                                        pd.dismiss();
                                        Toast.makeText(LoginActivity.this, "Incorrect Password!", Toast.LENGTH_SHORT).show();
                                    } else if (e.toString().contains("A network error (such as timeout, interrupted connection or unreachable host) has occurred")) {
                                        pd.dismiss();
                                        Toast.makeText(LoginActivity.this, "Internet Connection Lost!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        pd.dismiss();
                                        Toast.makeText(LoginActivity.this, "Please Contact Your Service Provider", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                    }
                });

    }

}