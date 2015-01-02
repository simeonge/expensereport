package com.simgeoapps.expensereport;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
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
import android.widget.ListView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

/**
 * Activity to display list of categories for a user.
 * @author Simeon
 */
public class ViewCategories extends ListActivity {

    /** Category data source. */
    private CategoryDao catSource;

    /** Currently active user, as specified by intent received from ViewUsers class. */
    private User curUser;

    /** Variable to hold today's date. */
    private static Calendar date;

    public static final String[] MONTHS = { "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};

    /** Action mode for the context menu. */
    private ActionMode aMode;

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_categories, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    // edit a category name
                    editCategory();
                    mode.finish(); // close the CAB
                    return true;
                case R.id.action_del:
                    // delete selected category
                    deleteCategory();
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
        }
    };

    /**
     * Method to populate the list view with all categories for specified user.
     */
    private void populateCats() {
        // get all categories for specified user
        List<Category> values = catSource.getCategories(curUser);

//        TextView tv = (TextView) findViewById(R.id.catMon);
//        tv.setText(MONTHS[date.get(Calendar.MONTH)]);

        // use adapter to show the elements in a ListView
        // change to custom layout if necessary
        final ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(this,
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);

        // set item onclick listener to each item in list
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // retrieve selected category
                Category cat = adapter.getItem(i);

                // pass user + category to ViewExpenses activity using the intent
                Intent intent = new Intent(ViewCategories.this, ViewExpenses.class);
                intent.putExtra(IntentTags.CURRENT_USER, curUser);
                intent.putExtra(IntentTags.CURRENT_CATEGORY, cat);
                intent.putExtra(IntentTags.CURRENT_DATE, date);
                startActivity(intent);
            }
        });

        // set long click listener, to display CAB
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            // Called when the user long-clicks on an item
            public boolean onItemLongClick(AdapterView<?> aView, View view, int i, long l) {
                if (aMode != null) {
                    return false;
                }
                getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                // mark item at position i as selected
                getListView().setItemChecked(i, true);
                // Start the CAB using the ActionMode.Callback defined above
                aMode = ViewCategories.this.startActionMode(mActionModeCallback);
                return true;
            }
        });
    }

    /**
     * Method to add a new category. Called when Add button in action bar is clicked.
     */
    public void addCategory() {
        // build dialog to ask for category
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create category");
        builder.setMessage("Please enter a category name.");

        // construct input field
        final EditText enterCat = new EditText(this);
        enterCat.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES); // capitalized phrase
        enterCat.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        builder.setView(enterCat);

        // add ok and cancel buttons
        builder.setPositiveButton(R.string.ok, null);
        builder.setNegativeButton(R.string.cancel, null);

        // create dialog
        final AlertDialog dia = builder.create(); // don't show yet

        // set listener to input field to click OK when done
        enterCat.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
        final ArrayAdapter<Category> adapter = (ArrayAdapter<Category>) getListAdapter();

        // override onclick for OK button; must be done after show()ing to retrieve OK button
        dia.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // retrieve name entered
                String catName = enterCat.getText().toString().trim();

                // perform checks and add if pass
                if (catName.equals("")) { // must not be empty
                    enterCat.setError("Please enter a name.");
                } else if (catSource.exists(catName, curUser)) { // must not exist for current user
                    enterCat.setError("This category already exists.");
                } else {
                    // can be added
                    Category cat = catSource.newCategory(catName, curUser);
                    adapter.add(cat);
                    adapter.notifyDataSetChanged();
                    dia.dismiss();
                }
            }
        });
    }

    /**
     * Method to edit a category title, called when the Edit button in the context menu is clicked.
     */
    private void editCategory() {
        // retrieve adapter and retrieve selected category
        ListView lv = getListView();
        final ArrayAdapter<Category> aa = (ArrayAdapter<Category>) getListAdapter();
        final Category catToEdi = aa.getItem(lv.getCheckedItemPosition()); // get item at checked pos

        // show dialog to enter new name for the category
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit category");
        builder.setMessage("Please enter a new category name.");

        // construct input field
        final EditText enterName = new EditText(this);
        enterName.setText(catToEdi.getCategory()); // prepopulate with current category name
        enterName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES); // capitalized phrase
        enterName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        builder.setView(enterName);

        // add ok and cancel buttons
        builder.setPositiveButton(R.string.conf, null);
        builder.setNegativeButton(R.string.cancel, null);

        // create dialog
        final AlertDialog dia = builder.create(); // does not show it yet

        // set listener to input field to click OK when done
        enterName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
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

        dia.show(); // show dialog

        // override onclick for OK button; must be done after show()ing to retrieve OK button
        dia.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // retrieve name entered
                String catName = enterName.getText().toString().trim();

                // perform checks and add only if pass
                if (catName.equals("")) { // must not be empty
                    enterName.setError("Please enter a name.");
                } else if (catSource.exists(catName, curUser)) { // must not exist
                    enterName.setError("This category already exists.");
                } else {
                    // can be changed
                    aa.remove(catToEdi); // remove category from adapter
                    catToEdi.setCategory(catName); // change name in object
                    catSource.editCaterogy(catToEdi, curUser); // change in db
                    aa.add(catToEdi); // add category back to adapter
                    aa.notifyDataSetChanged();
                    dia.dismiss();
                }
            }
        });
    }

    /**
     * Method to delete a category, called when the Delete button in the context menu is clicked.
     */
    private void deleteCategory() {
        // show dialog confirming deletion
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete category");
        builder.setMessage("Are you sure? All expenses for this category will be deleted.");

        // add ok and cancel buttons
        builder.setPositiveButton(R.string.conf, null);
        builder.setNegativeButton(R.string.cancel, null);

        // create dialog
        final AlertDialog dia = builder.create();
        dia.show(); // show dialog

        // get list view and list adapter
        ListView lv = getListView();
        final ArrayAdapter<Category> aa = (ArrayAdapter<Category>) getListAdapter();
        final Category catToDel = aa.getItem(lv.getCheckedItemPosition()); // get item at checked pos

        // override onclick for OK button; must be done after show()ing to retrieve OK button
        dia.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                catSource.deleteCategory(catToDel, curUser); // delete category from db
                aa.remove(catToDel); // remove from adapter
                aa.notifyDataSetChanged(); // update view
                dia.dismiss(); // close dialog
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_categories);

        // retrieve selected user's user ID from intent
        curUser = (User) getIntent().getSerializableExtra(IntentTags.CURRENT_USER);
        date = (Calendar) getIntent().getSerializableExtra(IntentTags.CURRENT_DATE);

        // open data source
        catSource = new CategoryDao(this);
        catSource.open();
        populateCats(); // display user's categories
    }

    @Override
    protected void onResume() {
        catSource.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        catSource.close();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_categories, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a w activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_new) {
            addCategory();
            return true;
        } else if (id == R.id.switch_user) {
            Intent intent = new Intent(this, ViewUsers.class);
            startActivity(intent); // start user activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
