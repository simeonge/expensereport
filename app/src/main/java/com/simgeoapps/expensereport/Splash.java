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
     * Called during splash screen. Starts the ViewUsers activity.
     */
    public void begin() {
        // here pass current month and year, and default/only user, and
        UserDao uSource = new UserDao(this);
        uSource.open();
        List<User> lu = uSource.getAllUsers();

        // if we have only one user, specify it in config, then go directly to categories class
        if (lu.size() == 1) {
            Intent it = new Intent(this, ViewCategories.class);
            User soleUser = lu.get(0);
            GlobalConfig.setCurrentUser(soleUser); // set default user if only one
            GlobalConfig.setDate(Calendar.getInstance()); // use current date by default
            startActivity(it); // start category activity
        } else {
            Intent intent = new Intent(this, ViewUsers.class);
            GlobalConfig.setDate(Calendar.getInstance()); // use current date by default
            startActivity(intent); // start user activity
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
        }, 1000);

    }

}
