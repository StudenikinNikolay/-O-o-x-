package edu.diploma.model;

import java.util.ArrayList;
import java.util.List;

public class LoginErrors {

    private final List<String> email = new ArrayList<>();
    private final List<String> password = new ArrayList<>();

    public LoginErrors addEmailMsg(String msg) {
        this.email.add(msg);
        return this;
    }

    public LoginErrors addPasswordMsg(String msg) {
        this.password.add(msg);
        return this;
    }

    public List<String> getEmail() {
        return email;
    }

    public List<String> getPassword() {
        return password;
    }
}
