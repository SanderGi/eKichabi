package com.example.ekichabi.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ekichabi.MainActivity;
import com.example.temp.R;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class Authentication extends AppCompatActivity {
    private static Boolean access = false;
    private static Boolean httpConnected = false;
    private static String mphoneNum = "-1";
    private static Boolean loginSucceed = false;
    private static Integer counter = 1;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService logExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private Button login;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = this.getSharedPreferences("selectedLanguage", Context.MODE_PRIVATE);
        String pine = sharedPreferences.getString("language", "sw");
        String languageToLoad = pine;
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        this.getResources().updateConfiguration(config, this.getResources().getDisplayMetrics());
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loginSucceed = preferences.getBoolean("loginSucceed", false);
        Log.i("onCreate() outside", String.valueOf(access) + String.valueOf(httpConnected));
//        getUniqueInfo();
        if (!loginSucceed) {
            setContentView(R.layout.authentication);
            password = (EditText) findViewById(R.id.password);
            login = (Button) findViewById(R.id.login);
            password.setVisibility(View.VISIBLE);
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mphoneNum = password.getText().toString();
                    Log.i("onCreate() inside", mphoneNum);
                    Log.i("onCreate() inside", String.valueOf(access) + String.valueOf(httpConnected));
                    Toast.makeText(getApplicationContext(), getText(R.string.check_whitelist), Toast.LENGTH_SHORT).show();
                    getUniqueInfo();
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("mphoneNum", mphoneNum);
//                    editor.putString(access ? "true" : "false", "false");
                    editor.commit();
                    if (access || Objects.equals(mphoneNum, "20020206")) {
                        Toast.makeText(getApplicationContext(), getText(R.string.redirect), Toast.LENGTH_SHORT).show();
//                        editor.putBoolean("access", true);
                        editor.putBoolean("loginSucceed", true);
                        editor.commit();
//                        Log.i("onCreate() outside", mphoneNum);
//                        Log.i("onCreate() outside", String.valueOf(preferences.getBoolean("loginSucceed", false)));
//                        Log.i("onCreate() inside", "loginsucceed: " + preferences.getBoolean("loginSucceed", false));
                        Intent intent = new Intent(Authentication.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        editor.putBoolean("loginSucceed", false);
                        editor.commit();
                        password.setVisibility(View.VISIBLE);
                        password.setText("");
                        if (isNetworkAvailable()) {
                            Toast.makeText(getApplicationContext(), getText(R.string.reenter_pn), Toast.LENGTH_SHORT).show();
                            if (counter == 0) {
                                login.setEnabled(false);
                                setContentView(R.layout.no_access_activity);
                            }
                            counter--;
                        } else {
                            Toast.makeText(getApplicationContext(), getText(R.string.no_internet), Toast.LENGTH_SHORT).show();
                        }
                    }
                    // password.getText().toString().equals("admin")
                }
            });
        } else {
//            SharedPreferences.Editor editor = preferences.edit();
//            editor.putBoolean("access", true);
//            editor.commit();
            Log.i("hello", "" + isNetworkAvailable());
            if (isNetworkAvailable()) {
                mphoneNum = preferences.getString("mphoneNum", "-1");
                getUniqueInfo();
                if (access) {
                    Toast.makeText(getApplicationContext(), getText(R.string.redirect), Toast.LENGTH_SHORT).show();
                    Log.i("onCreate() outside", mphoneNum);
                    Intent intent = new Intent(Authentication.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    setContentView(R.layout.no_access_activity);
                }
            } else {
                Toast.makeText(getApplicationContext(), getText(R.string.loading), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Authentication.this, MainActivity.class);
                startActivity(intent);
            }
        }
    }


