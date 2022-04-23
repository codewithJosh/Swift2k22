package com.codewithjosh.Swift2k22.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.codewithjosh.Swift2k22.models.BusModel;
import com.codewithjosh.Swift2k22.models.RouteModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {

    public Context _mContext;
    public List<RouteModel> _routeList;
    public List<BusModel> _busList;
    FirebaseFirestore firebaseFirestore;
    DateFormat formatter;

    public RouteAdapter(Context _mContext, List<RouteModel> _routeList, List<BusModel> _busList) {
        this._mContext = _mContext;
        this._routeList = _routeList;
        this._busList = _busList;
    }

    private static Date _currentDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View _view = LayoutInflater.from(_mContext).inflate(R.layout.item_route, parent, false);
        return new ViewHolder(_view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final RouteModel _route = _routeList.get(position);
        final String route_name = _route.getRoute_name();

        holder._routeName.setText(route_name);

        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore
                .collection("Buses")
                .whereEqualTo("route_id", _route.getRoute_id())
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                            final Date _date = snapshot.getDate("bus_timestamp");

                            long _time = _date != null ? _date.getTime() : 0;
                            if (_time < 1000000000000L) _time *= 1000;

                            long now = _currentDate().getTime();
                            if (_time > now || _time <= 0) {

                                _busList.clear();
                                for (QueryDocumentSnapshot _snapshot : task.getResult()) {

                                    BusModel _busModel = new BusModel(
                                            _snapshot.getString("route_id"),
                                            _snapshot.getString("bus_id"),
                                            _snapshot.getString("bus_number"),
                                            _snapshot.getString("bus_slots"),
                                            _snapshot.getDate("bus_timestamp"),
                                            _snapshot.getString("bus_fare")
                                    );
                                    _busList.add(_busModel);
                                }
                                Collections.sort(_busList, BusModel._comparator);

                                formatter = new SimpleDateFormat("MMMM dd, yyyy");
                                if (_date != null)
                                    holder._busTimestamp.setText(formatter.format(_date));
                                else holder._busFare.setText("UNAVAILABLE");

                                formatter = new SimpleDateFormat("h:mm a");
                                if (_date != null)
                                    holder._busDateTimestamp.setText(formatter.format(_date));
                                else holder._busFare.setText("UNAVAILABLE");

                                final String bus_fare = snapshot.getString("bus_fare");
                                if (bus_fare != null)
                                    holder._busFare.setText("PHP " + bus_fare + ".00");
                                else holder._busFare.setText("UNAVAILABLE");

                                holder.setListener(position, holder._theHiddenBox, holder._theMainBox);

                                holder._onBookSchedule.setOnClickListener(v -> {

                                    Intent i = new Intent(_mContext, PaymentActivity.class);
                                    i.putExtra("bus_id", snapshot.getString("bus_id"));
                                    i.putExtra("route_name", route_name);
                                    i.putExtra("bus_number", snapshot.getString("bus_number"));
                                    i.putExtra("bus_fare", bus_fare);

                                    firebaseFirestore
                                            .collection("Tickets")
                                            .whereEqualTo("bus_id", snapshot.getString("bus_id"))
                                            .addSnapshotListener((_v, e) -> {
                                                if (_v != null)
                                                    if (Integer.parseInt(snapshot.getString("bus_slots")) - _v.size() != 0)
                                                        _mContext.startActivity(i);
                                                    else
                                                        Toast.makeText(_mContext, "Reservation is already full!", Toast.LENGTH_SHORT).show();
                                            });
                                });
                            }
                        }
                    }
                });

        if (position % 2 == 0) {
            holder._theMainBox.setBackgroundColor(_mContext.getResources().getColor(R.color.colorBlueJeans));
            holder._onBookSchedule.setBackgroundColor(_mContext.getResources().getColor(R.color.colorBlueJeans));
        }

        holder._onViewMoreSchedule.setOnClickListener(v -> {
            Intent i = new Intent(_mContext, ReservationActivity.class);
            i.putExtra("route_id", _route.getRoute_id());
            i.putExtra("route_name", route_name);
            _mContext.startActivity(i);
        });

    }

    @Override
    public int getItemCount() {
        return _routeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView _routeName, _busDateTimestamp, _busTimestamp, _theSubTitle, _busFare;
        public ConstraintLayout _theMainBox, _theHiddenBox;
        public Button _onBookSchedule, _onViewMoreSchedule;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            _routeName = itemView.findViewById(R.id.route_name);
            _busDateTimestamp = itemView.findViewById(R.id.bus_date_timestamp);
            _busTimestamp = itemView.findViewById(R.id.bus_timestamp);
            _busFare = itemView.findViewById(R.id.bus_fare);
            _onBookSchedule = itemView.findViewById(R.id.on_book_schedule);
            _onViewMoreSchedule = itemView.findViewById(R.id.on_view_more_schedule);

            _theSubTitle = itemView.findViewById(R.id.subtitle_destination);
            _theMainBox = itemView.findViewById(R.id.main_box);
            _theHiddenBox = itemView.findViewById(R.id.hidden_box);

        }

        public void setListener(int position, ConstraintLayout constraintLayout, ConstraintLayout clickable) {
            clickable.setOnClickListener(view -> {
                if (constraintLayout.getVisibility() == View.VISIBLE)
                    constraintLayout.setVisibility(View.GONE);
                else constraintLayout.setVisibility(View.VISIBLE);
            });
        }

    }

}
