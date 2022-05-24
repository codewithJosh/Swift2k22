package com.codewithjosh.Swift2k22;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codewithjosh.Swift2k22.adapters.BusAdapter;
import com.codewithjosh.Swift2k22.models.BusModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReservationActivity extends AppCompatActivity {

    RecyclerView _recyclerBus;
    TextView _routeName, _routeTimestamp;
    String route_id, route_name, route_timestamp;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    private BusAdapter _busAdapter;
    private List<BusModel> _busList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        _recyclerBus = findViewById(R.id.recycler_bus);
        _routeName = findViewById(R.id.route_name);
        _routeTimestamp = findViewById(R.id.bus_date_timestamp);

        route_id = getIntent().getStringExtra("route_id");
        route_name = getIntent().getStringExtra("route_name");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        _routeName.setText(route_name);
        _routeTimestamp.setText(route_timestamp);

        _recyclerBus.setHasFixedSize(true);
        LinearLayoutManager _linearLayoutManager = new LinearLayoutManager(this);
        _linearLayoutManager.setReverseLayout(true);
        _linearLayoutManager.setStackFromEnd(true);
        _recyclerBus.setLayoutManager(_linearLayoutManager);
        _busList = new ArrayList<>();
        _busAdapter = new BusAdapter(this, _busList, route_name);
        _recyclerBus.setAdapter(_busAdapter);

        _readRoutes();

    }

    private void _readRoutes() {

        firebaseFirestore
                .collection("Buses")
                .whereEqualTo("route_id", route_id)
                .get()
                .addOnCompleteListener(task -> {

                    _busList.clear();
                    for (QueryDocumentSnapshot _snapshot : task.getResult()) {

                        final BusModel bus = _snapshot.toObject(BusModel.class);
                        final String s_bus_id = bus.getBus_id();

                        firebaseFirestore
                                .collection("Buses")
                                .document(s_bus_id)
                                .collection("Tickets")
                                .addSnapshotListener((v, e) -> {

                                    if (v != null)
                                    {

                                        final int i_bus_slots = bus.getBus_slots();
                                        final int i_current_bus_slots = v.size();

                                        if (i_bus_slots - i_current_bus_slots != 0)
                                        {
                                            _busList.add(bus);
                                        }_busAdapter.notifyDataSetChanged();

                                    }

                                });

                    }

                });

    }

}