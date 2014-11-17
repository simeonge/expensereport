package com.simgeoapps.expensereport;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

/**
 * Activity to display the list of users.
 * @author Simeon
 */
public class ViewUsers extends ListActivity {

    /** User data source. */
    private UserDao uSource;

    /** Intent extra tag for selected user. */
    public static final String CURRENT_USER = "com.simgeoapps.expensereport.CURRENTUSER";

    private void populateListView() {
        // get all users from db
        List<User> values = uSource.getAllUsers();

        // use the SimpleCursorAdapter to show the elements in a ListView
        ArrayAdapter<User> adapter = new ArrayAdapter<User>(this,
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);

        // set item onclick listener to each item in list
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // retrieve selected user
                String name = ((TextView) view).getText().toString();
                User us = new User();
                us.setName(name);

                // pass user to ViewCategories activity using the intent
                Intent intent = new Intent(ViewUsers.this, ViewCategories.class);
                intent.putExtra(CURRENT_USER, us.getName());
                startActivity(intent);
            }
        });
    }

    public void addUser(View v) {
        // build dialog to ask for name of user
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create user");
        builder.setMessage("Please enter your name.");

        // construct input field
        final EditText enterName = new EditText(this);
        enterName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS); // capitalized words
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_users);

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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
