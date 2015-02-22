package com.simgeoapps.expensereport;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Expenses DAO. Supports adding, (editing), deleting expenses.
 */
public class ExpenseDao {
    /** Instance of database which will be queried. */
    private SQLiteDatabase database;

    /** Instance of the database helper class. */
    private ExpenseData dbHelper;

    // columns
    private String[] colsToReturn = { ExpenseData.EXPENSE_ID, ExpenseData.USER_ID,
            ExpenseData.CATEGORY_ID, ExpenseData.COST_COLUMN, ExpenseData.DESCRIPTION_COLUMN,
            ExpenseData.DAY_COLUMN, ExpenseData.MONTH_COLUMN, ExpenseData.YEAR_COLUMN };

    public ExpenseDao(Context context) {
        dbHelper = new ExpenseData(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * Insert a new expense record into the database.
     * @param cost The cost property for this expense.
     * @param description The description for this expense.
     * @param day The integer day for the date that this expense occurred.
     * @param mon The month that this expense occurred.
     * @param yea The year that this expense occurred.
     * @param us The user that this expense belongs to.
     * @param cat The category under which this expense belongs.
     * @return The inserted expense.
     */
    public Expense newExpense(BigDecimal cost, String description, int day, int mon, int yea, User us,
                              Category cat) {
        ContentValues cv = new ContentValues();
        cv.put(ExpenseData.USER_ID, us.getId());
        cv.put(ExpenseData.CATEGORY_ID, cat.getId());
        cv.put(ExpenseData.COST_COLUMN, cost.movePointRight(2).longValueExact());
        cv.put(ExpenseData.DESCRIPTION_COLUMN, description);
        cv.put(ExpenseData.DAY_COLUMN, Integer.toString(day));
        cv.put(ExpenseData.MONTH_COLUMN, Integer.toString(mon));
        cv.put(ExpenseData.YEAR_COLUMN, Integer.toString(yea));
        long insertId = database.insert(ExpenseData.EXPENSES_TABLE, null, cv);

        // query db and get inserted category
        Cursor cur = database.query(ExpenseData.EXPENSES_TABLE, colsToReturn,
                ExpenseData.EXPENSE_ID + " = " + insertId, null, null, null, null);

        cur.moveToFirst();
        Expense ans = new Expense();
        ans.setId(cur.getInt(0)); // expense id
        ans.setUserId(cur.getInt(1)); // user id
        ans.setCategoryId(cur.getInt(2)); // category id
        ans.setCost(new BigDecimal(cur.getLong(3)).movePointLeft(2)); // cost from integer
        ans.setDescription(cur.getString(4)); // description
        ans.setDay(cur.getString(5)); // day
        ans.setMonth(cur.getString(6)); // month
        ans.setYear(cur.getString(7)); // year

        cur.close();

        return ans;
    }

    /**
     * Updates the cost and description of an existing expense.
     * @param ex The expense to be updated, having the ID of an existing expense, and the new cost
     *           and description.
     * @return The updated expense.
     */
    public Expense editExpense(Expense ex) {
        ContentValues cv = new ContentValues();
        cv.put(ExpenseData.COST_COLUMN, ex.getCost().movePointRight(2).longValueExact());
        cv.put(ExpenseData.DESCRIPTION_COLUMN, ex.getDescription());

        database.update(ExpenseData.EXPENSES_TABLE, cv, ExpenseData.EXPENSE_ID + " = '" +
                ex.getId() + "'", null);
        return ex;
    }

    /**
     * Deletes an existing expense from the database.
     * @param exp The expense to be deleted.
     * @return The deleted expense.
     */
    public Expense deleteExpense(Expense exp) {
        // returns number of rows affected
        database.delete(ExpenseData.EXPENSES_TABLE, ExpenseData.EXPENSE_ID + " = '" + exp.getId()
                + "'", null);
        return exp;
    }

    /**
     * Retrieves the sum total of all expenses for the given criteria. This method is used in the
     * ViewExpenses activity to show the total at the top.
     * @param us The user whose expenses should be included in the total.
     * @param cat The category whose expenses should be included in the total.
     * @param month The expenses for this month should be included in the total.
     * @param year The expenses for this year should be included in the total.
     * @return The dollar amount of the sum total of expenses that match the criteria.
     */
    public BigDecimal getTotalCost(User us, Category cat, int month, int year) {
        String[] cols = { ExpenseData.COST_COLUMN };
        Cursor res = database.query(ExpenseData.EXPENSES_TABLE, cols, ExpenseData.USER_ID + " = '" +
                us.getId() + "' AND " + ExpenseData.CATEGORY_ID + " = '" + cat.getId() + "' AND " +
                ExpenseData.MONTH_COLUMN + " = '" + Integer.toString(month) + "' AND " +
                ExpenseData.YEAR_COLUMN + " = '" + Integer.toString(year) + "'", null, null, null, null);

        BigDecimal totCost = new BigDecimal(0);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            totCost = totCost.add(new BigDecimal(res.getLong(0)));
            res.moveToNext();
        }
        res.close();

        // move decimal point
        return totCost.movePointLeft(2);
    }

    /**
     * Retrieves the sum total of all expenses for the given user and month/year.
     * @param us The user whose expenses should be included in the total.
     * @param month The expenses for this month should be included in the total.
     * @param year The expenses for this year should be included in the total.
     * @return The dollar amount of the sum total of expenses that match the criteria.
     */
    public BigDecimal getTotalCost(User us, int month, int year) {
        String[] cols = { ExpenseData.COST_COLUMN };
        Cursor res = database.query(ExpenseData.EXPENSES_TABLE, cols, ExpenseData.USER_ID + " = '" +
                us.getId() + "' AND " + ExpenseData.MONTH_COLUMN + " = '" + Integer.toString(month) +
                "' AND " + ExpenseData.YEAR_COLUMN + " = '" + Integer.toString(year) + "'", null, null, null, null);

        BigDecimal totCost = new BigDecimal(0);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            totCost = totCost.add(new BigDecimal(res.getLong(0)));
            res.moveToNext();
        }
        res.close();

        // move decimal point
        return totCost.movePointLeft(2);
    }

