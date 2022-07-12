package com.codewithjosh.Swift2k22.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
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
    public List<TicketModel> tickets;
    FirebaseFirestore firebaseFirestore;
    DateFormat dateFormat;
    SharedPreferences.Editor editor;

    public TicketAdapter(final Context context, final List<TicketModel> tickets) {

        this.context = context;
        this.tickets = tickets;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_ticket, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final TicketModel ticket = tickets.get(position);

//        initViews
        final ConstraintLayout constraint = holder.constraint;
        final TextView tvRouteName = holder.tvRouteName;
        final TextView tvBusNumber = holder.tvBusNumber;
        final TextView tvBusTimestamp = holder.tvBusTimestamp;
        final TextView tvBusDateTimestamp = holder.tvBusDateTimestamp;

//        load
        final String routeName = ticket.getRoute_name();
        final String ticketId = ticket.getTicket_id();
        final String busId = ticket.getBus_id();
        final Date dateBusTimestamp = ticket.getBus_timestamp();
        final String busDateTimestamp = "MMMM dd, yyyy";
        final String busTimestamp = "h:mm a";
        final String futureBusTimestamp = "dd MMMM yyyy h:mm a";

        initInstances();
        initSharedPref();

        tvRouteName.setText(routeName);

        dateFormat = new SimpleDateFormat(busTimestamp);
        if (dateBusTimestamp != null) tvBusTimestamp.setText(dateFormat.format(dateBusTimestamp));

        dateFormat = new SimpleDateFormat(busDateTimestamp);
        if (dateBusTimestamp != null)
            tvBusDateTimestamp.setText(dateFormat.format(dateBusTimestamp));

        firebaseFirestore
                .collection("Buses")
                .document(busId)
                .addSnapshotListener((value, error) ->
                {

                    if (value != null) {

                        final BusModel bus = value.toObject(BusModel.class);

                        final String busNumber = bus != null
                                ? bus.getBus_number()
                                : "";

                        tvBusNumber.setText(busNumber);

                        holder.itemView.setOnClickListener(v ->
                        {

                            dateFormat = new SimpleDateFormat(futureBusTimestamp);
                            final String _futureBusTimestamp = dateFormat.format(dateBusTimestamp);
                            final int busFare = bus != null
                                    ? bus.getBus_fare()
                                    : 0;

                            editor.putString("ticket_id", ticketId);
                            editor.putString("future_bus_timestamp", _futureBusTimestamp);
                            editor.putInt("bus_fare", busFare);
                            editor.apply();
                            context.startActivity(new Intent(context, ViewTicketActivity.class));

                        });

                    }

                });

        if (position % 2 == 0)
            constraint.setBackgroundColor(context.getResources().getColor(R.color.color_blue_jeans));

    }

    private void initInstances() {

        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void initSharedPref() {

        editor = context.getSharedPreferences("user", Context.MODE_PRIVATE).edit();

    }

    @Override
    public int getItemCount() {

        return tickets.size();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ConstraintLayout constraint;
        public TextView tvRouteName;
        public TextView tvBusNumber;
        public TextView tvBusTimestamp;
        public TextView tvBusDateTimestamp;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            constraint = itemView.findViewById(R.id.constraint);
            tvRouteName = itemView.findViewById(R.id.tv_route_name);
            tvBusNumber = itemView.findViewById(R.id.tv_bus_number);
            tvBusTimestamp = itemView.findViewById(R.id.tv_bus_timestamp);
            tvBusDateTimestamp = itemView.findViewById(R.id.tv_bus_date_timestamp);

        }

    }

}
