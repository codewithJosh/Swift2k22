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

                        firebaseFirestore
                                .collection("Tickets")
                                .whereEqualTo("bus_id", _snapshot.getString("bus_id"))
                                .addSnapshotListener((v, e) -> {

                                    final int _availableSlots = Integer.parseInt(_snapshot.getString("bus_slots")) - v.size();

                                    BusModel _busModel = new BusModel(
                                            _snapshot.getString("route_id"),
                                            _snapshot.getString("bus_id"),
                                            _snapshot.getString("bus_number"),
                                            _snapshot.getString("bus_slots"),
                                            _snapshot.getDate("bus_timestamp"),
                                            _snapshot.getString("bus_fare")
                                    );

                                    if (_availableSlots != 0) _busList.add(_busModel);
                                    _busAdapter.notifyDataSetChanged();
                                });
                    }
                });

    }
}