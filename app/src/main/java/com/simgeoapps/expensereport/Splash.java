package com.simgeoapps.expensereport;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Launcher activity.
 * @author Simeon
 */
public class Splash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                GlobalConfig gc = (GlobalConfig) getApplication();
                if (gc.getCurrentUser() == null) {
                    Intent it = new Intent(Splash.this, ViewUsers.class);
                    startActivity(it);
                    overridePendingTransition(0,0);
                } else {
                    Intent it = new Intent(Splash.this, ViewCategories.class);
                    startActivity(it);
                    overridePendingTransition(0,0);
                }
                Splash.this.finish();
            }
        }, 1000);

    }

}
