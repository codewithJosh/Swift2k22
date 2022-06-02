package com.codewithjosh.Swift2k22.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.codewithjosh.Swift2k22.PaymentActivity;
import com.codewithjosh.Swift2k22.R;
import com.codewithjosh.Swift2k22.ReservationActivity;
import com.codewithjosh.Swift2k22.ViewTicketActivity;
import com.codewithjosh.Swift2k22.models.BusModel;
import com.codewithjosh.Swift2k22.models.RouteModel;
import com.codewithjosh.Swift2k22.models.TicketModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {

    public Context context;
    public List<BusModel> busList;
    public List<RouteModel> routeList;
    String s_user_id;
    FirebaseFirestore firebaseFirestore;
    DateFormat dateFormat;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    public RouteAdapter(Context context, List<RouteModel> routeList, List<BusModel> busList) {

        this.context = context;
        this.routeList = routeList;
        this.busList = busList;

    }

    private static Date currentDate() {

        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.item_route, parent, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final RouteModel route = routeList.get(position);

//        initViews
        final Button nav_payment = holder.nav_payment;
        final Button nav_reservation = holder.nav_reservation;
        final ConstraintLayout constraint_main_box = holder.constraint_main_box;
        final ConstraintLayout constraint_hidden_box = holder.constraint_hidden_box;
        final TextView tv_route_name = holder.tv_route_name;
        final TextView tv_bus_timestamp = holder.tv_bus_timestamp;
        final TextView tv_bus_date_timestamp = holder.tv_bus_date_timestamp;
        final TextView tv_bus_fare = holder.tv_bus_fare;

//        load
        final String s_route_id = route.getRoute_id();
        final String s_route_name = route.getRoute_name();

        initInstances();
        initSharedPref();
        load();

        tv_route_name.setText(s_route_name);

        firebaseFirestore
                .collection("Buses")
                .whereEqualTo("route_id", s_route_id)
                .addSnapshotListener((value, error) ->
                {

                    if (value != null) {

                        busList.clear();
                        for (QueryDocumentSnapshot snapshot : value) {

                            final BusModel bus = snapshot.toObject(BusModel.class);
                            final Date date = bus.getBus_timestamp();

                            long _time = date != null ? date.getTime() : 0;
                            if (_time < 1000000000000L) _time *= 1000;

                            long now = currentDate().getTime();
                            if (_time > now || _time <= 0) {

                                busList.add(bus);
                                Collections.sort(busList, BusModel.comparator);

                            }

                        }

                        if (busList.size() != 0) {

                            final BusModel bus = busList.get(0);

                            final Date date_bus_timestamp = bus.getBus_timestamp();
                            final int i_bus_fare = bus.getBus_fare();
                            final String s_bus_fare = "PHP " + i_bus_fare + ".00";
                            final String s_bus_date_timestamp = "MMMM dd, yyyy";
                            final String s_bus_timestamp = "h:mm a";
                            final String s_future_bus_timestamp = "dd MMMM yyyy h:mm a";

                            dateFormat = new SimpleDateFormat(s_bus_date_timestamp);
                            if (date_bus_timestamp != null) {

                                tv_bus_date_timestamp.setText(dateFormat.format(date_bus_timestamp));
                                tv_bus_date_timestamp.setTag("");

                            }

                            dateFormat = new SimpleDateFormat(s_bus_timestamp);
                            if (date_bus_timestamp != null)
                                tv_bus_timestamp.setText(dateFormat.format(date_bus_timestamp));

                            tv_bus_fare.setText(s_bus_fare);

                            constraint_main_box.setOnClickListener(v ->
                            {

                                if (tv_bus_date_timestamp.getTag().equals("")) {

                                    if (constraint_hidden_box.getVisibility() == View.GONE)
                                        constraint_hidden_box.setVisibility(View.VISIBLE);

                                    else constraint_hidden_box.setVisibility(View.GONE);

                                }

                            });

                            nav_payment.setOnClickListener(v ->
                            {

                                final String s_bus_id = bus.getBus_id();

                                if (isConnected()) {

                                    firebaseFirestore
                                            .collection("Tickets")
                                            .whereEqualTo("user_id", s_user_id)
                                            .whereEqualTo("bus_id", s_bus_id)
                                            .get()
                                            .addOnSuccessListener(queryDocumentSnapshots ->
                                            {

                                                if (queryDocumentSnapshots != null) {

                                                    if (queryDocumentSnapshots.isEmpty()) {

                                                        final int i_bus_slots = bus.getBus_slots();
                                                        final int i_current_bus_slots = queryDocumentSnapshots.size();

                                                        if (i_bus_slots - i_current_bus_slots != 0) {

                                                            final String s_bus_number = "BUS NO. " + bus.getBus_number();
                                                            dateFormat = new SimpleDateFormat(s_future_bus_timestamp);
                                                            final String _s_future_bus_timestamp = dateFormat.format(date_bus_timestamp);

                                                            editor.putString("s_bus_id", s_bus_id);
                                                            editor.putString("s_route_name", s_route_name);
                                                            editor.putString("s_bus_number", s_bus_number);
                                                            editor.putString("s_future_bus_timestamp", _s_future_bus_timestamp);
                                                            editor.putInt("i_bus_fare", i_bus_fare);
                                                            editor.apply();

                                                            context.startActivity(new Intent(context, PaymentActivity.class));

                                                        } else
                                                            Toast.makeText(context, "Reservation is already full!", Toast.LENGTH_SHORT).show();

                                                    }
                                                    else
                                                    {

                                                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots)
                                                        {
                                                            final TicketModel ticket = snapshot.toObject(TicketModel.class);
                                                            final String s_ticket_id = ticket.getTicket_id();

                                                            dateFormat = new SimpleDateFormat(s_future_bus_timestamp);
                                                            final String _s_future_bus_timestamp = dateFormat.format(date_bus_timestamp);

                                                            editor.putString("s_ticket_id", s_ticket_id);
                                                            editor.putString("s_future_bus_timestamp", _s_future_bus_timestamp);
                                                            editor.putInt("i_bus_fare", i_bus_fare);
                                                            editor.apply();

                                                            context.startActivity(new Intent(context, ViewTicketActivity.class));
                                                        }

                                                    }

                                                }

                                            });

                                } else
                                    Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_SHORT).show();

                            });

                            nav_reservation.setOnClickListener(v ->
                            {

                                if (isConnected()) {

                                    editor.putString("s_route_id", s_route_id);
                                    editor.putString("s_route_name", s_route_name);
                                    editor.putString("s_bus_date_timestamp", tv_bus_date_timestamp.getText().toString());
                                    editor.apply();

                                    context.startActivity(new Intent(context, ReservationActivity.class));

                                } else
                                    Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_SHORT).show();

                            });

                        } else {

                            tv_bus_date_timestamp.setText("N/A");
                            tv_bus_date_timestamp.setTag("unavailable");
                            tv_bus_timestamp.setText("N/A");
                            if (constraint_hidden_box.getVisibility() == View.VISIBLE)
                                constraint_hidden_box.setVisibility(View.GONE);

                        }

                    }

                });

        if (position % 2 == 0)
            constraint_main_box.setBackgroundColor(context.getResources().getColor(R.color.colorBlueJeans));

    }

    private void load() {

        s_user_id = sharedPref.getString("s_user_id", String.valueOf(Context.MODE_PRIVATE));

    }

    private void initInstances() {

        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void initSharedPref() {

        sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

    }

    private boolean isConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    @Override
    public int getItemCount() {

        return routeList.size();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        Button nav_payment;
        Button nav_reservation;
        ConstraintLayout constraint_main_box;
        ConstraintLayout constraint_hidden_box;
        TextView tv_route_name;
        TextView tv_bus_timestamp;
        TextView tv_bus_date_timestamp;
        TextView tv_bus_fare;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            nav_payment = itemView.findViewById(R.id.nav_payment);
            nav_reservation = itemView.findViewById(R.id.nav_reservation);
            constraint_main_box = itemView.findViewById(R.id.constraint_main_box);
            constraint_hidden_box = itemView.findViewById(R.id.constraint_hidden_box);
            tv_route_name = itemView.findViewById(R.id.tv_route_name);
            tv_bus_timestamp = itemView.findViewById(R.id.tv_bus_timestamp);
            tv_bus_date_timestamp = itemView.findViewById(R.id.tv_bus_date_timestamp);
            tv_bus_fare = itemView.findViewById(R.id.tv_bus_fare);

        }

    }

}
