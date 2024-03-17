package com.example.ekichabi;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.ekichabi.localdatabase.BusinessResult;
import com.example.temp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;


/**
 * Class containing the logic for interfacing between the
 * Browse fragments and the SQLite database.
 */
public class QueryHandler {


    public static class BusinessData {
        private String name;
        private String phoneNumber;
        private String district;
        private String village;
        private String subvillage;
        private String sector;
        private String subsector_sw;
        private String subsector_en;
        private boolean favorite;
        private int businessId;
        private String owner;


        public BusinessData(JSONObject json) throws JSONException {
            this.businessId = json.getInt("pk");
            json = (JSONObject )json.getJSONObject("fields");
            this.name = json.getString("name");
            this.phoneNumber = json.getString("number1");
            this.subvillage = json.getString("subvillage");
            this.district = json.getString("district");
            this.village = json.getString("village");
            this.sector = json.getString("category");
            this.subsector_sw = json.getString("subsector1_sw");
            this.subsector_en = json.getString("subsector1_en");
            if (this.subsector_en.equals("Other")) {
                this.subsector_en = this.subsector_sw;
            }
            this.owner = json.getString("owner");
            this.favorite = false;
        }

        public String name() {
            return this.name;
        }

        public String phoneNumber() {
            return this.phoneNumber;
        }

        public String district() {
            return this.district;
        }

        public String village() {
            return this.village;
        }

        public String subvillage() {
            return this.subvillage;
        }

        public String sector() {
            return this.sector;
        }

        public String subsector() {
            return this.subsector_en;
        }

        public String subsector_sw() {
            return this.subsector_sw;
        }

        public String owner() {
            return this.owner;
        }

        public boolean isFavorite() {
            return this.favorite;
        }

        public void setFavorite(boolean favorite) {
            this.favorite = favorite;
        }

        public int getBusinessId() {
            return this.businessId;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof BusinessData) {
                return this.businessId == ((BusinessData) other).businessId;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.businessId;
        }
    }

    private static List<String[]> directory;

    public static void parseCSV(Context context) {
        directory = new ArrayList<>();

        InputStream is = context.getResources().openRawResource(R.raw.census_data_trimmed);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8")));
        String line = "";
        try {
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                for (int i = 0; i < tokens.length; i++) {
                    tokens[i] = tokens[i].replaceAll("^\"|\"$", "");
                }
                directory.add(tokens);
            }
            directory.remove(0); // Remove header row
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }


    /**
     * Filters the given list of businesses by district
     * @param businesses list of businesses to filter
     * @param district district of desired businesses
     * @return a new list of businesses which match the given district
     */
    public static List<BusinessResult> filterByDistrict(List<BusinessResult> businesses, @NonNull String district) {
        List<BusinessResult> filtered = new ArrayList<>();
        for (BusinessResult businessData : businesses) {
            if (businessData.district.equals(district)) {
                filtered.add(businessData);
            }
        }
        return filtered;
    }

    /**
     * Filters the given list of businesses by village
     * @param businesses list of businesses to filter
     * @param village village of desired businesses
     * @return a new list of businesses which match the given village
     */
    public static List<BusinessResult> filterByVillage(List<BusinessResult> businesses, @NonNull String village) {
        List<BusinessResult> filtered = new ArrayList<>();
        for (BusinessResult businessData : businesses) {
            if (businessData.village.equals(village)) {
                filtered.add(businessData);
            }
        }
        return filtered;
    }

    /**
     * Filters the given list of businesses by sector
     * @param businesses list of businesses to filter
     * @param sector sector of desired businesses
     * @return a new list of businesses which match the given sector
     */
    public static List<BusinessResult> filterBySector(List<BusinessResult> businesses, @NonNull String sector) {
        List<BusinessResult> filtered = new ArrayList<>();
        for (BusinessResult businessData : businesses) {
            if (businessData.category.equals(sector)) {
                filtered.add(businessData);
            }
        }
        return filtered;
    }

    public static List<BusinessResult> filterBySubsector(List<BusinessResult> businesses, @NonNull String subsector) {
        List<BusinessResult> filtered = new ArrayList<>();
        for (BusinessResult businessData : businesses) {
            if (businessData.subcategory != null && businessData.subcategory.equals(subsector)) {
                filtered.add(businessData);
            }
        }
        return filtered;
    }


}
