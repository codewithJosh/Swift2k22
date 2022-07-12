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

    Button btnPayment;
    TextView tvRouteName;
    TextView tvBusNumber;
    TextView tvUserBalance;
    TextView tvBusFare;
    TextView tvTotalAmount;
    Date dateBusTimestamp;
    String userId;
    String busId;
    String routeName;
    String busNumber;
    int busFare;
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
                .document(userId);

        userRef
                .addSnapshotListener((value, error) ->
                {

                    if (value != null) {

                        final UserModel user = value.toObject(UserModel.class);

                        if (user != null) {

                            final int userBalance = user.getUser_balance();
                            final int futureUserBalance = userBalance - busFare;
                            final String _userBalance = "PHP " + userBalance + ".00";

                            tvUserBalance.setText(_userBalance);

                            btnPayment.setOnClickListener(v ->
                            {

                                pd = new ProgressDialog(this);
                                pd.setMessage("Please wait");
                                pd.show();

                                if (futureUserBalance >= 0) {

                                    final String ticketId = firebaseFirestore
                                            .collection("Tickets")
                                            .document()
                                            .getId();

                                    final TicketModel ticket = new TicketModel(
                                            busId,
                                            dateBusTimestamp,
                                            routeName,
                                            ticketId,
                                            userId
                                    );

                                    final HashMap<String, Object> _user = new HashMap<>();
                                    _user.put("user_balance", futureUserBalance);

                                    ticketRef = firebaseFirestore
                                            .collection("Tickets")
                                            .document(ticketId);

                                    ticketRef
                                            .get()
                                            .addOnSuccessListener(_documentSnapshot ->
                                            {

                                                if (_documentSnapshot != null)

                                                    if (!_documentSnapshot.exists())

                                                        ticketRef
                                                                .set(ticket)
                                                                .addOnSuccessListener(runnable ->

                                                                        userRef
                                                                                .update(_user)
                                                                                .addOnSuccessListener(_runnable ->
                                                                                {

                                                                                    editor.putString("ticket_id", ticketId);
                                                                                    editor.apply();

                                                                                    Toast.makeText(this, "Transaction complete!", Toast.LENGTH_SHORT).show();
                                                                                    startActivity(new Intent(this, ViewTicketActivity.class));
                                                                                    finish();

                                                                                }));

                                            });

                                } else {

                                    pd.dismiss();
                                    Toast.makeText(this, "You have insufficient balance!", Toast.LENGTH_SHORT).show();

                                }

                            });

                        }

                    }

                });

    }

    private void initViews() {

        btnPayment = findViewById(R.id.btn_payment);
        tvRouteName = findViewById(R.id.tv_route_name);
        tvBusNumber = findViewById(R.id.tv_bus_number);
        tvUserBalance = findViewById(R.id.tv_user_balance);
        tvBusFare = findViewById(R.id.tv_bus_fare);
        tvTotalAmount = findViewById(R.id.tv_total_amount);

    }

    private void initInstances() {

        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void initSharedPref() {

        sharedPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

    }

    private void load() {

        userId = sharedPref.getString("user_id", String.valueOf(Context.MODE_PRIVATE));
        busId = sharedPref.getString("bus_id", String.valueOf(Context.MODE_PRIVATE));
        routeName = sharedPref.getString("route_name", String.valueOf(Context.MODE_PRIVATE));
        busNumber = sharedPref.getString("bus_number", String.valueOf(Context.MODE_PRIVATE));
        busFare = sharedPref.getInt("bus_fare", Context.MODE_PRIVATE);

        final String _busFare = "PHP " + busFare + ".00";
        final String __busFare = "PAY " + _busFare;

        tvRouteName.setText(routeName);
        tvBusNumber.setText(busNumber);
        tvBusFare.setText(_busFare);
        tvTotalAmount.setText(_busFare);
        btnPayment.setText(__busFare);

        firebaseFirestore
                .collection("Buses")
                .document(busId)
                .get()
                .addOnSuccessListener(documentSnapshot ->
                {

                    if (documentSnapshot != null) {

                        final BusModel bus = documentSnapshot.toObject(BusModel.class);

                        if (bus != null) dateBusTimestamp = bus.getBus_timestamp();

                    }

                });

    }

}