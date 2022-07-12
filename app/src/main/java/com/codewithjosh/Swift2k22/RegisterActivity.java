package com.codewithjosh.Swift2k22;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.codewithjosh.Swift2k22.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    Button btnRegister;
    EditText etUserName;
    EditText etEmail;
    EditText etPassword;
    EditText etRePassword;
    TextView navLogin;
    String userName;
    String email;
    String password;
    String rePassword;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser firebaseUser;
    DocumentReference documentRef;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        initInstances();
        buildButtons();

    }

    private void initViews() {

        etUserName = findViewById(R.id.et_user_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etRePassword = findViewById(R.id.et_re_password);
        btnRegister = findViewById(R.id.btn_register);
        navLogin = findViewById(R.id.nav_login);

    }

    private void initInstances() {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void buildButtons() {

        navLogin.setOnClickListener(v ->
        {

            startActivity(new Intent(this, LoginActivity.class));
            finish();

        });

        btnRegister.setOnClickListener(v ->
        {

            getString();

            if (validate(v)) checkUserName();

            else pd.dismiss();

        });

    }

    private void getString() {

        userName = etUserName.getText().toString().toLowerCase();
        email = etEmail.getText().toString().toLowerCase();
        password = etPassword.getText().toString();
        rePassword = etRePassword.getText().toString();

    }

    private boolean validate(final View v) {

        pd = new ProgressDialog(this);
        pd.setMessage("Signing up");
        pd.show();

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        if (getCurrentFocus() != null) getCurrentFocus().clearFocus();

        if (!isConnected()) Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

        else if (userName.isEmpty()
                || email.isEmpty()
                || password.isEmpty()
                || rePassword.isEmpty())
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();

        else if (userName.length() < 6)
            Toast.makeText(this, "Username must be at least 6 characters", Toast.LENGTH_SHORT).show();

        else if (!(email.contains("@") && email.endsWith(".com"))
                || email.startsWith("@")
                || email.contains("@.com"))
            Toast.makeText(this, "Provide a valid Email Address", Toast.LENGTH_SHORT).show();

        else if (password.length() < 6) Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();

        else if (!password.equals(rePassword)) Toast.makeText(this, "Password doesn't match", Toast.LENGTH_SHORT).show();

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

                    if (queryDocumentSnapshots != null)
                    {

                        if (!queryDocumentSnapshots.isEmpty())
                        {

                            pd.dismiss();
                            Toast.makeText(this, "Username is Already Taken!", Toast.LENGTH_SHORT).show();

                        }
                        else onRegister();

                    }


                });

    }

    private void onRegister() {

        firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult ->
                {

                    firebaseUser = firebaseAuth.getCurrentUser();

                    if (firebaseUser != null)
                    {

                        final int userBalance = 250;
                        final String userId = firebaseUser.getUid();

                        final UserModel user = new UserModel(
                                userBalance,
                                email,
                                userId,
                                userName
                        );

                        setUser(userId, user);

                    }

                }).addOnFailureListener(e ->
                {

                    pd.dismiss();

                    final String _e = e.toString().toLowerCase();

                    if (_e.contains("the email address is already in use by another account")) Toast.makeText(this, "Email is Already Exist!", Toast.LENGTH_SHORT).show();

                    else if (_e.contains("a network error (such as timeout, interrupted connection or unreachable host) has occurred")) Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

                    else Toast.makeText(this, "Please Contact Your Service Provider", Toast.LENGTH_SHORT).show();

                });

    }

    private void setUser(final String s_user_id, final UserModel user) {

        documentRef = firebaseFirestore
                .collection("Users")
                .document(s_user_id);

        documentRef
                .get()
                .addOnSuccessListener(documentSnapshot ->
                {

                    if (documentSnapshot != null)

                        if (!documentSnapshot.exists())
                        {

                            documentRef
                                    .set(user)
                                    .addOnSuccessListener(unused ->
                                    {

                                        Toast.makeText(this, "You're Successfully Added!", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(this, LoginActivity.class));
                                        finish();

                                    })
                                    .addOnFailureListener(e ->
                                    {

                                        pd.dismiss();
                                        Toast.makeText(this, "Please Contact Your Service Provider", Toast.LENGTH_SHORT).show();

                                    });

                        }

                });

    }

}