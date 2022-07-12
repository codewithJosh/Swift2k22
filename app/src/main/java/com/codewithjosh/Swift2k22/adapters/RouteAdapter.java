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
    public List<BusModel> buses;
    public List<RouteModel> routes;
    String userId;
    FirebaseFirestore firebaseFirestore;
    DateFormat dateFormat;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    public RouteAdapter(final Context context, final List<RouteModel> routes, final List<BusModel> buses) {

        this.context = context;
        this.routes = routes;
        this.buses = buses;

    }

    private static Date currentDate() {

        final Calendar calendar = Calendar.getInstance();
        return calendar.getTime();

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_route, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final RouteModel route = routes.get(position);

//        initViews
        final Button navPayment = holder.navPayment;
        final Button navReservation = holder.navReservation;
        final ConstraintLayout constraintMainBox = holder.constraintMainBox;
        final ConstraintLayout constraintHiddenBox = holder.constraintHiddenBox;
        final TextView tvRouteName = holder.tvRouteName;
        final TextView tvBusTimestamp = holder.tvBusTimestamp;
        final TextView tvBusDateTimestamp = holder.tvBusDateTimestamp;
        final TextView tvBusFare = holder.tvBusFare;

//        load
        final String routeId = route.getRoute_id();
        final String routeName = route.getRoute_name();

        initInstances();
        initSharedPref();
        load();

        tvRouteName.setText(routeName);

        firebaseFirestore
                .collection("Buses")
                .whereEqualTo("route_id", routeId)
                .addSnapshotListener((value, error) ->
                {

                    if (value != null) {

                        buses.clear();
                        for (QueryDocumentSnapshot snapshot : value) {

                            final BusModel bus = snapshot.toObject(BusModel.class);
                            final Date date = bus.getBus_timestamp();

                            long _time = date != null
                                    ? date.getTime()
                                    : 0;

                            if (_time < 1000000000000L) _time *= 1000;

                            long now = currentDate().getTime();
                            if (_time > now || _time <= 0) {

                                buses.add(bus);
                                Collections.sort(buses, BusModel.comparator);

                            }

                        }

                        if (buses.size() != 0) {

                            final BusModel bus = buses.get(0);

                            final Date dateBusTimestamp = bus.getBus_timestamp();
                            final int busFare = bus.getBus_fare();
                            final String _busFare = "PHP " + busFare + ".00";
                            final String busDateTimestamp = "MMMM dd, yyyy";
                            final String busTimestamp = "h:mm a";
                            final String futureBusTimestamp = "dd MMMM yyyy h:mm a";

                            dateFormat = new SimpleDateFormat(busDateTimestamp);
                            if (dateBusTimestamp != null) {

                                tvBusDateTimestamp.setText(dateFormat.format(dateBusTimestamp));
                                tvBusDateTimestamp.setTag("");

                            }

                            dateFormat = new SimpleDateFormat(busTimestamp);
                            if (dateBusTimestamp != null)
                                tvBusTimestamp.setText(dateFormat.format(dateBusTimestamp));

                            tvBusFare.setText(_busFare);

                            constraintMainBox.setOnClickListener(v ->
                            {

                                if (tvBusDateTimestamp.getTag().equals("")) {

                                    if (constraintHiddenBox.getVisibility() == View.GONE)
                                        constraintHiddenBox.setVisibility(View.VISIBLE);

                                    else constraintHiddenBox.setVisibility(View.GONE);

                                }

                            });

                            navPayment.setOnClickListener(v ->
                            {

                                dateFormat = new SimpleDateFormat(futureBusTimestamp);
                                final String busId = bus.getBus_id();
                                final String _futureBusTimestamp = dateFormat.format(dateBusTimestamp);

                                if (isConnected())

                                    firebaseFirestore
                                            .collection("Tickets")
                                            .whereEqualTo("user_id", userId)
                                            .whereEqualTo("bus_id", busId)
                                            .get()
                                            .addOnSuccessListener(queryDocumentSnapshots ->
                                            {

                                                if (queryDocumentSnapshots != null) {

                                                    if (queryDocumentSnapshots.isEmpty()) {

                                                        final int busSlots = bus.getBus_slots();
                                                        final int currentBusSlots = queryDocumentSnapshots.size();

                                                        if (busSlots - currentBusSlots != 0) {

                                                            final String busNumber = bus.getBus_number();

                                                            editor.putString("bus_id", busId);
                                                            editor.putString("route_name", routeName);
                                                            editor.putString("bus_number", busNumber);
                                                            editor.putString("future_bus_timestamp", _futureBusTimestamp);
                                                            editor.putInt("bus_fare", busFare);
                                                            editor.apply();
                                                            context.startActivity(new Intent(context, PaymentActivity.class));

                                                        } else
                                                            Toast.makeText(context, "Reservation is already full!", Toast.LENGTH_SHORT).show();

                                                    } else

                                                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {

                                                            final TicketModel ticket = snapshot.toObject(TicketModel.class);
                                                            final String ticketId = ticket.getTicket_id();

                                                            editor.putString("ticket_id", ticketId);
                                                            editor.putString("future_bus_timestamp", _futureBusTimestamp);
                                                            editor.putInt("bus_fare", busFare);
                                                            editor.apply();
                                                            context.startActivity(new Intent(context, ViewTicketActivity.class));

                                                        }

                                                }

                                            });

                                else
                                    Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_SHORT).show();

                            });

                            navReservation.setOnClickListener(v ->
                            {

                                if (isConnected()) {

                                    final String _busDateTimestamp = tvBusDateTimestamp.getText().toString();

                                    editor.putString("route_id", routeId);
                                    editor.putString("route_name", routeName);
                                    editor.putString("bus_date_timestamp", _busDateTimestamp);
                                    editor.apply();
                                    context.startActivity(new Intent(context, ReservationActivity.class));

                                } else
                                    Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_SHORT).show();

                            });

                        } else {

                            tvBusDateTimestamp.setText("N/A");
                            tvBusDateTimestamp.setTag("unavailable");
                            tvBusTimestamp.setText("N/A");
                            if (constraintHiddenBox.getVisibility() == View.VISIBLE)
                                constraintHiddenBox.setVisibility(View.GONE);

                        }

                    }

                });

        if (position % 2 == 0)
            constraintMainBox.setBackgroundColor(context.getResources().getColor(R.color.color_blue_jeans));

    }

    private void initInstances() {

        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void initSharedPref() {

        sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

    }

    private void load() {

        userId = sharedPref.getString("user_id", String.valueOf(Context.MODE_PRIVATE));

    }

    private boolean isConnected() {

        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    @Override
    public int getItemCount() {

        return routes.size();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public Button navPayment;
        public Button navReservation;
        public ConstraintLayout constraintMainBox;
        public ConstraintLayout constraintHiddenBox;
        public TextView tvRouteName;
        public TextView tvBusTimestamp;
        public TextView tvBusDateTimestamp;
        public TextView tvBusFare;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            navPayment = itemView.findViewById(R.id.nav_payment);
            navReservation = itemView.findViewById(R.id.nav_reservation);
            constraintMainBox = itemView.findViewById(R.id.constraint_main_box);
            constraintHiddenBox = itemView.findViewById(R.id.constraint_hidden_box);
            tvRouteName = itemView.findViewById(R.id.tv_route_name);
            tvBusTimestamp = itemView.findViewById(R.id.tv_bus_timestamp);
            tvBusDateTimestamp = itemView.findViewById(R.id.tv_bus_date_timestamp);
            tvBusFare = itemView.findViewById(R.id.tv_bus_fare);

        }

    }

}
