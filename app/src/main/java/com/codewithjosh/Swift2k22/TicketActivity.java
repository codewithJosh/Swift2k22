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

    private static final int secondMillis = 1000;
    private static final int minuteMillis = 60 * secondMillis;
    RecyclerView recyclerTicket;
    String userId;
    FirebaseFirestore firebaseFirestore;
    SharedPreferences sharedPref;
    private TicketAdapter ticketAdapter;
    private List<TicketModel> tickets;

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

    private void initViews() {

        recyclerTicket = findViewById(R.id.recycler_ticket);

        initRecyclerView();

        tickets = new ArrayList<>();
        ticketAdapter = new TicketAdapter(this, tickets);
        recyclerTicket.setAdapter(ticketAdapter);

    }

    private void initRecyclerView()
    {

        recyclerTicket.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerTicket.setLayoutManager(linearLayoutManager);

    }

    private void initInstances() {

        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void initSharedPref() {

        sharedPref = getSharedPreferences("user", Context.MODE_PRIVATE);

    }

    private void load() {

        userId = sharedPref.getString("user_id", String.valueOf(Context.MODE_PRIVATE));

    }

    private void loadTickets() {

        firebaseFirestore
                .collection("Tickets")
                .whereEqualTo("user_id", userId)
                .addSnapshotListener((value, error) ->
                {

                    if (value != null)
                    {

                        if (isConnected()) onLoadTickets(value);

                        else Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();

                    }

                });

    }

    private boolean isConnected() {

        final ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    private void onLoadTickets(final QuerySnapshot value) {

        tickets.clear();
        for (QueryDocumentSnapshot snapshot : value) {

            final TicketModel ticket = snapshot.toObject(TicketModel.class);
            final Date date = ticket.getBus_timestamp();

            long time = date != null
                    ? date.getTime()
                    : 0;

            if (time < 1000000000000L) time *= 1000;

            long now = currentDate().getTime();

            final long diff = now - time;

            if (diff < 30 * minuteMillis)
            {

                tickets.add(ticket);
                Collections.sort(tickets, TicketModel.comparator);

            }

        }
        ticketAdapter.notifyDataSetChanged();

    }

    private static Date currentDate() {

        final Calendar calendar = Calendar.getInstance();
        return calendar.getTime();

    }

}