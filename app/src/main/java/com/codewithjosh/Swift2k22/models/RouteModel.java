package com.codewithjosh.Swift2k22.models;

public class RouteModel {

    private final String route_id;
    private final String route_name;

    public RouteModel(String route_id, String route_name) {
        this.route_id = route_id;
        this.route_name = route_name;
    }

    public String getRoute_id() {
        return route_id;
    }

    public String getRoute_name() {
        return route_name;
    }

}
