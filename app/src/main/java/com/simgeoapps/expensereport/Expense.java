package com.simgeoapps.expensereport;

import java.text.NumberFormat;

/**
 * Expense model.
 * Created by Simeon on 10/5/2014.
 */
public class Expense {
    // fields corresponding to the expense table columns
    private int id;
    private String username;
    private String categoryName;
    private float cost;
    private String description;
    private String day;
    private String month;
    private String year;

    // field getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String username) {
        this.username = username;
    }

    public String getCategory() {
        return categoryName;
    }

    public void setCategoryId(String categoryName) {
        this.categoryName = categoryName;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return NumberFormat.getCurrencyInstance().format(cost) + " " + description;
    }

}
