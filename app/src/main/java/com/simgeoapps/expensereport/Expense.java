package com.simgeoapps.expensereport;

import java.text.NumberFormat;

/**
 * Expense model.
 * Created by Simeon on 10/5/2014.
 */
public class Expense {
    // fields corresponding to the expense table columns
    private int id;
    private int userId;
    private int categoryId;
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userID) {
        this.userId = userID;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryID) {
        this.categoryId = categoryID;
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
