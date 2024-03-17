package com.example.ekichabi.ui.main;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.Observable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.ekichabi.localdatabase.BusinessResult;
import com.example.ekichabi.localdatabase.CategoryRow;
import com.example.ekichabi.localdatabase.DistrictRow;
import com.example.ekichabi.localdatabase.LocalDatabase;
import com.example.ekichabi.localdatabase.LocalDatabaseDao;
import com.example.ekichabi.localdatabase.SubcategoryRow;
import com.example.ekichabi.localdatabase.VillageRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * The job of the ViewModel is to manage data used in our various fragments. When an orientation
 * occurs, data in individual fragments may be deleted as the fragment is deleted and recreated, so
 * we use the ViewModel to maintain this data as it stays alive. Note, however, that ViewModel
 * does not persist between GC cleanups.
 */
public class MainViewModel extends AndroidViewModel implements Observable {
    private final String DEBUG_TAG = "MainViewModel";

    public Language currentLang;
    public String currentTab;
    public final MutableLiveData<String> searchString;
    // Filter information for Browsing
    public final MutableLiveData<String> browseDistrictValue;
    public final MutableLiveData<Long> browseDistrictId;
    public final MutableLiveData<String> browseVillageValue;
    public final MutableLiveData<Long> browseVillageId;
    public final MutableLiveData<String> browseSectorValue;
    public final MutableLiveData<Long> browseSectorId;
    public final MutableLiveData<String> browseSubsectorValue;
    public final MutableLiveData<Long> browseSubsectorId;

    // businessId -> BusinessData map for fast access. Sorted in alphabetical order
    private final LinkedHashMap<Integer, BusinessResult> businesses;
    public final MutableLiveData<List<BusinessResult>> businessList;
    private final LocalDatabaseDao databaseDao;

    @NonNull
    public final MutableLiveData<Boolean> isDataLoaded;

    private final Observer<List<BusinessResult>> businessListObserver = new Observer<List<BusinessResult>>() {
        @Override
        public void onChanged(@Nullable List<BusinessResult> list) {
            if (list != null) {
                if (Boolean.FALSE.equals(isDataLoaded.getValue()) && list.size() > 0) {
                    isDataLoaded.setValue(true);
                }
//                Log.i(DEBUG_TAG, "business list changed");
//                Log.i(DEBUG_TAG, "business list length is now: " + list.size());
                // sync up business hash map with database
                for (BusinessResult result : list) {
                    businesses.put((int) result.id, result);
                }
                businessList.setValue(list);
            }
        }
    };

    // methods to call when something changes
    private final Set<OnPropertyChangedCallback> mOnPropertyChangedCallbacks;

    public enum FILTER_TYPES {
        DISTRICT,
        VILLAGE,
        SUBVILLAGE,
        SECTOR,
        SUBSECTOR
    }

    public enum Language {
        ENGLISH,
        SWAHILI;
        public static Language fromString(String code) {
            return new HashMap<String, Language>() {{
                put("en", ENGLISH);
                put("sw", SWAHILI);
            }}.get(code);
        }
    }

    public MainViewModel(Application application) {
        super(application);
        Log.i("info", "MAIN VIEW MODEL CONSTRUCTOR CALLED");
        this.browseDistrictValue = new MutableLiveData<>(null);
        this.browseVillageValue = new MutableLiveData<>(null);
        this.browseSectorValue = new MutableLiveData<>(null);
        this.browseDistrictId = new MutableLiveData<>(null);
        this.browseVillageId = new MutableLiveData<>(null);
        this.browseSectorId = new MutableLiveData<>(null);
        this.searchString = new MutableLiveData<>(null);
        this.browseSubsectorValue = new MutableLiveData<>(null);
        this.browseSubsectorId = new MutableLiveData<>(null);

        this.isDataLoaded = new MutableLiveData<>(false);
        this.databaseDao = LocalDatabase.getDatabase(application).localDatabaseDao();
        Log.i("info", "" + (this.databaseDao == null));
        this.businesses = new LinkedHashMap<>();
        this.businessList = new MutableLiveData<>(new ArrayList<>()); // TODO: change this to default to null so we can have loading scree
        // TODO: Hold onto Language based on native value flag
        SharedPreferences sharedPreferences = this.getApplication().getSharedPreferences("selectedLanguage", Context.MODE_PRIVATE);
        String langCode = sharedPreferences.getString("language", "sw");
        currentLang = Language.fromString(langCode);
        if (currentLang == MainViewModel.Language.ENGLISH) {
            Log.i("info", "eng");
        } else {
            Log.i("info", "swa");
        }
        assert this.databaseDao != null;
        this.databaseDao.getAllBusinesses(currentLang).observeForever(this.businessListObserver);
        this.mOnPropertyChangedCallbacks = new HashSet<>();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        businessList.removeObserver(this.businessListObserver);
    }

