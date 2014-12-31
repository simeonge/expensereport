package com.simgeoapps.expensereport;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * User DAO. Supports adding, editing. Support for deleting users pending.
 * Created by Simeon on 11/6/2014.
 */
public class UserDao {
    // Database fields
    private SQLiteDatabase database;
    private ExpenseData dbHelper;
    private String[] colsToReturn = { ExpenseData.USER_ID,
            ExpenseData.USER_NAME };

    public UserDao(Context context) {
        dbHelper = new ExpenseData(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public boolean exists(String user) {
        Cursor res = database.query(ExpenseData.USERS_TABLE, colsToReturn, ExpenseData.USER_NAME +
                " = '" + user + "'", null, null, null, null);
        return res.getCount() > 0;
    }

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
            Log.e("DB Error", "Unique constrain violated", ce);
        }

        if (insertId > 0) {
            cursor.moveToFirst();
            User us = new User();
            us.setId(cursor.getInt(0));
            us.setName(cursor.getString(1));
            cursor.close();
            return us;
        } else {
            Log.e("Insert User", "Unable to insert user");
            return null;
        }
    }

    public void editUser(User name) {
        ContentValues cv = new ContentValues();
        cv.put(ExpenseData.USER_NAME, name.getName());
        database.update(ExpenseData.USERS_TABLE, cv, ExpenseData.USER_ID + " = '" + name.getId() + "'", null);
    }

    public void deleteUser(User user) {
        // will delete user only. categories and expenses will remain but cannot be accessed
        database.delete(ExpenseData.USERS_TABLE, ExpenseData.USER_ID + " = '" + user.getId() + "'", null);
    }

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
