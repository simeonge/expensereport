package com.simgeoapps.expensereport;

import java.io.Serializable;

/**
 * Category model.
 */
public class Category implements Serializable{
    // fields corresponding to the category table columns
    private int id;
    private int userId;
    private String category;

    // fields getters and setters
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getUserID() {
        return userId;
    }

    public void setUserId(int userID) {
        this.userId = userID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return category;
    }
}
