package com.example.shiftmanagerapp.models;

public class Worker {
    private String name;
    private String role;
    private String shift;

    public Worker() {
    }

    public Worker(String name, String role, String shift) {
        this.name = name;
        this.role = role;
        this.shift = shift;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }
}