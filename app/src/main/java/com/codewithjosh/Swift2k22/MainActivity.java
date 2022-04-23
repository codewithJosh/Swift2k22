package com.codewithjosh.Swift2k22;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorVividCerulean));
        getWindow().setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.colorVividCerulean));

        //hide action bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        Thread thread = new Thread(){

            @Override
            public void run() {

                try {

                    sleep(2000);
                }
                catch (Exception e) {

                    e.printStackTrace();
                }
                finally {

                    firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                    if (firebaseUser != null) startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    else startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();

                }
            }
        };

        thread.start();

    }

}