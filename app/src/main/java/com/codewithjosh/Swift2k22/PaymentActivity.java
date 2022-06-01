package com.codewithjosh.Swift2k22;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.codewithjosh.Swift2k22.models.BusModel;
import com.codewithjosh.Swift2k22.models.TicketModel;
import com.codewithjosh.Swift2k22.models.UserModel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;

public class PaymentActivity extends AppCompatActivity {

    Button btn_payment;
    TextView tv_route_name;
    TextView tv_bus_number;
    TextView tv_user_balance;
    TextView tv_bus_fare;
    TextView tv_total_amount;
    Date date_bus_timestamp;
    String s_user_id;
    String s_bus_id;
    String s_route_name;
    String s_bus_number;
    int i_bus_fare;
    FirebaseFirestore firebaseFirestore;
    DocumentReference ticketRef;
    DocumentReference userRef;
    ProgressDialog pd;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initViews();
        initInstances();
        initSharedPref();
        load();

        userRef = firebaseFirestore
                .collection("Users")
                .document(s_user_id);

        userRef
                .addSnapshotListener((value, error) ->
                {

                    if (value != null) {

                        final UserModel user = value.toObject(UserModel.class);

                        final int i_user_balance = user.getUser_balance();
                        final int i_future_user_balance = i_user_balance - i_bus_fare;
                        final String s_user_balance = "PHP " + i_user_balance + ".00";

                        tv_user_balance.setText(s_user_balance);

                        btn_payment.setOnClickListener(v ->
                        {

                            pd = new ProgressDialog(this);
                            pd.setMessage("Please wait");
                            pd.show();

                            if (i_future_user_balance >= 0) {

                                final String s_ticket_id = firebaseFirestore
                                        .collection("Tickets")
                                        .document()
                                        .getId();

                                final TicketModel ticket = new TicketModel(
                                        s_bus_id,
                                        date_bus_timestamp,
                                        s_route_name,
                                        s_ticket_id,
                                        s_user_id
                                );

                                final HashMap<String, Object> _user = new HashMap<>();
                                _user.put("user_balance", i_future_user_balance);

                                ticketRef = firebaseFirestore
                                        .collection("Tickets")
                                        .document(s_ticket_id);

                                ticketRef
                                        .get()
                                        .addOnSuccessListener(_documentSnapshot ->
                                        {

                                            if (_documentSnapshot != null)

                                                if (!_documentSnapshot.exists())

                                                    ticketRef
                                                            .set(ticket)
                                                            .addOnSuccessListener(runnable ->
                                                            {

                                                                userRef
                                                                        .update(_user)
                                                                        .addOnSuccessListener(_runnable ->
                                                                        {

                                                                            editor.putString("s_ticket_id", s_ticket_id);
                                                                            editor.apply();

                                                                            Toast.makeText(this, "Transaction complete!", Toast.LENGTH_SHORT).show();
                                                                            startActivity(new Intent(this, ViewTicketActivity.class));
                                                                            finish();

                                                                        });

                                                            });

                                        });

                            } else {

                                pd.dismiss();
                                Toast.makeText(this, "You have insufficient balance!", Toast.LENGTH_SHORT).show();

                            }

                        });

                    }

                });


    }

    private void initViews() {

        btn_payment = findViewById(R.id.btn_payment);
        tv_route_name = findViewById(R.id.tv_route_name);
        tv_bus_number = findViewById(R.id.tv_bus_number);
        tv_user_balance = findViewById(R.id.tv_user_balance);
        tv_bus_fare = findViewById(R.id.tv_bus_fare);
        tv_total_amount = findViewById(R.id.tv_total_amount);

    }

    private void initInstances() {

        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void initSharedPref() {

        sharedPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

    }

    private void load() {

        s_user_id = sharedPref.getString("s_user_id", String.valueOf(Context.MODE_PRIVATE));
        s_bus_id = sharedPref.getString("s_bus_id", String.valueOf(Context.MODE_PRIVATE));
        s_route_name = sharedPref.getString("s_route_name", String.valueOf(Context.MODE_PRIVATE));
        s_bus_number = sharedPref.getString("s_bus_number", String.valueOf(Context.MODE_PRIVATE));
        i_bus_fare = sharedPref.getInt("i_bus_fare", Context.MODE_PRIVATE);

        final String s_bus_fare = "PHP " + i_bus_fare + ".00";
        final String _s_bus_fare = "PAY " + s_bus_fare;

        tv_route_name.setText(s_route_name);
        tv_bus_number.setText(s_bus_number);
        tv_bus_fare.setText(s_bus_fare);
        tv_total_amount.setText(s_bus_fare);
        btn_payment.setText(_s_bus_fare);

        firebaseFirestore
                .collection("Buses")
                .document(s_bus_id)
                .get()
                .addOnSuccessListener(documentSnapshot ->
                {

                    if (documentSnapshot != null)
                    {

                        final BusModel bus = documentSnapshot.toObject(BusModel.class);

                        if (bus != null) date_bus_timestamp = bus.getBus_timestamp();

                    }

                });

    }

}