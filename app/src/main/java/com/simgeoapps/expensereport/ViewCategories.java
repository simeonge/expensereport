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
 * Activity to display list of categories for a user.
 * @author Simeon
 */
public class ViewCategories extends ListActivity {

    /** Category data source. */
    private CategoryDao catSource;

    /** Currently active user, as specified by intent received from ViewUsers class. */
    private User curUser;

    /** Intent extra tag for selected category. */
    public static final String CURRENT_CATEGORY = "com.simgeoapps.expensereport.CURRENTCATEGORY";

    /**
     * Method to populate the list view with all categories for specified user.
     */
    private void populateCats() {
        // get all categories for specified user
        List<Category> values = catSource.getCategories(curUser);

        // use adapter to show the elements in a ListView
        ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(this,
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);

        // set item onclick listener to each item in list
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // retrieve selected category
                String name = ((TextView) view).getText().toString();
                Category cat = new Category();
                cat.setCategory(name);

                // pass user to ViewCategories activity using the intent
                Intent intent = new Intent(ViewCategories.this, ViewExpenses.class);
                intent.putExtra(ViewUsers.CURRENT_USER, curUser.getName());
                intent.putExtra(CURRENT_CATEGORY, cat.getCategory());
                startActivity(intent);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_categories);

        // retrieve selected user's user ID from intent
        String curUserName = getIntent().getStringExtra(ViewUsers.CURRENT_USER);
        curUser = new User();
        curUser.setName(curUserName);
        // what about id? we don't have to set it?

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
        } else if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
