package com.simgeoapps.expensereport;

import android.app.Application;

import java.util.Calendar;
import java.util.List;

/**
 * Class to hold global app settings
 */
public class GlobalConfig extends Application {

    /** Current app user.*/
    private User currentUser;

    /** Month and year values for expenses, initialized to today's month and year by default. */
    private Calendar currentDate;

    // getters and setters
    public User getCurrentUser() {
        return currentUser;
    }

    public Calendar getDate() {
        return currentDate;
    }

    public void setCurrentUser(User user) {
        currentUser = user;
    }

    public void setDate(Calendar date) {
        currentDate = date;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // here pass current month and year, and default/only user, and
        setDate(Calendar.getInstance()); // use current date by default
        UserDao uSource = new UserDao(this);
        uSource.open();
        List<User> lu = uSource.getAllUsers(); // TODO cache

        if (lu.size() == 1) {
            User soleUser = lu.get(0);
            setCurrentUser(soleUser); // set default user if only one
        }

        uSource.close();
    }
}
