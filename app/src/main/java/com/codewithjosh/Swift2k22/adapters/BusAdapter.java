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
import com.codewithjosh.Swift2k22.ViewTicketActivity;
import com.codewithjosh.Swift2k22.models.BusModel;
import com.codewithjosh.Swift2k22.models.TicketModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BusAdapter extends RecyclerView.Adapter<BusAdapter.ViewHolder> {

    private static final int secondMillis = 1000;
    private static final int minuteMillis = 60 * secondMillis;
    public Context context;
    public List<BusModel> buses;
    String userId;
    String routeName;
    FirebaseFirestore firebaseFirestore;
    DateFormat dateFormat;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    public BusAdapter(final Context context, final List<BusModel> buses) {

        this.context = context;
        this.buses = buses;

    }

    public static String getTimeAgo(final Date date) {

        long time = date.getTime();
        if (time < 1000000000000L) time *= 1000;

        final long now = currentDate().getTime();
        if (time > now || time <= 0) return "AT THE STATION";

        final long diff = now - time;
        if (diff < minuteMillis) return "INBOUND";

        else return "DEPARTED";

    }

    private static Date currentDate() {

        final Calendar calendar = Calendar.getInstance();
        return calendar.getTime();

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_bus, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final BusModel bus = buses.get(position);

//        initViews
        final Button navPayment = holder.navPayment;
        final ConstraintLayout constraintMainBox = holder.constraintMainBox;
        final ConstraintLayout constraintHiddenBox = holder.constraintHiddenBox;
        final TextView tvBusNumber = holder.tvBusNumber;
        final TextView tvBusSlots = holder.tvBusSlots;
        final TextView tvBusTimestamp = holder.tvBusTimestamp;
        final TextView tvBusFare = holder.tvBusFare;
        final TextView tvBusStatus = holder.tvBusStatus;

//        load
        final String busId = bus.getBus_id();
        final int busFare = bus.getBus_fare();
        final String _busFare = "PHP " + busFare + ".00";
        final String busNumber = bus.getBus_number();
        final int busSlots = bus.getBus_slots();
        final Date dateBusTimestamp = bus.getBus_timestamp();
        final String busTimestamp = "h:mm a";
        final String futureBusTimestamp = "dd MMMM yyyy h:mm a";
        final String busStatus = "STATUS: " + getTimeAgo(dateBusTimestamp);

        dateFormat = new SimpleDateFormat(busTimestamp);

        tvBusFare.setText(_busFare);
        tvBusNumber.setText(busNumber);
        tvBusTimestamp.setText(dateFormat.format(dateBusTimestamp));
        tvBusStatus.setText(busStatus);

        initInstances();
        initSharedPref();
        load();

        firebaseFirestore
                .collection("Tickets")
                .whereEqualTo("bus_id", busId)
                .addSnapshotListener((value, error) ->
                {

                    if (value != null) {

                        final int currentBusSlots = value.size();
                        final int availableBusSlots = busSlots - currentBusSlots;

                        if (availableBusSlots != 0) {

                            final String _busSlots = availableBusSlots + " slots left";

                            if (!getTimeAgo(dateBusTimestamp).equals("DEPARTED"))
                                tvBusSlots.setText(_busSlots);

                        } else {

                            tvBusSlots.setText("");
                            if (constraintHiddenBox.getVisibility() == View.VISIBLE)
                                constraintHiddenBox.setVisibility(View.GONE);

                        }

                    }

                });

        constraintMainBox.setOnClickListener(v ->
        {

            if (!getTimeAgo(dateBusTimestamp).equals("DEPARTED") && !tvBusSlots.getText().toString().equals("")) {

                if (constraintHiddenBox.getVisibility() == View.GONE)
                    constraintHiddenBox.setVisibility(View.VISIBLE);

                else constraintHiddenBox.setVisibility(View.GONE);

            }

        });

        navPayment.setOnClickListener(v ->
        {

            dateFormat = new SimpleDateFormat(futureBusTimestamp);
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

                                    editor.putString("bus_id", busId);
                                    editor.putString("route_name", routeName);
                                    editor.putString("bus_number", busNumber);
                                    editor.putString("future_bus_timestamp", _futureBusTimestamp);
                                    editor.putInt("bus_fare", busFare);
                                    editor.apply();
                                    context.startActivity(new Intent(context, PaymentActivity.class));

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

            else Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_SHORT).show();

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
        routeName = sharedPref.getString("route_name", String.valueOf(Context.MODE_PRIVATE));

    }

    private boolean isConnected() {

        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    @Override
    public int getItemCount() {

        return buses.size();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public Button navPayment;
        public ConstraintLayout constraintMainBox;
        public ConstraintLayout constraintHiddenBox;
        public TextView tvBusNumber;
        public TextView tvBusSlots;
        public TextView tvBusTimestamp;
        public TextView tvBusFare;
        public TextView tvBusStatus;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            navPayment = itemView.findViewById(R.id.nav_payment);
            constraintMainBox = itemView.findViewById(R.id.constraint_main_box);
            constraintHiddenBox = itemView.findViewById(R.id.constraint_hidden_box);
            tvBusNumber = itemView.findViewById(R.id.tv_bus_number);
            tvBusSlots = itemView.findViewById(R.id.tv_bus_slots);
            tvBusTimestamp = itemView.findViewById(R.id.tv_bus_timestamp);
            tvBusFare = itemView.findViewById(R.id.tv_bus_fare);
            tvBusStatus = itemView.findViewById(R.id.tv_bus_status);

        }

    }

}
