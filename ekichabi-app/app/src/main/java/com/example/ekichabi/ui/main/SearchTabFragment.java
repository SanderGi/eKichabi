package com.example.ekichabi.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.Observable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ekichabi.localdatabase.BusinessResult;
import com.example.temp.R;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class SearchTabFragment extends RecyclerViewTabFragment {
    private static final String DEBUG_TAG = "SearchTabFragment";

    public static SearchTabFragment newInstance() {
        return new SearchTabFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.i(DEBUG_TAG, "onCreate");
        return inflater.inflate(R.layout.search_tab_fragment, container, false);
    }

    public Map.Entry<String, Boolean> createEntry(String a, Boolean b) {
        return new AbstractMap.SimpleEntry<>(a, b);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        List<Map.Entry<String,String>> strActionSCombs = new ArrayList<>();
//        List<Map.Entry<String, Boolean>> strSuccessCombs = new ArrayList<>();
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
                    mBusinessListDisplayAdapter.getFilter().filter(mViewModel.searchString.getValue());
                }
            }
        };
        mViewModel.addOnPropertyChangedCallback(callback);

        this.businessData = mViewModel.getAllBusinesses();
        mViewModel.businessList.observe(getViewLifecycleOwner(), new Observer<List<BusinessResult>>() {
            @Override
            public void onChanged(List<BusinessResult> businessResults) {
                businessData = businessResults;
                mBusinessListDisplayAdapter.updateFullData(businessData);
                mBusinessListDisplayAdapter.getFilter().filter(mViewModel.searchString.getValue());
            }
        });


        this.mBusinessListDisplayAdapter = new BusinessListDisplayAdapter(getContext(), this.businessData, this, strActionSCombs, false);
        LinearLayoutManager recyclerViewManager = new LinearLayoutManager(getContext());
        this.mRecyclerView.setLayoutManager(recyclerViewManager);
        this.mRecyclerView.setAdapter(this.mBusinessListDisplayAdapter);
        this.mRecyclerView.addItemDecoration(new DividerItemDecoration(this.mRecyclerView.getContext(),
                recyclerViewManager.getOrientation()));

        SearchView searchView = view.findViewById(R.id.search_bar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                AndroidLogger logger = new AndroidLogger();
                try {
                    logger.writeBytesToFile(logger.getSearchAction(s.toLowerCase(Locale.ROOT).replaceAll("[^a-zA-Z]", ""), true, s.matches("[a-z]+")), requireContext());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (mBusinessListDisplayAdapter.getItemCount() > 0) {
                    mViewModel.searchString.setValue(s);
                    mBusinessListDisplayAdapter.getFilter().filter(s);
                } else {
                    mBusinessListDisplayAdapter.updateActionCombs();
                    Toast.makeText(getActivity(), getText(R.string.no_match_found), Toast.LENGTH_LONG).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mViewModel.searchString.setValue(s);
                mBusinessListDisplayAdapter.getFilter().filter(s);
                AndroidLogger logger = new AndroidLogger();
//                    strSuccessCombs.add(createEntry(mViewModel.searchString.getValue(), true));
                try {
                    logger.writeBytesToFile(logger.getSearchAction(s.toLowerCase().replaceAll("[^a-zA-Z]", ""), false, s.matches("[a-z]+")), requireContext());
//                        Log.i("logger content", "content:" + logger.toJSON("deviceID", logger.readBytes(requireContext())));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mBusinessListDisplayAdapter.updateActionCombs();
                return false;
            }
        });
        if (mViewModel.searchString.getValue() != null) {
            searchView.setQuery(mViewModel.searchString.getValue(), true);
            mBusinessListDisplayAdapter.getFilter().filter(mViewModel.searchString.getValue());
        }

        toggleLoadingInterstitial();
        mViewModel.isDataLoaded.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean dataLoaded) {
                toggleLoadingInterstitial();
                toggleNoBusinessMessage();
            }
        });

        toggleNoBusinessMessage();
//        mViewModel.removeOnPropertyChangedCallback(callback);
        mViewModel.removeOnPropertyChangedCallback(callback);
    }

    public void onDestroyView() {
        mViewModel.onCleared();
        super.onDestroyView();
    }

    @Override
    protected void toggleNoBusinessMessage() {
        // Check if there are businesses to display. If not, toggle the no businesses view.
        if (this.mBusinessListDisplayAdapter.getItemCount() == 0 && this.mViewModel.isDataLoaded.getValue()) {
            this.mNoBusinessesMessage.setVisibility(View.VISIBLE);
        } else {
            this.mNoBusinessesMessage.setVisibility(View.GONE);
        }
    }

    @Override
    public void sendBackResult(Object result) {
        this.toggleNoBusinessMessage();
    }
}
