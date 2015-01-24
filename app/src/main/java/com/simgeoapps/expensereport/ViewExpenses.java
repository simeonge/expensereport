package com.simgeoapps.expensereport;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
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
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;


public class ViewExpenses extends ListActivity {

    /** Expenses data source. */
    private ExpenseDao exSource;

    /** Currently active user, as specified in config class. */
    private User curUser;

    /** Variable to hold currently specified date. */
    private static Calendar date;

    /** Currently selected category, as specified by intent received from ViewCategories class. */
    private Category curCat;

    /** Sum total of the current category, as given by intent received from ViewCategories class. */
    private String totalCost;

    /** Action mode for the context menu. */
    private ActionMode aMode;

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        /** Title which displays category name. */
        private TextView title;

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_expenses, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            title = (TextView) findViewById(R.id.exCat);
            title.setClickable(false); // prevent navigation away from activity
            return true; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
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
     * Gets the all the expenses for a particular category.
     */
    private void populateExpenses() {
        // retrieve all expenses for the user and category and specified month and year
        List<Expense> exs = exSource.getExpenses(curUser, curCat, date.get(Calendar.MONTH), date.get(Calendar.YEAR));

        // use adapter to show elements in list
        ArrayAdapter<Expense> aa = new ArrayAdapter<Expense>(this,
                android.R.layout.simple_list_item_activated_1, exs);
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
        enterDesc.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
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
                } else if (!Pattern.matches("^(\\d{1,10})?(\\.\\d{0,2})?$", cost)) { // must be $$
                    enterCost.setError("Please enter a valid dollar amount.");
                } else {
                    // can be added
                    try {
                        Expense ex = exSource.newExpense(new BigDecimal(cost), desc,
                                date.get(Calendar.DATE), date.get(Calendar.MONTH),
                                date.get(Calendar.YEAR), curUser, curCat);
                        adapter.add(ex);
                        adapter.notifyDataSetChanged();
                        dia.dismiss();
                        // update total
                        TextView total = (TextView) findViewById(R.id.exTotal);
                        total.setText("Total: " + exSource.getTotalCost(curUser, curCat, date.get(Calendar.MONTH), date.get(Calendar.YEAR)));
                    } catch (NumberFormatException ne) {
                        enterCost.setError("Please enter a valid dollar amount.");
                    }
                }
            }
        });
    }

    /**
     * Method to delete selected expense. Called when Delete button is click in context menu.
     */
    private void deleteExpense() {
        // get list view and list adapter
        ListView lv = getListView();
        ArrayAdapter<Expense> aa = (ArrayAdapter<Expense>) getListAdapter();
        int pos = lv.getCheckedItemPosition(); // get pos of selected item
        Expense del = aa.getItem(pos); // get item in adapter at position pos
        exSource.deleteExpense(del); // delete selected item from db
        aa.remove(del); // remove selected item from adapter
        aa.notifyDataSetChanged();
        // update total
        TextView total = (TextView) findViewById(R.id.exTotal);
        total.setText("Total: " + exSource.getTotalCost(curUser, curCat, date.get(Calendar.MONTH), date.get(Calendar.YEAR)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_expenses);

        // get intent
        curUser = GlobalConfig.getCurrentUser();
        date = GlobalConfig.getDate();
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
        TextView total = (TextView) findViewById(R.id.exTotal);
        total.setText("Total: " + exSource.getTotalCost(curUser, curCat, date.get(Calendar.MONTH), date.get(Calendar.YEAR)));

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
