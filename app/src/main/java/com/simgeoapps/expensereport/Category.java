package com.simgeoapps.expensereport;

/**
 * Category model.
 * Created by Simeon on 10/5/2014.
 */
public class Category {
    // fields corresponding to the category table columns
    private int id;
    private String usermame;
    private String category;

    // fields getters and setters
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUserName() {
        return usermame;
    }

    public void setUserName(String username) {
        this.usermame = username;
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
