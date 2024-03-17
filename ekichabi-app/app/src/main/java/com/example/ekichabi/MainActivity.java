package com.example.ekichabi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.lifecycle.Observer;

import com.example.ekichabi.ui.main.AndroidLogger;
import com.example.ekichabi.ui.main.Authentication;
import com.example.ekichabi.ui.main.BrowseTabFragment;
import com.example.ekichabi.localdatabase.LocalDatabase;
import com.example.ekichabi.ui.main.Demo;
import com.example.ekichabi.ui.main.FavoritesTabFragment;
import com.example.ekichabi.ui.main.HelpFragment;
import com.example.ekichabi.ui.main.MainViewModel;
import com.example.ekichabi.ui.main.SearchTabFragment;
import com.example.ekichabi.ui.main.SettingsFragment;
import com.example.ekichabi.ui.main.TextSize;
import com.example.temp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * The starter activity that will store all of our various fragments.
 */
public class MainActivity extends AppCompatActivity {
    public static final String DEBUG_TAG = "MainActivity";
    public static final long DB_UPDATE_PERIOD_HOURS = 72;
    public static final long DB_UPDATE_RANDOM_INTERVAL = DB_UPDATE_PERIOD_HOURS / 10;
    private MainViewModel mViewModel;
    private BottomNavigationView mBottomNavigationView;
    private final Random r = new Random();


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("getUniqueInfo()", "success");
        Log.i(DEBUG_TAG, "MainActivity onCreate called");

