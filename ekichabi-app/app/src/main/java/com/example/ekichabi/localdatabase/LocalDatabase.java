package com.example.ekichabi.localdatabase;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.ekichabi.QueryHandler;
import com.example.temp.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {BusinessRow.class,
        VillageRow.class,
        DistrictRow.class,
        CategoryRow.class,
        SubcategoryRow.class,
        SubvillageRow.class},
        version = 1, exportSchema = false)
public abstract class LocalDatabase extends RoomDatabase {
    private static final String DEBUG_TAG = "LocalDB/";
//    private static final String SERVER_URL = "https://SECRET.pythonanywhere.com/";
//    private static final long UPDATE_PERIOD = 24;
    public abstract LocalDatabaseDao localDatabaseDao();

    private static volatile LocalDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private static String lastUpdate = "";
    private static String temp = "";
    private static volatile Context context1;
    private static Boolean getData = false;

    public static LocalDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (LocalDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    LocalDatabase.class, "local_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                    INSTANCE.query("select 1", null);
                }
            }
        }
        context1 = context;
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Log.i("info", "DATABASE POPULATING");
            databaseWriteExecutor.execute(() -> {
                populateDatabase("business");
            });
            lastUpdate = getUpdateDate();
        }
    };

    public static JSONArray makeBusinessGetRequest(String endpoint) throws Exception {
        //URL url = new URL(SERVER_URL + endpoint);
        URL url = new URL("https://SECRET.pythonanywhere.com/business/");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setInstanceFollowRedirects(false);
        con.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty( "charset", "utf-8");
        con.setDoOutput(true);
        String jsonInputString = "username=SECRET&password=SECRET";
        try {
            SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(context1.getApplicationContext());
            String number = preferences.getString("mphoneNum", null);
            if (number != null) {
                jsonInputString += "&number=" + number;
            }
        } catch (Exception e) {
            Log.i("makeBusinessGetRequest", "Failed to get number");
        }
        try(OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        if (con.getResponseCode() < 299) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String json = reader.readLine();
            Log.i("makeBusinessGetRequest", "HTTP Request Status: " + con.getResponseCode());
            return new JSONArray(json);
        } else {
//            BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(con.getInputStream()));
//            String json = reader.readLine();
            Log.i("makeBusinessGetRequest", "HTTP Request Status: " + con.getResponseCode());
            return new JSONArray("[]");
        }
    }

    public static void populateDatabase(String apiEndpoint) {
        Log.i(DEBUG_TAG, "Populating Database from Endpoint: " + apiEndpoint);
        LocalDatabaseDao dao = INSTANCE.localDatabaseDao();
        JSONArray businesses;
        try {
            businesses = makeBusinessGetRequest(apiEndpoint);
        } catch (Exception e) {
            Log.e(DEBUG_TAG, "populate" + e.getLocalizedMessage());
            return;
        }
        Map<String, Set<String>> categoryToSubcategory = new HashMap<>();
        Map<String, String> translation = new HashMap<>();
        Map<String, Map<String, Set<String>>> locales = new HashMap<>();
        Log.i(DEBUG_TAG, "array: " + businesses);
        for (int i = 0; i < businesses.length(); i++) {
            QueryHandler.BusinessData business;

            try {
                business = new QueryHandler.BusinessData(businesses.getJSONObject(i));
            } catch (JSONException e) {
                Log.e(DEBUG_TAG, "for" + e.getMessage());
                continue;
            }
            if (!categoryToSubcategory.containsKey(business.sector())) {
                categoryToSubcategory.put(business.sector(), new HashSet<>());
            }
            if (!locales.containsKey((business.district()))) {
                locales.put(business.district(), new HashMap<>());
            }
            if (!Objects.requireNonNull(locales.get(business.district())).containsKey(business.village())) {
                Objects.requireNonNull(locales.get(business.district())).put(business.village(), new HashSet<>());
            }
            Objects.requireNonNull(categoryToSubcategory.get(business.sector())).add(business.subsector());
            translation.put(business.subsector(), business.subsector_sw());
            Objects.requireNonNull(Objects.requireNonNull(locales.get(business.district())).get(business.village())).add(business.subvillage());
            // use prefix of 0 for calling within Tanzania, +255 for calling from outside Tanzania
            if (business.phoneNumber() != null && !Objects.equals(business.phoneNumber(), "")) {
                dao.insertBusiness(new BusinessRow(business.getBusinessId(),
                        business.name(), "0,0",
                        business.sector().hashCode(),
                        Objects.hash(business.subsector(), business.sector()),
                        business.district().hashCode(),
                        business.village().hashCode(),
                        Objects.hash(business.subvillage(), business.village()),
                        "english_description",
                        "swahili_description",
                        "0" + business.phoneNumber(),
                        false, business.owner()));
            }
        }
        for (String district : locales.keySet()) {
            dao.insertDistrict(new DistrictRow(district.hashCode(), district, "0,0"));
            for (String village : Objects.requireNonNull(locales.get(district)).keySet()) {
                for (String subvillage : Objects.requireNonNull(Objects.requireNonNull(locales.get(district)).get(village))) {
                    dao.insertSubvillage(new SubvillageRow(
                            Objects.hash(subvillage, village),
                            subvillage, village.hashCode(),
                            "0,0"));
                }
                dao.insertVillage(new VillageRow(village.hashCode(), village, district.hashCode(), "0,0"));
            }
        }

        Map<String, String> categoryTranslation = new HashMap<String, String>();
        InputStream is = context1.getResources().openRawResource(R.raw.category);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8")));
        String line = "";
        try {
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                categoryTranslation.put(tokens[0], tokens[1]);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }


        for (String category : categoryToSubcategory.keySet()) {
            dao.insertCategory(new CategoryRow(category.hashCode(), category,categoryTranslation.get(category) == null ? "sw_" + category : categoryTranslation.get(category)));
            for (String subcategory : categoryToSubcategory.get(category)) {
                if (subcategory == null) {
                    dao.insertSubcategory(new SubcategoryRow(
                            Objects.hash(subcategory, category),
                            subcategory,
                            "",
                            category.hashCode()));
                } else {
                    dao.insertSubcategory(new SubcategoryRow(
                            Objects.hash(subcategory, category),
                            subcategory,
                            translation.get(subcategory),
                            category.hashCode()));
                }
            }
        }


    }

    public static String getUpdateDate() {
        //URL url = new URL(SERVER_URL + endpoint);
        getData = false;
        new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://SECRET.pythonanywhere.com/date/");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    if (con.getResponseCode() < 299) {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(con.getInputStream()));
                        temp = reader.readLine();
                    } else {
                        Log.e(DEBUG_TAG, "HTTP Request Status: " + con.getResponseCode());
                        temp = "";
                    }
                } catch (Exception e) {

                }
                getData = true;
            }
        }.start();

