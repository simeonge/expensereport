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
 * Activity to display the list of users.
 * @author Simeon
 */
public class ViewUsers extends ListActivity {

    /** User data source. */
    private UserDao uSource;

    /* TODO Currently active user. */
    public User curUser;

    /** Action mode for the context menu. */
    private ActionMode aMode;

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_users, menu);
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
                    // edit a user's name
                    editUser();
                    mode.finish(); // close the CAB
                    return true;
                case R.id.action_del:
                    // delete selected user
                    deleteUser();
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
     * Method to populate the list view all users.
     */
    private void populateListView() {
        // get all users from db
        List<User> values = uSource.getAllUsers();

        // use the SimpleCursorAdapter to show the elements in a ListView
        final ArrayAdapter<User> adapter = new ArrayAdapter<User>(this,
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);

        // set item onclick listener to each item in list
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // retrieve selected user
                User us = adapter.getItem(i);

                // pass user to ViewCategories activity using the intent
                Intent intent = new Intent(ViewUsers.this, ViewCategories.class);
                intent.putExtra(IntentTags.CURRENT_USER, us);
                intent.putExtra(IntentTags.CURRENT_DATE, Calendar.getInstance());
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
                aMode = ViewUsers.this.startActionMode(mActionModeCallback);
                return true;
            }
        });

        if (values.size() == 0) {
            addUser();
        }
    }

    /**
     * Method to add a new user, called when the Add button the action bar is clicked.
     */
    private void addUser() {
        // build dialog to ask for name of user
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create user");
        builder.setMessage("Please enter your name.");

        // construct input field
        final EditText enterName = new EditText(this);
        enterName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS); // capitalized words
        enterName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(14)});
        builder.setView(enterName);

        // add ok and cancel buttons
        builder.setPositiveButton(R.string.ok, null);
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

        // retrieve adapter to add user to the list
        final ArrayAdapter<User> adapter = (ArrayAdapter<User>) getListAdapter();

        // override onclick for OK button; must be done after show()ing to retrieve OK button
        dia.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // retrieve name entered
                String username = enterName.getText().toString().trim();

                // perform checks and add only if pass
                if (username.equals("")) { // must not be empty
                    enterName.setError("Please enter a name.");
                } else if (uSource.exists(username)) { // must not exist
                    enterName.setError("This user already exists.");
                } else {
                    // can be added
                    User us = uSource.newUser(username);
                    adapter.add(us);
                    adapter.notifyDataSetChanged();
                    dia.dismiss();
                }
            }
        });
    }

    /**
     * Method to edit a user's name, called when the Edit button in the context menu is clicked.
     */
    private void editUser() {

    }

    /**
     * Method to delete a user, called when the Delete button in the context menu is clicked.
     */
    private void deleteUser() {
        // get list view and list adapter
        ListView lv = getListView();
        ArrayAdapter<User> aa = (ArrayAdapter<User>) getListAdapter();
        int pos = lv.getCheckedItemPosition(); // get pos of selected item
        User userToDel = aa.getItem(pos); // get item in adapter at position pos
        // show dialog confirming deletion

        // delete all categories and expenses for this user

        // update adapter, notify change

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_users);

        // retrieve selected user's user ID from intent
        // getIntent().getSerializableExtra("Date");

        // open data source
        uSource = new UserDao(this);
        uSource.open();
        populateListView(); // fill list with users
    }

    @Override
    protected void onResume() {
        uSource.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        uSource.close();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_users, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new) {
            addUser();
            return true;
        } else if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
