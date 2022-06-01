package com.codewithjosh.Swift2k22.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codewithjosh.Swift2k22.R;
import com.codewithjosh.Swift2k22.ViewTicketActivity;
import com.codewithjosh.Swift2k22.models.BusModel;
import com.codewithjosh.Swift2k22.models.TicketModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.ViewHolder> {
    public Context context;
    public List<TicketModel> ticketList;
    FirebaseFirestore firebaseFirestore;
    DateFormat dateFormat;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    public TicketAdapter(Context context, List<TicketModel> ticketList) {

        this.context = context;
        this.ticketList = ticketList;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.item_ticket, parent, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final TicketModel ticket = ticketList.get(position);

//        initViews
        final TextView tv_route_name = holder.tv_route_name;
        final TextView tv_bus_number = holder.tv_bus_number;
        final TextView tv_bus_timestamp = holder.tv_bus_timestamp;
        final TextView tv_bus_date_timestamp = holder.tv_bus_date_timestamp;

//        load
        final String s_route_name = ticket.getRoute_name();
        final String s_ticket_id = ticket.getTicket_id();
        final String s_bus_id = ticket.getBus_id();
        final Date date_bus_timestamp = ticket.getBus_timestamp();
        final String s_bus_date_timestamp = "MMMM dd, yyyy";
        final String s_bus_timestamp = "h:mm a";
        final String s_future_bus_timestamp = "dd MMMM yyyy h:mm a";

        initInstances();
        initSharedPref();

        tv_route_name.setText(s_route_name);

        dateFormat = new SimpleDateFormat(s_bus_timestamp);
        if (date_bus_timestamp != null)
            tv_bus_timestamp.setText(dateFormat.format(date_bus_timestamp));

        dateFormat = new SimpleDateFormat(s_bus_date_timestamp);
        if (date_bus_timestamp != null)
            tv_bus_date_timestamp.setText(dateFormat.format(date_bus_timestamp));

        firebaseFirestore
                .collection("Buses")
                .document(s_bus_id)
                .addSnapshotListener((value, error) ->
                {

                    if (value != null) {

                        final BusModel bus = value.toObject(BusModel.class);

                        final String s_bus_number = bus != null ? bus.getBus_number() : "";

                        tv_bus_number.setText(s_bus_number);

                        holder.itemView.setOnClickListener(v ->
                        {

                            dateFormat = new SimpleDateFormat(s_future_bus_timestamp);
                            final String _s_future_bus_timestamp = dateFormat.format(date_bus_timestamp);
                            final int i_bus_fare = bus != null ? bus.getBus_fare() : 0;

                            editor.putString("s_ticket_id", s_ticket_id);
                            editor.putString("s_future_bus_timestamp", _s_future_bus_timestamp);
                            editor.putInt("i_bus_fare", i_bus_fare);
                            editor.apply();

                            context.startActivity(new Intent(context, ViewTicketActivity.class));

                        });

                    }

                });

    }

    private void initInstances() {

        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void initSharedPref() {

        sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

    }

    @Override
    public int getItemCount() {

        return ticketList.size();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_route_name;
        TextView tv_bus_number;
        TextView tv_bus_timestamp;
        TextView tv_bus_date_timestamp;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            tv_route_name = itemView.findViewById(R.id.tv_route_name);
            tv_bus_number = itemView.findViewById(R.id.tv_bus_number);
            tv_bus_timestamp = itemView.findViewById(R.id.tv_bus_timestamp);
            tv_bus_date_timestamp = itemView.findViewById(R.id.tv_bus_date_timestamp);

        }

    }

}
