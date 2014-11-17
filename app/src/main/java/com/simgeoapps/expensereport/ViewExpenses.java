package com.simgeoapps.expensereport;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;


public class ViewExpenses extends ListActivity {

    private ExpenseDao exSource;

    private User curUser;

    private Category curCat;

    private static Calendar date;

    /**
     * Gets the all the expenses for a particular category.
     */
    private void populateExpenses() {
        // retrieve all expenses for the user and category and today
        List<Expense> exs = exSource.getExpenses(curUser, curCat, date.get(Calendar.MONTH) + 1, date.get(Calendar.YEAR));

        // use adapter to show elements in list
        ArrayAdapter<Expense> aa = new ArrayAdapter<Expense>(this,
                android.R.layout.simple_list_item_1, exs);
        setListAdapter(aa);
    }

    /**
     * Method to record a new expense. Called when button in view is clicked.
     * @param v The view.
     */
    public void addExpense(View v) {
        // build dialog to ask for expense details
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Record expense");
        builder.setMessage("Please enter expense details.");

        // construct input fields
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        final EditText enterCost = new EditText(this);
        final EditText enterDesc = new EditText(this);
        enterCost.setHint("Cost");
        enterDesc.setHint("Description (optional)");
        enterCost.setInputType(InputType.TYPE_CLASS_NUMBER); // to accept dollar amount
        enterCost.setKeyListener(DigitsKeyListener.getInstance("0123456789.")); // accept digits
        enterDesc.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES); // description text
        ll.addView(enterCost);
        ll.addView(enterDesc);
        builder.setView(ll);

        // add ok and cancel buttons
        builder.setPositiveButton(R.string.ok, null);
        builder.setNegativeButton(R.string.cancel, null);

        // create dialog
        final AlertDialog dia = builder.create(); // don't show yet

        // set listener to description input field to click OK when done
        enterDesc.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // click dialog's OK when user presses Done on keyboard
                    dia.getButton(Dialog.BUTTON_POSITIVE).performClick();
                    handled = true;
                }
                return handled;
            }
        });

        // set input mode to let keyboard appear when dialog is shown
        dia.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        dia.show();

        // retrieve adapter to add category to the list
        final ArrayAdapter<Expense> adapter = (ArrayAdapter<Expense>) getListAdapter();

        // override onclick for OK button; must be done after show()ing to retrieve OK button
        dia.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // retrieve name entered
                String cost = enterCost.getText().toString().trim();
                String desc = enterDesc.getText().toString().trim();

                // perform checks and add if pass
                if (cost.equals("")) { // must not be empty
                    enterCost.setError("Please enter a dollar amount.");
                } else if (!Pattern.matches("^(\\d{1,4})?(\\.\\d{0,2})?$", cost)) { // must be $$
                    enterCost.setError("Please enter a valid dollar amount.");
                } else {
                    // can be added
                    try {
                        Expense ex = exSource.newExpense(Float.parseFloat(cost), desc,
                                date.get(Calendar.DATE), date.get(Calendar.MONTH) + 1,
                                date.get(Calendar.YEAR), curUser, curCat);
                        adapter.add(ex);
                        adapter.notifyDataSetChanged();
                        dia.dismiss();
                    } catch (NumberFormatException ne) {
                        enterCost.setError("Please enter a valid dollar amount.");
                    }
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_expenses);

        // get intent
        String curUserName = getIntent().getStringExtra(ViewUsers.CURRENT_USER);
        String curCatName = getIntent().getStringExtra(ViewCategories.CURRENT_CATEGORY);
        curUser = new User();
        curCat = new Category();
        curUser.setName(curUserName);
        curCat.setCategory(curCatName);

        // initialize calendar
        date = Calendar.getInstance();

        // open data source
        exSource = new ExpenseDao(this);
        exSource.open();
        populateExpenses(); // display expenses for the category
    }

    @Override
    protected void onResume() {
        exSource.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        exSource.close();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_expenses, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