//        while (!getData) {
//        }
        return temp;
    }

    public static class LocalDatabaseUpdateJobService extends JobService {
        @Override
        public boolean onStartJob(JobParameters jobParameters) {
            String date = "";
            date = getUpdateDate();
            String finalDate = date;
            if (!date.equals(lastUpdate)) {
                databaseWriteExecutor.execute(() -> {
                    populateDatabase("business");
                });
                lastUpdate = finalDate;
            }
            return false;
        }

        @Override
        public boolean onStopJob(JobParameters jobParameters) {
            return false;
        }
    }

    public static final class BusinessTable {
        public static final String TABLE_NAME = "businesses";

        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String COORDS = "coordinates";
        public static final String CATEGORY = "category";
        public static final String SUBCATEGORY = "subcategory";
        public static final String DISTRICT = "district";
        public static final String VILLAGE = "village";
        public static final String SUBVILLAGE = "subvillage";
        public static final String ENGLISH_DESCRIPTION = "englishDescription";
        public static final String SWAHILI_DESCRIPTION = "swahiliDescription";
        public static final String PHONE = "phone";
        public static final String IS_FAVORITE = "isFavorite";
        public static final String OWNER = "owner";
    }

    public static final class VillageTable {
        public static final String TABLE_NAME = "villages";

        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String DISTRICT = "district";
        public static final String COORDS = "coordinates";
    }

    public static final class DistrictTable {
        public static final String TABLE_NAME = "districts";

        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String COORDS = "coordinates";
    }

    public static final class CategoryTable {
        public static final String TABLE_NAME = "categories";

        public static final String ID = "id";
        public static final String ENGLISH_NAME = "englishName";
        public static final String SWAHILI_NAME = "swahiliName";
    }

    public static final class SubcategoryTable {
        public static final String TABLE_NAME = "subcategories";

        public static final String ID = "id";
        public static final String ENGLISH_NAME = "englishName";
        public static final String SWAHILI_NAME = "swahiliName";
        public static final String CATEGORY = "category";
    }

    public static final class SubvillageTable {
        public static final String TABLE_NAME = "subvillages";

        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String VILLAGE = "village";
        public static final String COORDS = "coordinates";
    }
}
