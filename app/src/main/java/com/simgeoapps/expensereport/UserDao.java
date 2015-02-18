package com.simgeoapps.expensereport;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * User DAO. Supports adding, editing, and deleting users.
 */
public class UserDao {
    /** Database instance which will be queried. */
    private SQLiteDatabase database;

    /** Instance of the database helper class. */
    private ExpenseData dbHelper;

    // columns
    private String[] colsToReturn = { ExpenseData.USER_ID,
            ExpenseData.USER_NAME };

    // constructor creates an instance of the helper class
    public UserDao(Context context) {
        dbHelper = new ExpenseData(context);
    }

    // methods to open and close DB
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * Determines if specified user exists in the database.
     * @param user The user whose existence will be checked.
     * @return True if user exists in DB, false otherwise.
     */
    public boolean exists(String user) {
        Cursor res = database.query(ExpenseData.USERS_TABLE, colsToReturn, ExpenseData.USER_NAME +
                " = '" + user + "'", null, null, null, null);
        int cnt = res.getCount();
        res.close();
        return cnt > 0;
    }

    /**
     * Inserts a new user into the database.
     * @param name The name of the user to insert.
     * @return The inserted user.
     */
    public User newUser(String name) {
        ContentValues cv = new ContentValues();
        cv.put(ExpenseData.USER_NAME, name);

        Cursor cursor = null;
        long insertId = 0;

        // watch for unique constraint exception
        try {
            // returns column position, or -1 if fail
            insertId = database.insert(ExpenseData.USERS_TABLE, null, cv);

            // query db to get id and return added user
            cursor = database.query(ExpenseData.USERS_TABLE, colsToReturn, ExpenseData.USER_ID +
                    " = " + insertId, null, null, null, null);
        } catch (SQLiteConstraintException ce) {
            // unique constraint violated
        }

        if (insertId > 0) {
            cursor.moveToFirst();
            User us = new User();
            us.setId(cursor.getInt(0));
            us.setName(cursor.getString(1));
            cursor.close();
            return us;
        } else {
            return null; // insertion failed
        }
    }

    /**
     * Updates an existing user's name in the database.
     * @param name The user object with the new name.
     * @return The updated user.
     */
    public User editUser(User name) {
        ContentValues cv = new ContentValues();
        cv.put(ExpenseData.USER_NAME, name.getName());
        database.update(ExpenseData.USERS_TABLE, cv, ExpenseData.USER_ID + " = '" + name.getId() + "'", null);
        return name;
    }

    /**
     * Deletes an existing user from the database.
     * @param user The user to be deleted.
     * @return The deleted user.
     */
    public User deleteUser(User user) {
        // will delete user only. categories and expenses will remain but cannot be accessed
        database.delete(ExpenseData.USERS_TABLE, ExpenseData.USER_ID + " = '" + user.getId() + "'", null);
        return user;
    }

    /**
     * Retrieves all users from the database.
     * @return The list of retrieved users.
     */
    public List<User> getAllUsers() {
        List<User> ans = new ArrayList<User>();

        // query db and get all users
        Cursor res = database.query(ExpenseData.USERS_TABLE, colsToReturn, null, null, null, null, null);

        res.moveToFirst();
        while (!res.isAfterLast()) {
            User u = new User();
            u.setId(res.getInt(0)); // get id
            u.setName(res.getString(1)); // get name
            ans.add(u);
            res.moveToNext();
        }

        res.close();
        return ans;
    }
}
