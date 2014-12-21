package com.simgeoapps.expensereport;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import java.util.Calendar;
import java.util.List;

/**
 * Test Launcher activity.
 * @author Simeon
 */
public class Splash extends Activity {

    /**
     * Called when the begin button is clicked. Starts the ViewUsers activity.
     */
    public void begin() {
        // here pass current month and year, and default/only user, and
        UserDao uSource = new UserDao(this);
        uSource.open();
        List<User> lu = uSource.getAllUsers();
        Calendar date = Calendar.getInstance();

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

        uSource.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                begin();
                Splash.this.finish();
            }
        }, 2000);

    }

}