    /**
     * Retrieves the expenses for the specified user, category, month, and year.
     * @param us The user for which expenses should be returned.
     * @param cat The category for which expenses should be returned.
     * @param mon The month for which expenses should be returned.
     * @param yea The year for which expenses should be returned.
     * @return A list of expenses that match the given criteria.
     */
    public List<Expense> getExpenses(User us, Category cat, int mon, int yea) {
        List<Expense> ans = new ArrayList<>();

        Cursor res = database.query(ExpenseData.EXPENSES_TABLE, colsToReturn, ExpenseData.USER_ID +
                " = '" + us.getId() + "' AND " + ExpenseData.CATEGORY_ID + " = '" + cat.getId() +
                "' AND " + ExpenseData.MONTH_COLUMN + " = '" + Integer.toString(mon) + "' AND " +
                ExpenseData.YEAR_COLUMN + " = '" + Integer.toString(yea) + "'", null, null, null, null);

        res.moveToFirst();
        while (!res.isAfterLast()) {
            Expense ex = new Expense();
            ex.setId(res.getInt(0)); // expense id
            ex.setUserId(res.getInt(1)); // user id
            ex.setCategoryId(res.getInt(2)); // category id
            ex.setCost(new BigDecimal(res.getLong(3)).movePointLeft(2)); // cost
            ex.setDescription(res.getString(4)); // description
            ex.setDay(res.getString(5)); // day
            ex.setMonth(res.getString(6)); // month
            ex.setYear(res.getString(7)); // year

            ans.add(ex);
            res.moveToNext();
        }

        res.close();
        return ans;
    }

    /**
     * Retrieves all the expenses for the specified user and category for all months and years.
     * @param us The user for which expenses should be returned.
     * @param cat The category for which expenses should be returned.
     * @return A list of expenses that match the given criteria.
     */
    public List<Expense> getExpenses(User us, Category cat) {
        List<Expense> ans = new ArrayList<>();

        Cursor res = database.query(ExpenseData.EXPENSES_TABLE, colsToReturn, ExpenseData.USER_ID +
                " = '" + us.getId() + "' AND " + ExpenseData.CATEGORY_ID + " = '" +
                cat.getId() + "'", null, null, null, null);

        res.moveToFirst();
        while (!res.isAfterLast()) {
            Expense ex = new Expense();
            ex.setId(res.getInt(0)); // expense id
            ex.setUserId(res.getInt(1)); // user id
            ex.setCategoryId(res.getInt(2)); // category id
            ex.setCost(new BigDecimal(res.getLong(3)).movePointLeft(2)); // cost
            ex.setDescription(res.getString(4)); // description
            ex.setDay(res.getString(5)); // day
            ex.setMonth(res.getString(6)); // month
            ex.setYear(res.getString(7)); // year

            ans.add(ex);
            res.moveToNext();
        }

        res.close();
        return ans;
    }
}