        // Get the font size preference and set it
        SharedPreferences sharedPreferences = this.getSharedPreferences("selectedTextSize", Context.MODE_PRIVATE);
        Log.i("settings", "text size " + sharedPreferences.getInt("size", TextSize.Large.resId));
        getTheme().applyStyle(sharedPreferences.getInt("size", TextSize.Large.resId), true);
        setContentView(R.layout.main_activity);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        try {
            QueryHandler.parseCSV(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Load main ViewModel and initialize its fields
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        mBottomNavigationView = findViewById(R.id.bottom_nav_view);
        mBottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_first_item:
                        switchToBrowseTab();
                        break;
                    case R.id.action_second_item:
                        switchToSearchTab();
                        break;
                    case R.id.action_third_item:
                        switchToFavoritesTab();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
        mBottomNavigationView.setSelectedItemId(R.id.action_first_item);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            // Notice that we can't store null as a long, so we use -1 instead. It's technically
            // possible for -1 to be a valid id, but very unlikely. Perhaps store a string
            // representation in the future?
            this.mViewModel.browseDistrictId.setValue(b.getLong("district_id") == -1 ? null : b.getLong("district_id"));
            this.mViewModel.browseDistrictValue.setValue(b.getString("district"));
            this.mViewModel.browseVillageId.setValue(b.getLong("village_id") == -1 ? null : b.getLong("village_id"));
            this.mViewModel.browseVillageValue.setValue(b.getString("village"));
            // In the case of the sector, it might need to be translated, so we should get the new value via lookup
            this.mViewModel.browseSectorId.setValue(b.getLong("sector_id") == -1 ? null : b.getLong("sector_id"));
            if (this.mViewModel.browseSectorId.getValue() != null) {
                mViewModel.getNameFromId(this.mViewModel.browseSectorId.getValue()).observeForever(new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        mViewModel.browseSectorValue.setValue(s);
                    }
                });
            }
            this.mViewModel.searchString.setValue(b.getString("search"));
            this.mViewModel.currentTab = b.getString("tab");
            switch (this.mViewModel.currentTab) {
                case ("search"):
                    mBottomNavigationView.setSelectedItemId(R.id.action_second_item);
                    break;
                case ("favorites"):
                    mBottomNavigationView.setSelectedItemId(R.id.action_third_item);
                    break;
                default:
                    break;
            }
        }
        Log.i("mainactivity onCreate()", sharedPreferences.getString("mphoneNum", "-1"));
        scheduleUpdateLogsJob();
        scheduleUpdateDatabaseJob();

        // Load fragment
        if (savedInstanceState == null) {
            switchToBrowseTab();
        }

        SharedPreferences pref = getSharedPreferences("mypref", MODE_PRIVATE);

        if (pref.getBoolean("firststart", true)) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("firststart", false);
            editor.commit();
            Demo demo = Demo.newInstance(this);
            demo.show(getSupportFragmentManager(), "demo");
        }
    }

    public void refreshApp() {
        Toast.makeText(getApplicationContext(), getText(R.string.refresh), Toast.LENGTH_SHORT).show();
        this.mViewModel.currentTab = "browse";
        Log.i("settings", "refresh app");
        Intent refresh = new Intent(MainActivity.this, MainActivity.class);
        refresh.putExtras(this.mViewModel.createBundle(new Bundle()));
        finish();
        startActivity(refresh);
        overridePendingTransition(0, 0);
        Toast.makeText(getApplicationContext(), getText(R.string.finished), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("selectedLanguage", Context.MODE_PRIVATE);
        String pine = sharedPreferences.getString("language", "sw");
        String languageToLoad = pine;
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        this.getResources().updateConfiguration(config, this.getResources().getDisplayMetrics());
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.action_settings):
                Log.i("tag", "settings buttons pressed");
                SettingsFragment settingsFragment = SettingsFragment.newInstance(this);
                settingsFragment.show(getSupportFragmentManager(), "settings_fragment");
                return true;
            case (R.id.action_help):
                Log.i("tag", "help button pressed");
                HelpFragment helpFragment = HelpFragment.newInstance(this);
                helpFragment.show(getSupportFragmentManager(), "help_fragment");
            default:
                return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return true;
    }

    private void switchToFavoritesTab() {
        // TODO: maybe instead of creating a new instance here, save fragment in a member variable and just update its args?
        this.mViewModel.currentTab = "favorites";
        FavoritesTabFragment fragment = FavoritesTabFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.main_display, fragment, "main_fragment")
                .commitNow();
    }

    private void switchToSearchTab() {
        this.mViewModel.currentTab = "search";
        // TODO: maybe instead of creating a new instance here, save fragment in a member variable and just update its args?
        SearchTabFragment fragment = SearchTabFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.main_display, fragment, "main_fragment")
                .commitNow();
    }

    private void switchToBrowseTab() {
        this.mViewModel.currentTab = "browse";
        // TODO: maybe instead of creating a new instance here, save fragment in a member variable and just update its args?
        BrowseTabFragment fragment = BrowseTabFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.main_display, fragment, "main_fragment")
                .commitNow();
    }

    private void scheduleUpdateDatabaseJob() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (!preferences.getBoolean("firstRunComplete", false)) {
            long randomHours = DB_UPDATE_PERIOD_HOURS - DB_UPDATE_RANDOM_INTERVAL
                    + (long)(r.nextInt(2 * (int)(DB_UPDATE_RANDOM_INTERVAL)));
            Log.i(DEBUG_TAG, "random hours is : " + randomHours);
            Log.i(DEBUG_TAG, "Scheduling Update Database Job");
            JobScheduler jobScheduler = (JobScheduler) getApplicationContext()
                    .getSystemService(JOB_SCHEDULER_SERVICE);
            ComponentName name = new ComponentName(this,
                    LocalDatabase.LocalDatabaseUpdateJobService.class);
            JobInfo jobInfo = new JobInfo.Builder(1, name)
                    .setPeriodic(randomHours * 3600000)
                    .setPersisted(true).build();
            jobScheduler.schedule(jobInfo);
            Log.i(DEBUG_TAG, "Update Database Job Scheduled");

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("firstRunComplete", true);
            editor.commit();
        }
    }

    private void scheduleUpdateLogsJob() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (!preferences.getBoolean("firstLogUpdateComplete", false)) {
            long randomPeriod = (long) (0.5 - 0.25 + (r.nextInt(50) / 100.0));
            Log.i("scheduleUpdateLogsJob()", "random period is : " + randomPeriod * 3600000);
            Log.i("scheduleUpdateLogsJob()", "Scheduling Update Log Job");
            JobScheduler jobScheduler = (JobScheduler) getApplicationContext()
                    .getSystemService(JOB_SCHEDULER_SERVICE);
            ComponentName name = new ComponentName(this,
                    Authentication.LogUpdateJobService.class);
            JobInfo jobInfo = new JobInfo.Builder(0, name)
                    .setPeriodic(20*1000)
                    .setPersisted(true).build();
            jobScheduler.schedule(jobInfo);
            Log.i("scheduleUpdateLogsJob()", "Update Log Job Scheduled");

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("firstLogUpdateComplete", true);
            editor.commit();
        }
    }
}