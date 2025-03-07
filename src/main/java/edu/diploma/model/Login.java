package edu.diploma.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Login {

    @JsonProperty("auth-token")
    private final String token;

    public Login(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Login login = (Login) o;
        return Objects.equals(token, login.token);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(token);
    }
}
