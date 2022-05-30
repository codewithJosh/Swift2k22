package com.codewithjosh.Swift2k22;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseUser firebaseUser;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSharedPref();

        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorVividCerulean));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorVividCerulean));

        //hide action bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        Thread thread = new Thread() {

            @Override
            public void run() {

                try {

                    sleep(2000);
                } catch (Exception e) {

                    e.printStackTrace();
                } finally {

                    if (isConnected()) checkCurrentAuthState();

                    else
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "No Internet Connection!", Toast.LENGTH_LONG).show());

                }

            }
        };

        thread.start();

    }

    private void initSharedPref() {

        sharedPref = getSharedPreferences("user", MODE_PRIVATE);
        editor = sharedPref.edit();

    }

    private boolean isConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void checkCurrentAuthState() {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {

            final String s_user_id = firebaseUser.getUid();

            editor.putString("s_user_id", s_user_id);
            editor.apply();

            startActivity(new Intent(this, HomeActivity.class));
        } else startActivity(new Intent(this, LoginActivity.class));

        finish();

    }

}