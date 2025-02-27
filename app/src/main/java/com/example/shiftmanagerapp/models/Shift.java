package com.example.shiftmanagerapp.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Shift {
    private String date;
    private String shift_type;
    private int waiters;
    private int barmen;
    private int admins;
    private int cooks;
    private int status;
    private List<String> assigned_employees;

    public Shift() {
        this.assigned_employees = new ArrayList<>();
    }

    public Shift(String date, String shift_type, int waiters, int barmen, int admins, int cooks, int status) {
        this.date = date;
        this.shift_type = shift_type;
        this.waiters = waiters;
        this.barmen = barmen;
        this.admins = admins;
        this.cooks = cooks;
        this.status = status;
        this.assigned_employees = new ArrayList<>();
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getShift_type() { return shift_type; }
    public void setShift_type(String shift_type) { this.shift_type = shift_type; }
    public int getWaiters() { return waiters; }
    public void setWaiters(int waiters) { this.waiters = waiters; }
    public int getBarmen() { return barmen; }
    public void setBarmen(int barmen) { this.barmen = barmen; }
    public int getAdmins() { return admins; }
    public void setAdmins(int admins) { this.admins = admins; }
    public int getCooks() { return cooks; }
    public void setCooks(int cooks) { this.cooks = cooks; }
    public int getStatus() { return status; }
    public List<String> getAssigned_employees() { return assigned_employees; }
    public void setAssigned_employees(List<String> assigned_employees) {
        this.assigned_employees = assigned_employees != null ? assigned_employees : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shift shift = (Shift) o;
        return Objects.equals(date, shift.date) && Objects.equals(shift_type, shift.shift_type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, shift_type);
    }
}