package com.codewithjosh.Swift2k22;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codewithjosh.Swift2k22.adapters.TicketAdapter;
import com.codewithjosh.Swift2k22.models.TicketModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TicketActivity extends AppCompatActivity {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    RecyclerView recycler_ticket;
    String s_user_id;
    FirebaseFirestore firebaseFirestore;
    SharedPreferences sharedPref;
    private TicketAdapter ticketAdapter;
    private List<TicketModel> ticketList;

    private static Date currentDate() {

        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);

        initViews();
        initInstances();
        initSharedPref();
        load();
        loadTickets();

    }

    private void initSharedPref() {

        sharedPref = getSharedPreferences("user", Context.MODE_PRIVATE);

    }

    private void load() {

        s_user_id = sharedPref.getString("s_user_id", String.valueOf(Context.MODE_PRIVATE));

    }

    private void initViews() {

        recycler_ticket = findViewById(R.id.recycler_ticket);

        recycler_ticket.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recycler_ticket.setLayoutManager(linearLayoutManager);
        ticketList = new ArrayList<>();
        ticketAdapter = new TicketAdapter(this, ticketList);
        recycler_ticket.setAdapter(ticketAdapter);

    }

    private void initInstances() {

        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void loadTickets() {

        firebaseFirestore
                .collection("Tickets")
                .whereEqualTo("user_id", s_user_id)
                .addSnapshotListener((value, error) ->
                {

                    if (value != null) {

                        if (!isConnected())
                            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

                        else onLoadTickets(value);

                    }

                });

    }

    private boolean isConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void onLoadTickets(final QuerySnapshot value) {

        ticketList.clear();
        for (QueryDocumentSnapshot snapshot : value) {

            final TicketModel ticket = snapshot.toObject(TicketModel.class);
            final Date date_bus_timestamp = ticket.getBus_timestamp();

            long time = date_bus_timestamp != null ? date_bus_timestamp.getTime() : 0;
            if (time < 1000000000000L) time *= 1000;

            long now = currentDate().getTime();

            final long diff = now - time;

            if (diff < 30 * MINUTE_MILLIS) {

                ticketList.add(ticket);
                Collections.sort(ticketList, TicketModel.comparator);

            }

        }
        ticketAdapter.notifyDataSetChanged();

    }

}