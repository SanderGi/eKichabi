package com.example.ekichabi.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.Observable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ekichabi.localdatabase.BusinessResult;
import com.example.temp.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The default fragment. The primary point of this Fragment is give ways to give options to filter
 * and to display a list of choices
 */
public class BrowseTabFragment extends RecyclerViewTabFragment {
    private static final String DEBUG_TAG = "BrowseTabFragment";
    private ConstraintLayout mDistrictLayout;
    private ConstraintLayout mVillageLayout;
    private ConstraintLayout mSectorLayout;
    private ConstraintLayout msubSectorLayout;
    private boolean noBusiness;

    public static BrowseTabFragment newInstance() {
        return new BrowseTabFragment();
    }

    public void writeSActionCombs(String s, boolean success) {
        AndroidLogger logger = new AndroidLogger();
//                    strSuccessCombs.add(createEntry(mViewModel.searchString.getValue(), true));
        Log.i("writeSActionCombs", "Attempted logging filterAction");
        try {
            String str = s.toLowerCase().replaceAll("[^a-zA-Z]", "");
            if (str != null && !str.isEmpty()) {
                logger.writeBytesToFile(logger.getFilterAction(str, success, s.matches("[a-z]+")), requireContext());
            }
            Log.i("logger content", "filter action:" + logger.toJSON(requireContext(), logger.getFilterAction(str, success, s.matches("[a-z]+"))));
            Log.i("logger content", "content:" + logger.toJSON(requireContext(), logger.readBytes(requireContext())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mBusinessListDisplayAdapter.updateActionCombs();
    }

    public Observer<String> observeDistrictFilterChange = new Observer<String>() {
        private boolean ignoreNextChange = true;

        @Override
        public void onChanged(String s) {
            if (s == null) {
                s = getResources().getString(R.string.clear_district_filter_entry);
            }
            Log.i("info", "District filter change observed: " + s);
            ((Button) mDistrictLayout.getViewById(R.id.filter_entry_button)).setText(s);
            updateRecyclerViewData(null);
            toggleVillageFilterVisibility();
            writeSActionCombs(s, !ignoreNextChange);
            if (ignoreNextChange) {
                ignoreNextChange = false;
            }
        }
    };

    public Observer<String> observeVillageFilterChange = new Observer<String>() {
        private boolean ignoreNextChange = true;

        @Override
        public void onChanged(String s) {
            if (s == null) {
                s = getResources().getString(R.string.clear_village_filter_entry);
            }
            Log.i("info", "Village filter change observed: " + s);
            ((Button) mVillageLayout.getViewById(R.id.filter_entry_button)).setText(s);
            updateRecyclerViewData(null);
            writeSActionCombs(s, !ignoreNextChange);
            if (ignoreNextChange) {
                ignoreNextChange = false;
            }
        }
    };

    public Observer<String> observeSectorFilterChange = new Observer<String>() {
        private boolean ignoreNextChange = true;

        @Override
        public void onChanged(String s) {
            if (s == null) {
                s = getResources().getString(R.string.clear_sector_filter_entry);
            }
            Log.i("info", "Sector filter change observed: " + s);
            ((Button) mSectorLayout.getViewById(R.id.filter_entry_button)).setText(s);
            updateRecyclerViewData(null);
            toggleSubsectorFilterVisibility();
            writeSActionCombs(s, !ignoreNextChange);
            if (ignoreNextChange) {
                ignoreNextChange = false;
            }
        }
    };

    public Observer<String> observeSubsectorFilterChange = new Observer<String>() {
        private boolean ignoreNextChange = true;

        @Override
        public void onChanged(String s) {
            if (s == null) {
                s = getResources().getString(R.string.all_subsector);
            }
            Log.i("info", "subSector filter change observed: " + s);
            ((Button) msubSectorLayout.getViewById(R.id.filter_entry_button)).setText(s);
            updateRecyclerViewData(null);
            writeSActionCombs(s, !ignoreNextChange);
            if (ignoreNextChange) {
                ignoreNextChange = false;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.i("logging", "MainFragment onCreate");
        return inflater.inflate(R.layout.browse_tab_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        List<Map.Entry<String,String>> strActionSCombs = new ArrayList<>();
        Log.i(DEBUG_TAG, "onViewCreated");
//        this.mViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
//        mViewModel.removeLastCallback();
        Observable.OnPropertyChangedCallback callback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (sender == mViewModel) {
                    // because the businessData list points to the same BusinessData references,
                    // changes made in MainViewModel to objects will also be reflected here
                    mBusinessListDisplayAdapter.updateFullData(businessData);
                    mBusinessListDisplayAdapter.getFilter().filter(null);
                    toggleNoBusinessMessage();
                }
            }
        };
        mViewModel.addOnPropertyChangedCallback(callback);
        this.businessData = mViewModel.getAllBusinesses(); // fill with default values while waiting for database
        mViewModel.businessList.observe(getViewLifecycleOwner(), new Observer<List<BusinessResult>>() {
            @Override
            public void onChanged(List<BusinessResult> businessResults) {
                Log.i("info", "new business results! size = " + businessResults.size());
                updateRecyclerViewData(businessResults);
            }
        });

        this.mBusinessListDisplayAdapter = new BusinessListDisplayAdapter(getContext(), this.businessData, this, strActionSCombs, false);
        LinearLayoutManager recyclerViewManager = new LinearLayoutManager(getContext());
        this.mRecyclerView.setLayoutManager(recyclerViewManager);
        this.mRecyclerView.setAdapter(this.mBusinessListDisplayAdapter);
        this.mRecyclerView.addItemDecoration(new DividerItemDecoration(this.mRecyclerView.getContext(),
                recyclerViewManager.getOrientation()));
        updateRecyclerViewData(null);

        // Assign listeners and modify the text on each filter entry
        RecyclerViewTabFragment currRecyclerViewTabFragment = this;
        mDistrictLayout = view.findViewById(R.id.district_button);
        mVillageLayout = view.findViewById(R.id.village_button);
        mSectorLayout = view.findViewById(R.id.sector_button);
        msubSectorLayout = view.findViewById(R.id.subsector_button);

        mDistrictLayout.getViewById(R.id.filter_entry_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BrowseFilterFragment bff = BrowseFilterFragment.newInstance(getResources().getString(R.string.select_district), MainViewModel.FILTER_TYPES.DISTRICT, currRecyclerViewTabFragment);
                bff.setTargetFragment(BrowseTabFragment.this, 300);
                bff.show(getActivity().getSupportFragmentManager(), "browse_fragment");
            }
        });

        mVillageLayout.getViewById(R.id.filter_entry_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BrowseFilterFragment bff = BrowseFilterFragment.newInstance(getResources().getString(R.string.select_village), MainViewModel.FILTER_TYPES.VILLAGE, currRecyclerViewTabFragment);
                bff.setTargetFragment(BrowseTabFragment.this, 300);
                bff.show(getActivity().getSupportFragmentManager(), "browse_fragment");
            }
        });
        this.toggleVillageFilterVisibility();

        mSectorLayout.getViewById(R.id.filter_entry_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BrowseFilterFragment bff = BrowseFilterFragment.newInstance(getResources().getString(R.string.select_sector), MainViewModel.FILTER_TYPES.SECTOR, currRecyclerViewTabFragment);
                bff.setTargetFragment(BrowseTabFragment.this, 300);
                bff.show(getActivity().getSupportFragmentManager(), "browse_fragment");
            }
        });

        msubSectorLayout.getViewById(R.id.filter_entry_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BrowseFilterFragment bff = BrowseFilterFragment.newInstance(getResources().getString(R.string.select_subsector), MainViewModel.FILTER_TYPES.SUBSECTOR, currRecyclerViewTabFragment);
                bff.setTargetFragment(BrowseTabFragment.this, 300);
                bff.show(getActivity().getSupportFragmentManager(), "browse_fragment");
            }
        });
        this.toggleSubsectorFilterVisibility();

        ((TextView) mDistrictLayout.getViewById(R.id.filter_entry_text)).setText(R.string.select_district);
        ((TextView) mVillageLayout.getViewById(R.id.filter_entry_text)).setText(R.string.select_village);
        ((TextView) mSectorLayout.getViewById(R.id.filter_entry_text)).setText(R.string.select_sector);
        ((TextView) msubSectorLayout.getViewById(R.id.filter_entry_text)).setText(R.string.select_subsector);

        mViewModel.browseDistrictValue.observe(getViewLifecycleOwner(), observeDistrictFilterChange);
        mViewModel.browseVillageValue.observe(getViewLifecycleOwner(), observeVillageFilterChange);
        mViewModel.browseSectorValue.observe(getViewLifecycleOwner(), observeSectorFilterChange);
        mViewModel.browseSubsectorValue.observe(getViewLifecycleOwner(), observeSubsectorFilterChange);

        toggleLoadingInterstitial();
        mViewModel.isDataLoaded.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean dataLoaded) {
                toggleLoadingInterstitial();
                toggleNoBusinessMessage();
            }
        });
        mViewModel.removeOnPropertyChangedCallback(callback);
    }

    /**
     * Updates the filtered data shown in the business list RecyclerView
     * @param businessResults List of updated business information, null if the business information remains unchanged
     */
    private void updateRecyclerViewData(@Nullable List<BusinessResult> businessResults) {
        if (businessResults != null) {
            Log.i("info", "new businesses found, updating " + businessResults.size());
            this.businessData = businessResults;
        }
        this.mBusinessListDisplayAdapter.updateFullData(this.businessData);
        this.mBusinessListDisplayAdapter.getFilter().filter(null);
        toggleNoBusinessMessage();
    }

    private void toggleVillageFilterVisibility() {
        if (getResources().getString(R.string.clear_district_filter_entry).equals(((Button) mDistrictLayout.findViewById(R.id.filter_entry_button)).getText())) {
            mVillageLayout.setVisibility(View.GONE);
        } else {
            mVillageLayout.setVisibility(View.VISIBLE);
        }
    }

    private void toggleSubsectorFilterVisibility() {
        if (getResources().getString(R.string.clear_sector_filter_entry).equals(((Button) mSectorLayout.findViewById(R.id.filter_entry_button)).getText())) {
            msubSectorLayout.setVisibility(View.GONE);
        } else {
            msubSectorLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void toggleNoBusinessMessage() {
        // Check if there are businesses to display. If not, toggle the no businesses view.
        if (this.mBusinessListDisplayAdapter.getItemCount() == 0 && this.mViewModel.isDataLoaded.getValue()) {
            this.mNoBusinessesMessage.setVisibility(View.VISIBLE);
            noBusiness = true;
        } else {
            this.mNoBusinessesMessage.setVisibility(View.GONE);
            noBusiness = false;
        }
    }

    @Override
    public void sendBackResult(Object result) {
        if (result instanceof FilterResult) {
            FilterResult filterResult = (FilterResult) result;
            if (filterResult.getType().equals(MainViewModel.FILTER_TYPES.DISTRICT)) {
                if (getResources().getString(R.string.clear_district_filter_entry).equals(filterResult.getValue())) {
                    mVillageLayout.setVisibility(View.GONE);
                } else {
                    mVillageLayout.setVisibility(View.VISIBLE);
                }
            }
            if (filterResult.getType().equals(MainViewModel.FILTER_TYPES.SECTOR)) {
                if (getResources().getString(R.string.clear_sector_filter_entry).equals(filterResult.getValue())) {
                    msubSectorLayout.setVisibility(View.GONE);
                } else {
                    msubSectorLayout.setVisibility(View.VISIBLE);
                }
            }
        }
        this.toggleNoBusinessMessage();
    }

    public Map<MainViewModel.FILTER_TYPES, String> getFilterValues() {
        Map<MainViewModel.FILTER_TYPES, String> filterValues = new HashMap<>();
        if (mViewModel.browseDistrictValue.getValue() != null) {
            filterValues.put(MainViewModel.FILTER_TYPES.DISTRICT, mViewModel.browseDistrictValue.getValue());
        }
        if (mViewModel.browseVillageValue.getValue() != null) {
            filterValues.put(MainViewModel.FILTER_TYPES.VILLAGE, mViewModel.browseVillageValue.getValue());
        }
        if (mViewModel.browseSectorValue.getValue() != null) {
            filterValues.put(MainViewModel.FILTER_TYPES.SECTOR, mViewModel.browseSectorValue.getValue());
        }
        if (mViewModel.browseSubsectorValue.getValue() != null) {
            filterValues.put(MainViewModel.FILTER_TYPES.SUBSECTOR, mViewModel.browseSubsectorValue.getValue());
        }
        return filterValues;
    }
}