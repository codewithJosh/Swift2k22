package com.codewithjosh.Swift2k22.models;

public class UserModel {

    private int user_balance;
    private String user_email;
    private String user_id;
    private String user_name;

    public UserModel() {

    }

    public UserModel(final int user_balance, final String user_email, final String user_id, final String user_name) {

        this.user_balance = user_balance;
        this.user_email = user_email;
        this.user_id = user_id;
        this.user_name = user_name;

    }

    public int getUser_balance() {

        return user_balance;

    }

    public String getUser_email() {

        return user_email;

    }

    public String getUser_id() {

        return user_id;

    }

    public String getUser_name() {

        return user_name;

    }

}
