package com.codewithjosh.Swift2k22;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PaymentActivity extends AppCompatActivity {

    Button _onPay;
    TextView _routeName, _busNumber, _userBalance, _busFare, _totalAmt;

    String route_name, bus_number, bus_fare, bus_id;

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

        if (route_name != null) {

            final String _fare = "PHP "+ bus_fare +".00";

            _routeName.setText(route_name);
            _busNumber.setText("BUS NO. " + bus_number);
            _busFare.setText(_fare);
            _totalAmt.setText(_fare);
            _onPay.setText("PAY " + _fare);
        }

    }

}