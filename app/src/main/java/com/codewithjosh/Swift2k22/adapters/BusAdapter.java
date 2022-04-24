package com.codewithjosh.Swift2k22.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

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

    public Context _mContext;
    public List<BusModel> _busList;

    public BusAdapter(Context _mContext, List<BusModel> _busList, String route_name) {
        this._mContext = _mContext;
        this._busList = _busList;
        this.route_name = route_name;
    }

    public String route_name;

    FirebaseFirestore firebaseFirestore;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View _view = LayoutInflater.from(_mContext).inflate(R.layout.item_bus, parent, false);
        return new ViewHolder(_view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final BusModel _bus = _busList.get(position);

        DateFormat formatter = new SimpleDateFormat("h:mm a");
        final String bus_timestamp = formatter.format(_bus.getBus_timestamp());

        holder._busNumber.setText("BUS NO. " + _bus.getBus_number());
        holder._busFare.setText("PHP " + _bus.getBus_fare() + ".00");
        holder._busTimestamp.setText(bus_timestamp);
        holder._busStatus.setText("STATUS: " + _getTimeAgo(_bus.getBus_timestamp()));

        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore
                .collection("Tickets")
                .whereEqualTo("bus_id", _bus.getBus_id())
                .get()
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful()){

                        if(task.getResult() != null) {

                            final int _availableSlots = Integer.parseInt(_bus.getBus_slots()) - task.getResult().size();

                            if (_getTimeAgo(_bus.getBus_timestamp()).equals("AT THE STATION")
                                    || _getTimeAgo(_bus.getBus_timestamp()).equals("INBOUND"))
                                holder._busSlots.setText(_availableSlots + " slots left");
                        }
                    }
                });

        if( position % 2 == 0){
            holder._theMainBox.setBackgroundColor(_mContext.getResources().getColor(R.color.colorBlueJeans));
            holder._onBookSchedule.setBackgroundColor(_mContext.getResources().getColor(R.color.colorBlueJeans));
        }

        holder.setListener(position, holder._theHiddenBox, holder._theMainBox);

    }

    private static Date _currentDate() {

        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();

    }

    public static String _getTimeAgo(Date date) {

        long time = date.getTime();
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = _currentDate().getTime();
        if (time > now || time <= 0) {
            return "AT THE STATION";
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "INBOUND";
        }
        else if (diff < 2 * MINUTE_MILLIS || diff < 60 * MINUTE_MILLIS) {
            return "DEPARTED";
        }
        else {
            return "ARRIVED";
        }

    }

    @Override
    public int getItemCount() {
        return _busList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView _busNumber, _busSlots, _busTimestamp, _busFare, _busStatus, _theSubTitle;
        public ConstraintLayout _theMainBox, _theHiddenBox;
        public Button _onBookSchedule;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            _busNumber = itemView.findViewById(R.id.bus_number);
            _busSlots = itemView.findViewById(R.id.bus_slots);
            _busTimestamp = itemView.findViewById(R.id.bus_timestamp);
            _busFare = itemView.findViewById(R.id.bus_fare);
            _busStatus = itemView.findViewById(R.id.bus_status);
            _onBookSchedule = itemView.findViewById(R.id.on_book_schedule);

            _theSubTitle = itemView.findViewById(R.id.subtitle_destination);
            _theMainBox = itemView.findViewById(R.id.main_box);
            _theHiddenBox = itemView.findViewById(R.id.hidden_box);

        }

        public void setListener(int position, ConstraintLayout constraintLayout, ConstraintLayout clickable) {
            clickable.setOnClickListener(view -> {
                if (constraintLayout.getVisibility() == View.VISIBLE) constraintLayout.setVisibility(View.GONE);
                else constraintLayout.setVisibility(View.VISIBLE);
            });
        }

    }

}
