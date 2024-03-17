package com.example.ekichabi.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.Observable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ekichabi.localdatabase.BusinessResult;
import com.example.temp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FavoritesTabFragment extends RecyclerViewTabFragment {
    private final String DEBUG_TAG = "FavoritesTabFragment";

    public static FavoritesTabFragment newInstance() {
        return new FavoritesTabFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.i(DEBUG_TAG, "FavoritesTabFragment onCreate");
        Bundle args = getArguments();
        return inflater.inflate(R.layout.favorites_tab_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        List<Map.Entry<String,String>> strActionSCombs = new ArrayList<>();
        Log.i(DEBUG_TAG, "FavoritesTabFragment onViewCreated");
//        this.mViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
//        mViewModel.removeLastCallback();
        Observable.OnPropertyChangedCallback callback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (sender == mViewModel) {
                    Log.i(DEBUG_TAG, "Updated businesses");
                    businessData = mViewModel.getFavoriteBusinesses();
                    mBusinessListDisplayAdapter.updateFilteredData(businessData);
                    toggleNoBusinessMessage();
                }
            }
        };
        mViewModel.addOnPropertyChangedCallback(callback);

        mViewModel.businessList.observe(getViewLifecycleOwner(), new Observer<List<BusinessResult>>() {
            @Override
            public void onChanged(List<BusinessResult> businessResults) {
                businessData = mViewModel.getFavoriteBusinesses();
                mBusinessListDisplayAdapter.updateFilteredData(businessData);
                toggleNoBusinessMessage();
            }
        });

        // Create our RecyclerView
        this.businessData = mViewModel.getFavoriteBusinesses();
        this.mBusinessListDisplayAdapter = new BusinessListDisplayAdapter(getContext(), this.businessData, this, strActionSCombs, true);
        LinearLayoutManager recyclerViewManager = new LinearLayoutManager(getContext());
        this.mRecyclerView.setLayoutManager(recyclerViewManager);
        this.mRecyclerView.setAdapter(this.mBusinessListDisplayAdapter);
        this.mRecyclerView.addItemDecoration(new DividerItemDecoration(this.mRecyclerView.getContext(),
                recyclerViewManager.getOrientation()));

        toggleLoadingInterstitial();
        mViewModel.isDataLoaded.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean dataLoaded) {
                toggleLoadingInterstitial();
                toggleNoBusinessMessage();
            }
        });
        toggleNoBusinessMessage();
        mViewModel.removeOnPropertyChangedCallback(callback);
    }

    @Override
    protected void toggleNoBusinessMessage() {
        // Check if there are businesses to display. If not, toggle the no businesses view.
        if (this.businessData.isEmpty() && this.mViewModel.isDataLoaded.getValue()) {
            this.mNoBusinessesMessage.setVisibility(View.VISIBLE);
        } else {
            this.mNoBusinessesMessage.setVisibility(View.GONE);
        }
    }
}
