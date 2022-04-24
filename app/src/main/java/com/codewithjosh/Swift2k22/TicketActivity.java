package com.codewithjosh.Swift2k22;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class TicketActivity extends AppCompatActivity {

    ImageView _barcode;
    TextView _busFare, _totalAmt, _ref, _busTimestamp;

    String bus_fare, bus_id, ticket_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);

        _barcode = findViewById(R.id.barcode);
        _busFare = findViewById(R.id.bus_fare);
        _totalAmt = findViewById(R.id.total_amt);
        _ref = findViewById(R.id.ref);
        _busTimestamp = findViewById(R.id.bus_timestamp);

        bus_fare = getIntent().getStringExtra("bus_fare");
        bus_id = getIntent().getStringExtra("bus_id");
        ticket_id = getIntent().getStringExtra("ticket_id");

        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorBlueJeans));
        getWindow().setStatusBarColor(ContextCompat.getColor(TicketActivity.this, R.color.colorBlueJeans));

        _busFare.setText("PHP " + bus_fare + ".00");
        _totalAmt.setText("PHP " + bus_fare + ".00");
        _ref.setText("Ref No: " + ticket_id.toUpperCase());

    }

}