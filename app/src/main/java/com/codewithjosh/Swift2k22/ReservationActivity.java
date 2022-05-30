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

public class ReservationActivity extends AppCompatActivity
{

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private BusAdapter busAdapter;
    private List<BusModel> busList;
    RecyclerView recycler_bus;
    TextView tv_route_name;
    TextView tv_bus_date_timestamp;
    String s_route_id;
    String s_route_name;
    String s_bus_date_timestamp;
    FirebaseFirestore firebaseFirestore;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        initViews();
        initInstances();
        initSharedPref();
        load();
        loadBuses();

    }

    private void initViews()
    {

        recycler_bus = findViewById(R.id.recycler_bus);
        tv_route_name = findViewById(R.id.tv_route_name);
        tv_bus_date_timestamp = findViewById(R.id.tv_bus_date_timestamp);

        recycler_bus.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recycler_bus.setLayoutManager(linearLayoutManager);
        busList = new ArrayList<>();
        busAdapter = new BusAdapter(this, busList);
        recycler_bus.setAdapter(busAdapter);

    }

    private void initInstances()
    {

        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void initSharedPref()
    {

        sharedPref = getSharedPreferences("user", Context.MODE_PRIVATE);

    }

    private void load()
    {

        s_route_id = sharedPref.getString("s_route_id", String.valueOf(Context.MODE_PRIVATE));
        s_route_name = sharedPref.getString("s_route_name", String.valueOf(Context.MODE_PRIVATE));
        s_bus_date_timestamp = sharedPref.getString("s_bus_date_timestamp", String.valueOf(Context.MODE_PRIVATE));

        tv_route_name.setText(s_route_name);
        tv_bus_date_timestamp.setText(s_bus_date_timestamp);

    }

    private void loadBuses()
    {

        firebaseFirestore
                .collection("Buses")
                .whereEqualTo("route_id", s_route_id)
                .addSnapshotListener((value, error) ->
                {

                    if (value != null)
                    {

                        if (!isConnected()) Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

                        else onLoadBuses(value);

                    }

                });

    }

    private boolean isConnected()
    {

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void onLoadBuses(final QuerySnapshot value)
    {

        busList.clear();
        for (QueryDocumentSnapshot snapshot : value)
        {

            final BusModel bus = snapshot.toObject(BusModel.class);
            final Date date = bus.getBus_timestamp();

            long time = date != null ? date.getTime() : 0;
            if (time < 1000000000000L) time *= 1000;

            long now = currentDate().getTime();

            final long diff = now - time;

            if (diff < 30 * MINUTE_MILLIS)
            {

                busList.add(bus);
                Collections.sort(busList, BusModel.comparator);

            }
            busAdapter.notifyDataSetChanged();

        }

    }

    private static Date currentDate()
    {

        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();

    }

}