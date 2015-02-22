package com.simgeoapps.expensereport;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Category DAO. Supports adding, editing, and deleting categories.
 */
public class CategoryDao {
    /** Instance of database which will be queried. */
    private SQLiteDatabase database;

    /** Instance of the database helper class. */
    private ExpenseData dbHelper;

    // columns
    private String[] colsToReturn = { ExpenseData.CATEGORY_ID, ExpenseData.USER_ID,
            ExpenseData.CATEGORY_NAME };

    public CategoryDao(Context context) {
        dbHelper = new ExpenseData(context);
    }

    // open and close DB.
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * Determines if the specified category already exists in the database for the specified user.
     * @param category The category to be checked for existence.
     * @param us The user for which to verify for the existing of the category.
     * @return True if the category exists for the specified user, false otherwise.
     */
    public boolean exists(String category, User us) {
        Cursor res = database.query(ExpenseData.CATEGORIES_TABLE, colsToReturn, ExpenseData.CATEGORY_NAME +
                " = '" + category + "' AND " + ExpenseData.USER_ID + " = '" + us.getId() + "'", null, null, null, null);
        int cnt = res.getCount();
        res.close();
        return cnt > 0;
    }

    /**
     * Inserts a new category in the database for the specified user.
     * @param cat The name of the category to insert.
     * @param us The user to which the category belongs.
     * @return The inserted category.
     */
    public Category newCategory(String cat, User us) {
        ContentValues cv = new ContentValues();
        cv.put(ExpenseData.CATEGORY_NAME, cat);
        cv.put(ExpenseData.USER_ID, us.getId());
        long insertId = database.insert(ExpenseData.CATEGORIES_TABLE, null, cv);

        // query db and get inserted category
        Cursor cur = database.query(ExpenseData.CATEGORIES_TABLE, colsToReturn,
                ExpenseData.CATEGORY_ID + " = " + insertId, null, null, null, null);

        cur.moveToFirst();
        Category ans = new Category();
        ans.setId(cur.getInt(0));
        ans.setUserId(cur.getInt(1));
        ans.setCategory(cur.getString(2));
        cur.close();

        return ans;
    }

    /**
     * Updates the name of an existing category that belongs to the specified user.
     * @param cat The category object with the new name.
     * @return The updated category.
     */
    public Category editCategory(Category cat) {
        ContentValues cv = new ContentValues();
        cv.put(ExpenseData.CATEGORY_NAME, cat.getCategory());
        database.update(ExpenseData.CATEGORIES_TABLE, cv, ExpenseData.CATEGORY_ID + " = '" +
                cat.getId() + "'", null);
        return cat;
    }

    /**
     * Deletes an existing category that belongs to the specified user.
     * @param cat The category to be deleted.
     * @return The deleted category.
     */
    public Category deleteCategory(Category cat) {
        // delete expenses for the category
        database.delete(ExpenseData.EXPENSES_TABLE, ExpenseData.CATEGORY_ID + " = '" + cat.getId() + "'", null);
        // delete category
        database.delete(ExpenseData.CATEGORIES_TABLE, ExpenseData.CATEGORY_ID + " = '" + cat.getId() + "'", null);
        return cat;
    }

    /**
     * Retrieves all categories for the specified user.
     * @param us The user whose categories to retrieve.
     * @return The list of retrieved categories.
     */
    public List<Category> getCategories(User us) {
        // must return all categories for a certain user
        List<Category> ans = new ArrayList<>();

        // query db and get all categories for user us
        Cursor res = database.query(ExpenseData.CATEGORIES_TABLE, colsToReturn,
                ExpenseData.USER_ID + " = '" + us.getId() + "'", null, null, null, null);

        res.moveToFirst();
        while (!res.isAfterLast()) {
            Category cat = new Category();
            cat.setId(res.getInt(0));
            cat.setUserId(res.getInt(1));
            cat.setCategory(res.getString(2));
            ans.add(cat);
            res.moveToNext();
        }

        res.close();
        return ans;
    }

}
