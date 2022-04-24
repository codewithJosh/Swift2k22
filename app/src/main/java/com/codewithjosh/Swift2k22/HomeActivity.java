package com.codewithjosh.Swift2k22;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codewithjosh.Swift2k22.adapters.RouteAdapter;
import com.codewithjosh.Swift2k22.models.BusModel;
import com.codewithjosh.Swift2k22.models.RouteModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    Button _onViewScheduleProof;
    RecyclerView _recyclerRoutes;
    String bus_fare, bus_id, ticket_id;
    FirebaseFirestore firebaseFirestore;
    private RouteAdapter _routeAdapter;
    private List<RouteModel> _routeList;
    private List<BusModel> _busList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        _onViewScheduleProof = findViewById(R.id.on_view_schedule_proof);
        _recyclerRoutes = findViewById(R.id.recycler_routes);

        bus_fare = getIntent().getStringExtra("bus_fare");
        bus_id = getIntent().getStringExtra("bus_id");
        ticket_id = getIntent().getStringExtra("ticket_id");

        firebaseFirestore = FirebaseFirestore.getInstance();

        _recyclerRoutes.setHasFixedSize(true);
        LinearLayoutManager _linearLayoutManager = new LinearLayoutManager(this);
        _linearLayoutManager.setReverseLayout(true);
        _linearLayoutManager.setStackFromEnd(true);
        _recyclerRoutes.setLayoutManager(_linearLayoutManager);
        _routeList = new ArrayList<>();
        _busList = new ArrayList<>();
        _routeAdapter = new RouteAdapter(this, _routeList, _busList);
        _recyclerRoutes.setAdapter(_routeAdapter);

        _readRoutes();

        _onViewScheduleProof.setOnClickListener(v -> {

            if (ticket_id != null) {

                Intent i = new Intent(this, TicketActivity.class);
                i.putExtra("bus_fare", bus_fare);
                i.putExtra("bus_id", bus_id);
                i.putExtra("ticket_id", ticket_id);
                startActivity(i);
                finish();
            }
        });

    }

    private void _readRoutes() {

        firebaseFirestore
                .collection("Routes")
                .get()
                .addOnCompleteListener(task -> {

                    _routeList.clear();
                    for (QueryDocumentSnapshot snapshot : task.getResult()) {

                        RouteModel _routeModel = new RouteModel(
                                snapshot.getString("route_id"),
                                snapshot.getString("route_name")
                        );
                        _routeList.add(_routeModel);
                    }
                    _routeAdapter.notifyDataSetChanged();
                });

    }

}