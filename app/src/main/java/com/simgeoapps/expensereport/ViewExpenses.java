package com.simgeoapps.expensereport;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Activity to display list of expenses for user's category.
 */
public class ViewExpenses extends ListActivity {

    /** Expenses data source. */
    private ExpenseDao exSource;

    /** Currently active user, as specified in config class. */
    private User curUser;

    /** Variable to hold currently specified date. */
    private static Calendar date;

    /** Currently selected category, as specified by intent received from ViewCategories class. */
    private Category curCat;

    /** The sum total of all expenses for the active category. */
    private BigDecimal categoryTotal;

    /** Action mode for the context menu. */
    private ActionMode aMode;

    /** Call back methods for the context menu. */
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        /** Title which displays category name. */
        private TextView title;

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_expenses, menu);

            // disable listener here; moved from onPrepareActionMode
            title = (TextView) findViewById(R.id.exCat);
            title.setClickable(false); // prevent navigation away from activity
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // EDIT 03/22/15: This method does not get called anymore
            // ANDROID ISSUE: 159527
//            title = (TextView) findViewById(R.id.exCat);
//            title.setClickable(false); // prevent navigation away from activity
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    // edit selected expense
                    editExpense();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.action_del:
                    // delete selected expense
                    deleteExpense();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // unselect item that was selected (if it wasn't deleted)
            final ListView lv = getListView();
            lv.clearChoices();
            lv.setItemChecked(lv.getCheckedItemPosition(), false);
            // ((ArrayAdapter<Expense>) getListAdapter()).notifyDataSetChanged();
            // prevent item selection when context menu is inactive
            // doesn't work if called in same thread and item remains highlighted;
            // calling from new thread as a work around
            lv.post(new Runnable() {
                @Override
                public void run() {
                    lv.setChoiceMode(ListView.CHOICE_MODE_NONE);
                }
            });
            aMode = null;

            title.setClickable(true); // restore category name click
        }
    };

    /**
     * Class to asynchronously retrieve expenses from database.
     */
    private class GetExpenses extends AsyncTask<Void, Void, List<Expense>> {
        @Override
        protected List<Expense> doInBackground(Void... params) {
            // retrieve all expenses for the user and category and specified month and year
            return exSource.getExpenses(curUser, curCat, date.get(Calendar.MONTH), date.get(Calendar.YEAR));
        }

        @Override
        protected void onPostExecute(final List<Expense> result) {
            // use adapter to show elements in list
            ArrayAdapter<Expense> aa = new ArrayAdapter<>(ViewExpenses.this,
                    android.R.layout.simple_list_item_activated_1, result);
            setListAdapter(aa);

            final ListView lv = getListView();
            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                // Called when the user long-clicks on an item
                public boolean onItemLongClick(AdapterView<?> aView, View view, int i, long l) {
                    if (aMode != null) {
                        return false;
                    }
                    lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    // mark item at position i as selected
                    lv.setItemChecked(i, true);
                    // Start the CAB using the ActionMode.Callback defined above
                    aMode = ViewExpenses.this.startActionMode(mActionModeCallback);
                    return true;
                }
            });
        }
    }

    /**
     * Class to asynchronously add new expense to database.
     */
    private class AddExpense extends AsyncTask<String, Void, Expense> {
        @Override
        protected Expense doInBackground(String... params) {
            return exSource.newExpense(new BigDecimal(params[0]), params[1],
                    date.get(Calendar.DATE), date.get(Calendar.MONTH),
                    date.get(Calendar.YEAR), curUser, curCat);
        }

        @Override
        protected void onPostExecute(Expense result) {
            @SuppressWarnings("unchecked")
            ArrayAdapter<Expense> aa = (ArrayAdapter<Expense>) getListAdapter();
            aa.add(result);
            aa.notifyDataSetChanged();

            // update total
            categoryTotal = categoryTotal.add(result.getCost());
            TextView total = (TextView) findViewById(R.id.exTotal);
            total.setText("Total: " + NumberFormat.getCurrencyInstance().format(categoryTotal));
    }
    }

    /**
     * Class to asynchronously edit an expense in database.
     */
    private class EditExpense extends AsyncTask<Expense, Void, Expense> {
        @Override
        protected Expense doInBackground(Expense... params) {
            return exSource.editExpense(params[0]);
        }

        @Override
        protected void onPostExecute(Expense result) {
            @SuppressWarnings("unchecked")
            ArrayAdapter<Expense> aa = (ArrayAdapter<Expense>) getListAdapter();
            aa.notifyDataSetChanged();

            // update total
            categoryTotal = categoryTotal.add(result.getCost());
            TextView total = (TextView) findViewById(R.id.exTotal);
            total.setText("Total: " + NumberFormat.getCurrencyInstance().format(categoryTotal));
        }
    }

    /**
     * Class to asynchronously delete an expense from database.
     */
    private class DeleteExpense extends AsyncTask<Expense, Void, Expense> {
        @Override
        protected Expense doInBackground(Expense... params) {
            return exSource.deleteExpense(params[0]); // delete selected item from db
        }

        @Override
        protected void onPostExecute(Expense result) {
            @SuppressWarnings("unchecked")
            ArrayAdapter<Expense> aa = (ArrayAdapter<Expense>) getListAdapter();
            aa.remove(result); // remove selected item from adapter
            aa.notifyDataSetChanged();

            // update total
            categoryTotal = categoryTotal.subtract(result.getCost());
            TextView total = (TextView) findViewById(R.id.exTotal);
            total.setText("Total: " + NumberFormat.getCurrencyInstance().format(categoryTotal));
        }
    }

    /**
     * Method to record a new expense. Called when Add button in action bar is clicked.
     */
    private void addExpense() {
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
        enterDesc.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
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
                } else if (!Pattern.matches("^(\\d{1,10})?(\\.\\d{0,2})?$", cost)) { // must be $$
                    enterCost.setError("Please enter a valid dollar amount.");
                } else {
                    // can be added
                    new AddExpense().execute(cost, desc);
                    dia.dismiss();
                }
            }
        });
    }

    /**
     * Method to edit selected expense. Called when Edit button is clicked in context menu.
     */
    private void editExpense() {
        // retrieve adapter and retrieve selected expense
        ListView lv = getListView();
        @SuppressWarnings("unchecked")
        final ArrayAdapter<Expense> aa = (ArrayAdapter<Expense>) getListAdapter();
        final Expense exToEdi = aa.getItem(lv.getCheckedItemPosition()); // get item at checked pos

        // build dialog to ask for expense details
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit expense");
        builder.setMessage("Please enter expense details.");

        // construct input fields
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        final EditText enterCost = new EditText(this);
        final EditText enterDesc = new EditText(this);
        enterCost.setText(exToEdi.getCost().toString());
        enterDesc.setText(exToEdi.getDescription());
        enterCost.setInputType(InputType.TYPE_CLASS_NUMBER); // to accept dollar amount
        enterCost.setKeyListener(DigitsKeyListener.getInstance("0123456789.")); // accept digits
        enterDesc.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES); // description text
        enterDesc.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
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
                } else if (!Pattern.matches("^(\\d{1,10})?(\\.\\d{0,2})?$", cost)) { // must be $$
                    enterCost.setError("Please enter a valid dollar amount.");
                } else {
                    // can be changed
                    categoryTotal = categoryTotal.subtract(exToEdi.getCost());
                    exToEdi.setCost(new BigDecimal(cost));
                    exToEdi.setDescription(desc);
                    new EditExpense().execute(exToEdi);
                    dia.dismiss();
                }
            }
        });
    }

    /**
     * Method to delete selected expense. Called when Delete button is clicked in context menu.
     */
    private void deleteExpense() {
        // get list view and list adapter
        ListView lv = getListView();
        @SuppressWarnings("unchecked")
        ArrayAdapter<Expense> aa = (ArrayAdapter<Expense>) getListAdapter();
        int pos = lv.getCheckedItemPosition(); // get pos of selected item
        Expense del = aa.getItem(pos); // get item in adapter at position pos
        new DeleteExpense().execute(del); // delete expense async and update total
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_expenses);

        // get intent
        GlobalConfig settings = (GlobalConfig) getApplication();
        curUser = settings.getCurrentUser();
        date = settings.getDate();
        curCat = (Category) getIntent().getSerializableExtra(IntentTags.CURRENT_CATEGORY);
        // set totalCost = ; here

        // set title to category
        TextView title = (TextView) findViewById(R.id.exCat);
        title.setText(curCat.getCategory());
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(ViewExpenses.this, ViewCategories.class);
                startActivity(it);
            }
        });

        // open data source
        exSource = new ExpenseDao(this);
        exSource.open();

        // display total for user, cat, month/year
        categoryTotal = exSource.getTotalCost(curUser, curCat, date.get(Calendar.MONTH), date.get(Calendar.YEAR));
        TextView total = (TextView) findViewById(R.id.exTotal);
        total.setText("Total: " + NumberFormat.getCurrencyInstance().format(categoryTotal));

        new GetExpenses().execute(); // retrieve display expenses for the category
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
        // Inflate the menu
        getMenuInflater().inflate(R.menu.view_expenses, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_new) {
            addExpense();
            return true;
        } else if (id == R.id.switch_user) {
            Intent intent = new Intent(this, ViewUsers.class);
            startActivity(intent); // start user activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
