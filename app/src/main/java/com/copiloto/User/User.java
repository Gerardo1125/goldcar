package com.copiloto.User;

public class User {
    private String token;
    private Integer user_id;
    private String user_name;
    private String user_first_name;
    private String user_last_name;
    private String user_email;
    private String user_password;
    private Boolean user_is_staff;
    private Boolean user_is_super;

    public User(String token, Integer user_id, String user_name, String user_first_name, String user_last_name, String user_email, Boolean user_is_staff, Boolean user_is_super) {
        this.token = token;
        this.user_id = user_id;
        this.user_name = user_name;
        this.user_first_name = user_first_name;
        this.user_last_name = user_last_name;
        this.user_email = user_email;
        this.user_is_staff = user_is_staff;
        this.user_is_super = user_is_super;
    }

    public User(String token, Integer user_id, String password){
        this.token = token;
        this.user_id  = user_id;
        this.user_password = password;
    }

    public String getToken() {
        return token;
    }
    public String getPassword() {
        return user_password;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_first_name() {
        return user_first_name;
    }

    public void setUser_first_name(String user_first_name) {
        this.user_first_name = user_first_name;
    }

    public String getUser_last_name() {
        return user_last_name;
    }

    public void setUser_last_name(String user_last_name) {
        this.user_last_name = user_last_name;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public Boolean getUser_is_staff() {
        return user_is_staff;
    }

    public void setUser_is_staff(Boolean user_is_staff) {
        this.user_is_staff = user_is_staff;
    }

    public Boolean getUser_is_super() {
        return user_is_super;
    }

    public void setUser_is_super(Boolean user_is_super) {
        this.user_is_super = user_is_super;
    }
}
