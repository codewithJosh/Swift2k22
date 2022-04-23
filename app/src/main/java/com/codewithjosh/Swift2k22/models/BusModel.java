package com.codewithjosh.Swift2k22.models;

import java.util.Comparator;
import java.util.Date;

public class BusModel {

    public static Comparator<BusModel> _comparator = (_bM1, _bM2) -> _bM1.getBus_timestamp().compareTo(_bM2.getBus_timestamp());
    private final String route_id;
    private final String bus_id;
    private final String bus_number;
    private final String bus_slots;
    private final Date bus_timestamp;
    private final String bus_fare;

    public BusModel(String route_id, String bus_id, String bus_number, String bus_slots, Date bus_timestamp, String bus_fare) {
        this.route_id = route_id;
        this.bus_id = bus_id;
        this.bus_number = bus_number;
        this.bus_slots = bus_slots;
        this.bus_timestamp = bus_timestamp;
        this.bus_fare = bus_fare;
    }

    public String getRoute_id() {
        return route_id;
    }

    public String getBus_id() {
        return bus_id;
    }

    public String getBus_number() {
        return bus_number;
    }

    public String getBus_slots() {
        return bus_slots;
    }

    public Date getBus_timestamp() {
        return bus_timestamp;
    }

    public String getBus_fare() {
        return bus_fare;
    }

}