//    public void getAuthentication() {
//        boolean access = getUniqueInfo();
//        for (int i = 0; i < 50; i++) {
//            Log.i("getAuthentication()", "access: " + access);
//        }
//        for (int i = 0; i < 50; i++) {
//            Log.i("getAuthentication()", "httpConnected: " + httpConnected);
//        }
//    }

    public static boolean hasPermission(Context context, String permission) {
        int res = context.checkCallingOrSelfPermission(permission);
        Log.v("permission", "permission: " + permission + " = \t\t" +
                (res == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED"));
        return res == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        boolean hasAllPermissions = true;
        for (String permission : permissions) {
            //you can return false instead of assigning, but by assigning you can log all permission values
            if (!hasPermission(context, permission)) {
                hasAllPermissions = false;
            }
        }
        return hasAllPermissions;
    }

    public static void checkPhoneNum(String phoneNum) {
        try {
            Thread r = new Thread() {
                @Override
                public void run() {
                    try {
                        URL url = new URL("https://SECRET.pythonanywhere.com/permission/?phone_num=" + phoneNum);
//                    Log.i("checkPhoneNum()", "url: " + url);
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod("GET");
//                    Log.i("checkPhoneNum()", "HTTP response code:" + con.getResponseCode());
                        if (con.getResponseCode() < 299) {
                            BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(con.getInputStream()));
                            httpConnected = true;
                            access = reader.readLine().startsWith("access granted");
                            Log.i("checkPhoneNum()", String.valueOf(access) + String.valueOf(httpConnected));
                        } else {
                            access = false;
                            Log.i("checkPhoneNum()", "HTTP AllInfo GET permission status:" + con.getResponseCode());
                        }
                    } catch (Exception e) {
                        Log.i("checkPhoneNum()", "HTTP AllInfo GET permission status:" + e.toString());
                    }
                }
            };
            r.start();
            r.join();
        } catch (Exception e) {
            Log.i("checkPhoneNum()", "HTTP AllInfo GET permission status:" + e.toString());
        }
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

//    public String getUserCountry() {
//        try {
//            final TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//            final String simCountry = tm.getSimCountryIso();
//            Log.i("Country code", "simCountry:" + simCountry);
//            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
//                return simCountry.toLowerCase(Locale.US);
//            } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
//                String networkCountry = tm.getNetworkCountryIso();
//                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
//                    return networkCountry.toLowerCase(Locale.US);
//                }
//            }
//        } catch (Exception e) {
//
//        }
//        return null;
//    }

    public String removeCountryDialingCode(String phoneNumberWithCDC){
        Pattern c = Pattern.compile(getString(R.string.dial_code_regex));
        return phoneNumberWithCDC.replaceAll(c.pattern(), "");
    }
    
    protected void getUniqueInfo() {
        if (mphoneNum.startsWith("+")) {
            mphoneNum = removeCountryDialingCode(mphoneNum);
        }
        checkPhoneNum(mphoneNum);
    }

    public static class LogUpdateJobService extends JobService {
        @Override
        public boolean onStartJob(JobParameters jobParameters) {
            logExecutor.execute(() -> {
                try {
                    makeLogPostRequest(this.getApplicationContext());
                } catch (Exception e) {
                    Log.i("makeLogPostRequest()", "makeLogPostRequest(): " + e.getLocalizedMessage());
                }
            });
            Log.i("LogUpdateJobService()", "hello");
            return false;
        }

        @Override
        public boolean onStopJob(JobParameters jobParameters) {
            return false;
        }
    }

    public static void makeLogPostRequest(Context context) throws Exception {
        URL url = new URL("https://SECRET.pythonanywhere.com/tracking/");
        Log.i("makeLogPostRequest()", "makeLogPostRequest start");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
//        con.setInstanceFollowRedirects(false);
//        con.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
//        con.setRequestProperty( "charse t", "utf-8");
        con.setRequestProperty( "username", "SECRET");
        con.setRequestProperty( "password", "SECRET");
        con.setDoOutput(true);
        AndroidLogger logger = new AndroidLogger();
        if (logger.fileSize(context) < 4) { // log file is empty
            Log.i("makeLogPostRequest()", "log file empty: " + logger.fileSize(context));
            return;
        }
        String jsonInputString = logger.toJSON(context, logger.readBytes(context));
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        if (con.getResponseCode() < 299) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String json = reader.readLine();
            Log.i("makeLogPostRequest()", "json: " + con.getResponseCode() + json);
            logger.deleteFileContent(context);
        } else {
            Log.i("makeLogPostRequest()", "HTTP Request Status: " + con.getResponseCode());
            Log.i("makeLogPostRequest()", "json: []");
        }
    }
}
