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

    Button nav_ticket;
    RecyclerView recycler_routes;
    FirebaseFirestore firebaseFirestore;
    private RouteAdapter routeAdapter;
    private List<BusModel> busList;
    private List<RouteModel> routeList;

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

        nav_ticket = findViewById(R.id.nav_ticket);
        recycler_routes = findViewById(R.id.recycler_routes);

        recycler_routes.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recycler_routes.setLayoutManager(linearLayoutManager);
        routeList = new ArrayList<>();
        busList = new ArrayList<>();
        routeAdapter = new RouteAdapter(this, routeList, busList);
        recycler_routes.setAdapter(routeAdapter);

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

                        if (!isConnected())
                            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

                        else onLoadRoutes(value);

                    }

                });

    }

    private boolean isConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void onLoadRoutes(final QuerySnapshot value) {

        routeList.clear();
        for (QueryDocumentSnapshot snapshot : value) {

            final RouteModel route = snapshot.toObject(RouteModel.class);

            routeList.add(route);

        }
        routeAdapter.notifyDataSetChanged();

    }

    private void buildButtons() {

        nav_ticket.setOnClickListener(v ->
        {
            startActivity(new Intent(this, TicketActivity.class));
            finish();
        });

    }

}