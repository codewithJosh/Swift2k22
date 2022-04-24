package com.codewithjosh.Swift2k22;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

public class ReservationActivity extends AppCompatActivity {

    RecyclerView _recyclerBus;
    TextView _routeName, _routeTimestamp;

    String route_id, route_name, route_timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        _recyclerBus = findViewById(R.id.recycler_bus);
        _routeName = findViewById(R.id.route_name);
        _routeTimestamp = findViewById(R.id.bus_date_timestamp);

        route_id = getIntent().getStringExtra("route_id");
        route_name = getIntent().getStringExtra("route_name");

        _routeName.setText(route_name);
        _routeTimestamp.setText(route_timestamp);

    }

}