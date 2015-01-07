package com.simgeoapps.expensereport;

import java.util.Calendar;

/**
 * Class to hold global app settings
 * @author Simeon
 */
public class GlobalConfig {

    /** Current app user.*/
    private static User currentUser;

    /** Month and year values for expenses, initialized to today's month and year by default. */
    private static Calendar currentDate;

    // getters and setters
    public static User getCurrentUser() {
        return currentUser;
    }

    public static Calendar getDate() {
        return currentDate;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void setDate(Calendar date) {
        currentDate = date;
    }

}
