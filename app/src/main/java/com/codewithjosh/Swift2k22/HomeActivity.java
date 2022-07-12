package com.codewithjosh.Swift2k22;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codewithjosh.Swift2k22.adapters.RouteAdapter;
import com.codewithjosh.Swift2k22.models.BusModel;
import com.codewithjosh.Swift2k22.models.RouteModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    Button navTicket;
    RecyclerView recyclerRoutes;
    FirebaseFirestore firebaseFirestore;
    private RouteAdapter routeAdapter;
    private List<BusModel> buses;
    private List<RouteModel> routes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        initInstances();
        loadRoutes();
        buildButtons();

    }

    private void initViews() {

        navTicket = findViewById(R.id.nav_ticket);
        recyclerRoutes = findViewById(R.id.recycler_routes);

        initRecyclerView();

        routes = new ArrayList<>();
        buses = new ArrayList<>();
        routeAdapter = new RouteAdapter(this, routes, buses);
        recyclerRoutes.setAdapter(routeAdapter);

    }

    private void initRecyclerView() {

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerRoutes.setLayoutManager(linearLayoutManager);
        recyclerRoutes.setHasFixedSize(true);

    }

    private void initInstances() {

        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void loadRoutes() {

        firebaseFirestore
                .collection("Routes")
                .addSnapshotListener((value, error) ->
                {

                    if (value != null) {

                        if (isConnected()) onLoadRoutes(value);

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

    private void onLoadRoutes(final QuerySnapshot value) {

        routes.clear();
        for (QueryDocumentSnapshot snapshot : value) {

            final RouteModel route = snapshot.toObject(RouteModel.class);

            routes.add(route);

        }
        routeAdapter.notifyDataSetChanged();

    }

    private void buildButtons() {

        navTicket.setOnClickListener(v -> startActivity(new Intent(this, TicketActivity.class)));

    }

}