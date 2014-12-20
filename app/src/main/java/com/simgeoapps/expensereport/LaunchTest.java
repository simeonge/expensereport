package com.simgeoapps.expensereport;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Calendar;
import java.util.List;

/**
 * Test Launcher activity.
 * @author Simeon
 */
public class LaunchTest extends Activity {

    private UserDao uSource;

    private Calendar date;

    /**
     * Called when the begin button is clicked. Starts the ViewUsers activity.
     * @param vi The view.
     */
    public void begin(View vi) {
        // here pass current month and year, and default/only user, and
        List<User> lu = uSource.getAllUsers();

        // if we have only one user, go directly to categories class
        if (lu.size() == 1) {
            Intent it = new Intent(this, ViewCategories.class);
            User soleUser = lu.get(0);
            it.putExtra(IntentTags.CURRENT_USER, soleUser);
            it.putExtra(IntentTags.CURRENT_DATE, date);
            startActivity(it); // start category activity
        } else {
            Intent intent = new Intent(this, ViewUsers.class);
            startActivity(intent); // start category activity
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_test);

        uSource = new UserDao(this);
        uSource.open();
        date = Calendar.getInstance();
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
        getMenuInflater().inflate(R.menu.menu_launch_test, menu);
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
