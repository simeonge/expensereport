package com.simgeoapps.expensereport;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Launcher activity.
 */
public class Splash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // handler to start the either users or categories activity and close after one second
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                GlobalConfig gc = (GlobalConfig) getApplication();
                // if there are 0 or more than one users, start user activity to allow selection
                if (gc.getCurrentUser() == null) {
                    Intent it = new Intent(Splash.this, ViewUsers.class);
                    startActivity(it);
                    overridePendingTransition(0,0); // no animation
                } else {
                    Intent it = new Intent(Splash.this, ViewCategories.class);
                    startActivity(it);
                    overridePendingTransition(0,0); // no animation
                }
                Splash.this.finish();
            }
        }, 1000);

    }

}
