package com.simgeoapps.expensereport;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Database helper class.
 * Created by Simeon on 10/5/2014.
 */
public class ExpenseData extends SQLiteOpenHelper {
    // fields
    /** Name of the database. */
    private static final String DATABASE_NAME = "Expenses.db";

    /** Database version. */
    private static final int DATABASE_VERSION = 1;

    // tables
    public static final String USERS_TABLE = "users";
    public static final String CATEGORIES_TABLE = "categories";
    public static final String EXPENSES_TABLE = "expenses";

    // USERS, CATEGORIES and EXPENSES table columns
    public static final String USER_ID = "user_id";
    public static final String USER_NAME = "name";

    public static final String CATEGORY_ID = "cat_id";
    public static final String CATEGORY_NAME = "category";

    public static final String EXPENSE_ID = "expense_id";
    public static final String COST_COLUMN = "cost"; // in cents
    public static final String DESCRIPTION_COLUMN = "description";
    public static final String DAY_COLUMN = "day";
    public static final String MONTH_COLUMN = "month"; // 0-based
    public static final String YEAR_COLUMN = "year";

    // sql statements
    private static final String CREATE_USER_TABLE = "CREATE TABLE " + USERS_TABLE + " (" + USER_ID +
            " integer primary key autoincrement, " + USER_NAME + " text not null unique);";

    private static final String CREATE_CATEGORIES_TABLE = "CREATE TABLE " + CATEGORIES_TABLE +
            " (" + CATEGORY_ID + " integer primary key autoincrement, " + USER_ID+
            " integer not null, " + CATEGORY_NAME + " text not null);";

    private static final String CREATE_EXPENSES_TABLE = "CREATE TABLE " + EXPENSES_TABLE +
            " (" + EXPENSE_ID + " integer primary key autoincrement, " + USER_ID +
            " integer not null, " + CATEGORY_ID + " integer not null, " + COST_COLUMN +
            " integer not null, " + DESCRIPTION_COLUMN + " text, " + DAY_COLUMN + " text not null, " +
            MONTH_COLUMN + " text not null, " + YEAR_COLUMN + " text not null);";

    // constructor
    public ExpenseData(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        // create tables
        database.execSQL(CREATE_USER_TABLE);
        database.execSQL(CREATE_CATEGORIES_TABLE);
        database.execSQL(CREATE_EXPENSES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(ExpenseData.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data.");
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + EXPENSES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CATEGORIES_TABLE);
        onCreate(db);
    }
}
