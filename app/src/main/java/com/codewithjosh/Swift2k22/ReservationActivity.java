package com.codewithjosh.Swift2k22;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codewithjosh.Swift2k22.adapters.BusAdapter;
import com.codewithjosh.Swift2k22.models.BusModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ReservationActivity extends AppCompatActivity {

    private static final int secondMillis = 1000;
    private static final int minuteMillis = 60 * secondMillis;
    RecyclerView recyclerBus;
    TextView tvRouteName;
    TextView tvBusDateTimestamp;
    String routeId;
    String routeName;
    String busDateTimestamp;
    FirebaseFirestore firebaseFirestore;
    SharedPreferences sharedPref;
    private BusAdapter busAdapter;
    private List<BusModel> buses;

    private static Date currentDate() {

        final Calendar calendar = Calendar.getInstance();
        return calendar.getTime();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        initViews();
        initInstances();
        initSharedPref();
        load();
        loadBuses();

    }

    private void initViews() {

        recyclerBus = findViewById(R.id.recycler_bus);
        tvRouteName = findViewById(R.id.tv_route_name);
        tvBusDateTimestamp = findViewById(R.id.tv_bus_date_timestamp);

        initRecyclerView();

        buses = new ArrayList<>();
        busAdapter = new BusAdapter(this, buses);
        recyclerBus.setAdapter(busAdapter);

    }

    private void initRecyclerView() {

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerBus.setLayoutManager(linearLayoutManager);
        recyclerBus.setHasFixedSize(true);

    }

    private void initInstances() {

        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void initSharedPref() {

        sharedPref = getSharedPreferences("user", Context.MODE_PRIVATE);

    }

    private void load() {

        routeId = sharedPref.getString("route_id", String.valueOf(Context.MODE_PRIVATE));
        routeName = sharedPref.getString("route_name", String.valueOf(Context.MODE_PRIVATE));
        busDateTimestamp = sharedPref.getString("bus_date_timestamp", String.valueOf(Context.MODE_PRIVATE));

        tvRouteName.setText(routeName);
        tvBusDateTimestamp.setText(busDateTimestamp);

    }

    private void loadBuses() {

        firebaseFirestore
                .collection("Buses")
                .whereEqualTo("route_id", routeId)
                .addSnapshotListener((value, error) ->
                {

                    if (value != null) {

                        if (isConnected()) onLoadBuses(value);

                        else
                            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

                    }

                });

    }

    private boolean isConnected() {

        final ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void onLoadBuses(final QuerySnapshot value) {

        buses.clear();
        for (QueryDocumentSnapshot snapshot : value) {

            final BusModel bus = snapshot.toObject(BusModel.class);
            final Date date = bus.getBus_timestamp();

            long time = date != null
                    ? date.getTime()
                    : 0;

            if (time < 1000000000000L) time *= 1000;

            long now = currentDate().getTime();

            final long diff = now - time;

            if (diff < 30 * minuteMillis) {

                buses.add(bus);
                Collections.sort(buses, BusModel.comparator);

            }
            busAdapter.notifyDataSetChanged();

        }

    }

}