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

    Button btn_register;
    EditText et_user_name, et_email, et_password, et_re_password;
    TextView nav_login;

    String s_user_name, s_email, s_password, s_re_password;

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

        et_user_name = findViewById(R.id.et_user_name);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        et_re_password = findViewById(R.id.et_re_password);
        btn_register = findViewById(R.id.btn_register);
        nav_login = findViewById(R.id.nav_login);

    }

    private void initInstances() {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void buildButtons() {

        nav_login.setOnClickListener(v ->
        {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btn_register.setOnClickListener(v ->
        {

            getString();

            if (validate(v)) checkUserName();

            else pd.dismiss();

        });

    }

    private void getString() {

        s_user_name = et_user_name.getText().toString().toLowerCase();
        s_email = et_email.getText().toString().toLowerCase();
        s_password = et_password.getText().toString();
        s_re_password = et_re_password.getText().toString();

    }

    private boolean validate(final View v) {

        pd = new ProgressDialog(this);
        pd.setMessage("Signing up");
        pd.show();

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        if (getCurrentFocus() != null) getCurrentFocus().clearFocus();

        if (!isConnected())
            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

        else if (s_user_name.isEmpty() || s_email.isEmpty()
                || s_password.isEmpty() || s_re_password.isEmpty())
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();

        else if (s_user_name.length() < 6)
            Toast.makeText(this, "Username must be at least 6 characters", Toast.LENGTH_SHORT).show();

        else if (!(s_email.contains("@") && s_email.endsWith(".com"))
                || s_email.startsWith("@")
                || s_email.contains("@.com"))
            Toast.makeText(this, "Provide a valid Email Address", Toast.LENGTH_SHORT).show();

        else if (s_password.length() < 6)
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();

        else if (!s_password.equals(s_re_password))
            Toast.makeText(this, "Password doesn't match", Toast.LENGTH_SHORT).show();

        else return true;

        return false;

    }

    private boolean isConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void checkUserName() {

        firebaseFirestore
                .collection("Users")
                .whereEqualTo("user_name", s_user_name)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                {

                    if (queryDocumentSnapshots != null) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            pd.dismiss();
                            Toast.makeText(this, "Username is Already Taken!", Toast.LENGTH_SHORT).show();

                        } else onRegister();

                    }


                });

    }

    private void onRegister() {

        firebaseAuth
                .createUserWithEmailAndPassword(s_email, s_password)
                .addOnSuccessListener(authResult ->
                {

                    firebaseUser = firebaseAuth.getCurrentUser();

                    if (firebaseUser != null) {

                        final int i_user_balance = 250;
                        final String s_user_id = firebaseUser.getUid();

                        final UserModel user = new UserModel(
                                i_user_balance,
                                s_email,
                                s_user_id,
                                s_user_name
                        );

                        setUser(s_user_id, user);

                    }

                }).addOnFailureListener(e ->
                {

                    pd.dismiss();

                    final String s_e = e.toString().toLowerCase();

                    if (s_e.contains("the email address is already in use by another account"))
                        Toast.makeText(this, "Email is Already Exist!", Toast.LENGTH_SHORT).show();

                    else if (s_e.contains("a network error (such as timeout, interrupted connection or unreachable host) has occurred"))
                        Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

                    else
                        Toast.makeText(this, "Please Contact Your Service Provider", Toast.LENGTH_SHORT).show();

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

                        if (!documentSnapshot.exists()) {

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