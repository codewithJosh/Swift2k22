package com.codewithjosh.Swift2k22;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    Button _onPay;
    TextView _routeName, _busNumber, _userBalance, _busFare, _totalAmt;

    String route_name, bus_number, bus_fare, bus_id;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    DocumentReference userRef;

    ProgressDialog _pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        _onPay = findViewById(R.id.on_pay);
        _routeName = findViewById(R.id.route_name);
        _busNumber = findViewById(R.id.bus_number);
        _userBalance = findViewById(R.id.user_balance);
        _busFare = findViewById(R.id.bus_fare);
        _totalAmt = findViewById(R.id.total_amt);

        bus_id = getIntent().getStringExtra("bus_id");
        route_name = getIntent().getStringExtra("route_name");
        bus_number = getIntent().getStringExtra("bus_number");
        bus_fare = getIntent().getStringExtra("bus_fare");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        if (route_name != null) {

            final String _fare = "PHP " + bus_fare + ".00";

            _routeName.setText(route_name);
            _busNumber.setText("BUS NO. " + bus_number);
            _busFare.setText(_fare);
            _totalAmt.setText(_fare);
            _onPay.setText("PAY " + _fare);
        }

        final String user_id = firebaseAuth.getCurrentUser().getUid();

        userRef = firebaseFirestore
                .collection("Users")
                .document(user_id);

        userRef
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        DocumentSnapshot doc = task.getResult();

                        final int user_balance = Integer.parseInt(doc.getString("user_balance"));
                        final int _processPayment = user_balance - Integer.parseInt(bus_fare);

                        if (user_balance >= 0) _userBalance.setText("PHP " + user_balance + ".00");

                        _onPay.setOnClickListener(v -> {
                            _pd = new ProgressDialog(this);
                            _pd.setMessage("Please wait");
                            _pd.show();

                            if (_processPayment >= 0) {

                                final String _ticketId = firebaseFirestore
                                        .collection("Tickets")
                                        .document()
                                        .getId();

                                Map<String, Object> _ticket = new HashMap<>();
                                _ticket.put("user_id", user_id);
                                _ticket.put("bus_id", bus_id);
                                _ticket.put("ticket", _ticketId);

                                firebaseFirestore
                                        .collection("Tickets")
                                        .whereEqualTo("bus_id", bus_id)
                                        .whereEqualTo("user_id", user_id)
                                        .get()
                                        .addOnCompleteListener(_task -> {

                                            if (_task.getResult() != null) {

                                                if (_task.getResult().isEmpty()) {

                                                    firebaseFirestore
                                                            .collection("Tickets")
                                                            .document(_ticketId)
                                                            .set(_ticket)
                                                            .addOnCompleteListener(__task -> {

                                                                userRef
                                                                        .update("user_balance", String.valueOf(_processPayment))
                                                                        .addOnCompleteListener(___task -> {

                                                                            _pd.dismiss();
                                                                            Toast.makeText(this, "Transaction complete!", Toast.LENGTH_SHORT).show();
                                                                            Intent i = new Intent(this, TicketActivity.class);
                                                                            i.putExtra("bus_fare", bus_fare);
                                                                            i.putExtra("bus_id", bus_id);
                                                                            i.putExtra("ticket_id", _ticketId);
                                                                            startActivity(i);
                                                                            finish();
                                                                        });
                                                            });
                                                } else {
                                                    _pd.dismiss();
                                                    Toast.makeText(this, "Reservation already booked!", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(this, HomeActivity.class));
                                                    finish();
                                                }
                                            }
                                        });
                            } else {
                                _pd.dismiss();
                                Toast.makeText(this, "You have insufficient balance!", Toast.LENGTH_SHORT).show();
                            }

                        });
                    }
                });

    }

}