    /**
     * Sets a browse filter to a certain value.
     * @param o The value to set the filter too
     * TODO: Modifying certain filters will need to null out later ones
     */
    public void setFilterResult(FilterResult o) {
        Log.i("info", "setFilterResult: " + o.toString());
        switch (o.getType()) {
            case DISTRICT:
                this.browseDistrictId.setValue(o.getId());
                this.browseDistrictValue.setValue(o.getValue());
                if (this.browseVillageValue.getValue() != null) {
                    this.browseVillageId.setValue(null);
                    this.browseVillageValue.setValue(null);
                }
                /*if (this.browseSectorValue.getValue() != null) {
                    this.browseSectorId.setValue(null);
                    this.browseSectorValue.setValue(null);
                }
                if (this.browseSubsectorValue.getValue() != null) {
                    this.browseSubsectorId.setValue(null);
                    this.browseSubsectorValue.setValue(null);
                }*/
                break;
            case VILLAGE:
                this.browseVillageId.setValue(o.getId());
                this.browseVillageValue.setValue(o.getValue());
                /*if (this.browseSectorValue.getValue() != null) {
                    this.browseSectorId.setValue(null);
                    this.browseSectorValue.setValue(null);
                }
                if (this.browseSubsectorValue.getValue() != null) {
                    this.browseSubsectorId.setValue(null);
                    this.browseSubsectorValue.setValue(null);
                }*/
                break;
            case SECTOR:
                this.browseSectorId.setValue(o.getId());
                this.browseSectorValue.setValue(o.getValue());
                if (this.browseSubsectorValue.getValue() != null) {
                    this.browseSubsectorId.setValue(null);
                    this.browseSubsectorValue.setValue(null);
                }
                break;
            case SUBSECTOR:
                this.browseSubsectorId.setValue(o.getId());
                this.browseSubsectorValue.setValue(o.getValue());
                break;
        }
    }

    public LiveData<List<DistrictRow>> getDistricts() {
        return this.databaseDao.getAllDistricts();
    }

    public LiveData<List<VillageRow>> getVillages() {
        if (this.browseDistrictValue.getValue() != null) {
            return this.databaseDao.getVillagesInDistrict(this.browseDistrictId.getValue());
        } else {
            return this.databaseDao.getAllVillages();
        }
    }

    public LiveData<List<CategoryRow>> getSectors() {
        return this.databaseDao.getAllCategories();
    }

    public LiveData<List<SubcategoryRow>> getSubsectors() {
        if (this.browseSectorValue.getValue() != null) {
            return this.databaseDao.getSubcategoryInCategory(this.browseSectorId.getValue());
        } else {
            return this.databaseDao.getAllSubcategories();
        }
    }

    public LiveData<String> getNameFromId(long id) {
        return this.databaseDao.getCategoryNameById(id, currentLang);
    }

    public LiveData<Long> getIdFromName(String name) {
        return this.databaseDao.getIdByCategoryName(name, currentLang);
    }


    /**
     * Get all businesses in the database
     * @return list of business data sorted in alphabetical order by name
     */
    @Nullable
    public List<BusinessResult> getAllBusinesses() {
        if (businessList != null && businessList.getValue() != null) {
            return businessList.getValue();
        }
        List<BusinessResult> businessData = new ArrayList<>();
        for (int businessId : this.businesses.keySet()) {
            BusinessResult business = this.businesses.get(businessId);
            if (business.name != null) {
                businessData.add(business);
            }
        }
        return businessData;
    }

    /**
     * Get the user's favorite businesses
     * @return list of favorite business' data sorted in alphabetical order by name
     */
    public List<BusinessResult> getFavoriteBusinesses() {
        List<BusinessResult> favoriteBusinesses = new ArrayList<>();
        for (int businessId : this.businesses.keySet()) {
            BusinessResult business = this.businesses.get(businessId);
            if (business.name != null && business.isFavorite) {
                favoriteBusinesses.add(business);
            }
        }
        Log.i(DEBUG_TAG, "favorite businesses length: " + favoriteBusinesses.size());
        return favoriteBusinesses;
    }

    /**
     * Mark a business as favorite
     * @param businessId ID of business to mark
     * @param favorite true to mark as favorite, false to remove from favorites
     */
    public void markFavorite(int businessId, boolean favorite) {
        if (this.businesses.containsKey(businessId)) {
            Objects.requireNonNull(this.businesses.get(businessId)).isFavorite = favorite; // optimistic update
            // no need to insert since favorites table is pre-populated with all businesses
            this.databaseDao.updateFavorite(businessId, favorite);

            // TODO: implement new interface for the onPropertyChangedCallback that takes in a long instead of int
            for (Observable.OnPropertyChangedCallback callback : this.mOnPropertyChangedCallbacks) {
                Log.i(DEBUG_TAG, "Call callback");
                callback.onPropertyChanged(this, businessId);
            }
        }
    }

    @Override
    public void addOnPropertyChangedCallback(Observable.OnPropertyChangedCallback callback) {
        // TODO: implement check here to make sure callbacks all extend some type (propertyID = businessID)
        Log.i(DEBUG_TAG, "Add callback");
        mOnPropertyChangedCallbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(Observable.OnPropertyChangedCallback callback) {
        mOnPropertyChangedCallbacks.remove(callback);
    }

    public void removeLastCallback() {
        if (!mOnPropertyChangedCallbacks.isEmpty()) {
            mOnPropertyChangedCallbacks.remove(((TreeSet) mOnPropertyChangedCallbacks).last());
        }
    }

    public Bundle createBundle(Bundle out) {
        out.putString("district", this.browseDistrictValue.getValue());
        out.putString("village", this.browseVillageValue.getValue());
        out.putString("sector", this.browseSectorValue.getValue());
        out.putLong("district_id", this.browseDistrictId.getValue() == null ? -1 : this.browseDistrictId.getValue());
        out.putLong("village_id", this.browseVillageId.getValue() == null ? -1 : this.browseVillageId.getValue());
        out.putLong("sector_id", this.browseSectorId.getValue() == null ? -1 : this.browseSectorId.getValue());
        out.putString("search", this.searchString.getValue());
        out.putString("tab", this.currentTab);
        return out;
    }
}