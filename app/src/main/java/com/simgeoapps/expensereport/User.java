package com.simgeoapps.expensereport;

import java.io.Serializable;

/**
 * User model.
 */
public class User implements Serializable{
    // data fields; these match the columns in DB
    private int id;
    private String name;

    // getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
