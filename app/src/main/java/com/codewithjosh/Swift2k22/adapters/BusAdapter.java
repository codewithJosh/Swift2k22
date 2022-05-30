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
import com.codewithjosh.Swift2k22.models.BusModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BusAdapter extends RecyclerView.Adapter<BusAdapter.ViewHolder> {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;

    public Context context;
    public List<BusModel> busList;
    String s_user_id;
    String s_route_name;
    FirebaseFirestore firebaseFirestore;
    DateFormat dateFormat;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    public BusAdapter(Context context, List<BusModel> busList) {

        this.context = context;
        this.busList = busList;

    }

    public static String getTimeAgo(final Date date) {

        long time = date.getTime();
        if (time < 1000000000000L) time *= 1000;

        final long now = currentDate().getTime();
        if (time > now || time <= 0) return "AT THE STATION";

        final long diff = now - time;

        if (diff < MINUTE_MILLIS) return "INBOUND";

        else return "DEPARTED";

    }

    private static Date currentDate() {

        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.item_bus, parent, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final BusModel bus = busList.get(position);

//        initViews
        final Button nav_payment = holder.nav_payment;
        final ConstraintLayout constraint_main_box = holder.constraint_main_box;
        final ConstraintLayout constraint_hidden_box = holder.constraint_hidden_box;
        final TextView tv_bus_number = holder.tv_bus_number;
        final TextView tv_bus_slots = holder.tv_bus_slots;
        final TextView tv_bus_timestamp = holder.tv_bus_timestamp;
        final TextView tv_bus_fare = holder.tv_bus_fare;
        final TextView tv_bus_status = holder.tv_bus_status;

//        load
        final String s_bus_id = bus.getBus_id();
        final int i_bus_fare = bus.getBus_fare();
        final String s_bus_fare = "PHP " + i_bus_fare + ".00";
        final String s_bus_number = "BUS NO. " + bus.getBus_number();
        final int i_bus_slots = bus.getBus_slots();
        final Date date_bus_timestamp = bus.getBus_timestamp();
        final String s_bus_timestamp = "h:mm a";
        final String s_future_bus_timestamp = "dd MMMM yyyy h:mm a";
        final String s_bus_status = "STATUS: " + getTimeAgo(date_bus_timestamp);

        dateFormat = new SimpleDateFormat(s_bus_timestamp);

        tv_bus_fare.setText(s_bus_fare);
        tv_bus_number.setText(s_bus_number);
        tv_bus_timestamp.setText(dateFormat.format(date_bus_timestamp));
        tv_bus_status.setText(s_bus_status);

        initInstances();
        initSharedPref();
        load();

        firebaseFirestore
                .collection("Tickets")
                .whereEqualTo("bus_id", s_bus_id)
                .addSnapshotListener((value, error) ->
                {

                    if (value != null) {

                        final int i_current_bus_slots = value.size();
                        final int i_available_bus_slots = i_bus_slots - i_current_bus_slots;

                        if (i_available_bus_slots != 0) {

                            final String s_bus_slots = i_available_bus_slots + " slots left";

                            if (!getTimeAgo(date_bus_timestamp).equals("DEPARTED"))
                                tv_bus_slots.setText(s_bus_slots);

                        } else {

                            tv_bus_slots.setText("");
                            if (constraint_hidden_box.getVisibility() == View.VISIBLE)
                                constraint_hidden_box.setVisibility(View.GONE);

                        }

                    }

                });

        constraint_main_box.setOnClickListener(v ->
        {

            if (!getTimeAgo(date_bus_timestamp).equals("DEPARTED") && !tv_bus_slots.getText().toString().equals("")) {

                if (constraint_hidden_box.getVisibility() == View.GONE)
                    constraint_hidden_box.setVisibility(View.VISIBLE);

                else constraint_hidden_box.setVisibility(View.GONE);

            }

        });

        nav_payment.setOnClickListener(v ->
        {

            if (isConnected()) {

                firebaseFirestore
                        .collection("Tickets")
                        .whereEqualTo("user_id", s_user_id)
                        .whereEqualTo("bus_id", s_bus_id)
                        .addSnapshotListener((value, error) ->
                        {

                            if (value != null) {

                                if (value.isEmpty()) {

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
                                    Toast.makeText(context, "Reservation already booked!", Toast.LENGTH_SHORT).show();

                            }

                        });

            } else Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_SHORT).show();

        });

        if (position % 2 == 0)
            constraint_main_box.setBackgroundColor(context.getResources().getColor(R.color.colorBlueJeans));

    }

    private void initInstances() {

        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void initSharedPref() {

        sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

    }

    private void load() {

        s_user_id = sharedPref.getString("s_user_id", String.valueOf(Context.MODE_PRIVATE));
        s_route_name = sharedPref.getString("s_route_name", String.valueOf(Context.MODE_PRIVATE));

    }

    private boolean isConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

    @Override
    public int getItemCount() {

        return busList.size();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        Button nav_payment;
        ConstraintLayout constraint_main_box;
        ConstraintLayout constraint_hidden_box;
        TextView tv_bus_number;
        TextView tv_bus_slots;
        TextView tv_bus_timestamp;
        TextView tv_bus_fare;
        TextView tv_bus_status;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            nav_payment = itemView.findViewById(R.id.nav_payment);
            constraint_main_box = itemView.findViewById(R.id.constraint_main_box);
            constraint_hidden_box = itemView.findViewById(R.id.constraint_hidden_box);
            tv_bus_number = itemView.findViewById(R.id.tv_bus_number);
            tv_bus_slots = itemView.findViewById(R.id.tv_bus_slots);
            tv_bus_timestamp = itemView.findViewById(R.id.tv_bus_timestamp);
            tv_bus_fare = itemView.findViewById(R.id.tv_bus_fare);
            tv_bus_status = itemView.findViewById(R.id.tv_bus_status);

        }

    }

}
