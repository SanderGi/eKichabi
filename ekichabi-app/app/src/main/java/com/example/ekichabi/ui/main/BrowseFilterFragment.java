package com.example.ekichabi.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ekichabi.localdatabase.CategoryRow;
import com.example.ekichabi.localdatabase.DistrictRow;
import com.example.ekichabi.localdatabase.SubcategoryRow;
import com.example.ekichabi.localdatabase.VillageRow;
import com.example.temp.R;

import java.util.ArrayList;
import java.util.List;

public class BrowseFilterFragment extends DialogFragment implements BrowseFilterDisplayAdapter.BrowseFragmentInterface {
    private final String DEBUG_TAG = "BrowseFilterFragment";
    private TextView mTitle;
    private MainViewModel mViewModel;
    private RecyclerView mTextDisplay;
    private BrowseFilterDisplayAdapter mTextDisplayAdapter;
    private MainViewModel.FILTER_TYPES mType;
    private RecyclerViewTabFragment mParentFragment;
    private ImageView mCloseButton;

    public static BrowseFilterFragment newInstance(String title, MainViewModel.FILTER_TYPES type, RecyclerViewTabFragment parentFragment) {
        BrowseFilterFragment frag = new BrowseFilterFragment(type, parentFragment);
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    public BrowseFilterFragment(MainViewModel.FILTER_TYPES type, RecyclerViewTabFragment parentFragment) {
        super();
        this.mType = type;
        this.mParentFragment = parentFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.browse_filter_fragment, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        // Set the title
        this.mTitle = view.findViewById(R.id.browse_filter_title);
        this.mTitle.setText(getArguments().getString("title"));
        // Create our RecyclerView
        this.mTextDisplay = view.findViewById(R.id.text_display);
        this.mTextDisplayAdapter = new BrowseFilterDisplayAdapter(getContext(), new ArrayList<FilterResult>(), mType,this);

        // Check if we're a district/village or sector, because that'll change our layout manager

        if (mType == MainViewModel.FILTER_TYPES.SECTOR) {
            /*GridLayoutManager gridViewManager = new GridLayoutManager(getContext(), 3);
            this.mTextDisplay.setLayoutManager(gridViewManager);
            this.mTextDisplay.setAdapter(mTextDisplayAdapter);
            this.mTextDisplay.addItemDecoration(new DividerItemDecoration(getContext(),
                    DividerItemDecoration.HORIZONTAL));
            this.mTextDisplay.addItemDecoration(new DividerItemDecoration(getContext(),
                    DividerItemDecoration.VERTICAL));*/
            LinearLayoutManager recyclerViewManager = new LinearLayoutManager(getContext());
            this.mTextDisplay.setLayoutManager(recyclerViewManager);
            this.mTextDisplay.setAdapter(mTextDisplayAdapter);
            this.mTextDisplay.addItemDecoration(new DividerItemDecoration(getContext(),
                    recyclerViewManager.getOrientation()));
        } else {
            LinearLayoutManager recyclerViewManager = new LinearLayoutManager(getContext());
            this.mTextDisplay.setLayoutManager(recyclerViewManager);
            this.mTextDisplay.setAdapter(mTextDisplayAdapter);
            this.mTextDisplay.addItemDecoration(new DividerItemDecoration(getContext(),
                    recyclerViewManager.getOrientation()));
        }

        switch (mType) {
            case DISTRICT:
                LiveData<List<DistrictRow>> live_d = mViewModel.getDistricts();
                live_d.observe(getViewLifecycleOwner(), new Observer<List<DistrictRow>>() {
                    @Override
                    public void onChanged(List<DistrictRow> districtRows) {
                        List<FilterResult> fr = new ArrayList<>();
                        for (DistrictRow dr : districtRows) {
                            fr.add(new FilterResult(MainViewModel.FILTER_TYPES.DISTRICT, dr.name, dr.id));
                        }
                        mTextDisplayAdapter.update(fr);
                    }
                });
                break;
            case VILLAGE:
                LiveData<List<VillageRow>> live_v = mViewModel.getVillages();
                live_v.observe(getViewLifecycleOwner(), new Observer<List<VillageRow>>() {
                    @Override
                    public void onChanged(List<VillageRow> villageRows) {
                        List<FilterResult> fr = new ArrayList<>();
                        for (VillageRow vr : villageRows) {
                            fr.add(new FilterResult(MainViewModel.FILTER_TYPES.VILLAGE, vr.name, vr.id));
                        }
                        mTextDisplayAdapter.update(fr);
                    }
                });
                break;
            case SECTOR:
                LiveData<List<CategoryRow>> live_c = mViewModel.getSectors();
                live_c.observe(getViewLifecycleOwner(), new Observer<List<CategoryRow>>() {
                    @Override
                    public void onChanged(List<CategoryRow> categoryRows) {
                        List<FilterResult> fr = new ArrayList<>();
                        for (CategoryRow cr : categoryRows) {
                            if (mViewModel.currentLang == MainViewModel.Language.ENGLISH) {
                                fr.add(new FilterResult(MainViewModel.FILTER_TYPES.SECTOR, cr.englishName, cr.id));
                            } else if (mViewModel.currentLang == MainViewModel.Language.SWAHILI) {
                                fr.add(new FilterResult(MainViewModel.FILTER_TYPES.SECTOR, cr.swahiliName, cr.id));
                            }
                        }
                        mTextDisplayAdapter.update(fr);
                    }
                });
                break;
            case SUBSECTOR:
                LiveData<List<SubcategoryRow>> live_sc = mViewModel.getSubsectors();
                live_sc.observe(getViewLifecycleOwner(), new Observer<List<SubcategoryRow>>() {
                    @Override
                    public void onChanged(List<SubcategoryRow> subcategoryRows) {
                        List<FilterResult> fr = new ArrayList<>();
                        for (SubcategoryRow cr : subcategoryRows) {
                            if (mViewModel.currentLang == MainViewModel.Language.ENGLISH && cr.englishName != null) {
                                fr.add(new FilterResult(MainViewModel.FILTER_TYPES.SUBSECTOR, cr.englishName, cr.id));
                            } else if (mViewModel.currentLang == MainViewModel.Language.SWAHILI && cr.englishName != null) {
                                fr.add(new FilterResult(MainViewModel.FILTER_TYPES.SUBSECTOR, cr.swahiliName, cr.id));
                            }
                        }
                        mTextDisplayAdapter.update(fr);
                    }
                });
                break;
        }
        // Set the back button
        this.mCloseButton = view.findViewById(R.id.browse_filter_close);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    @Nullable
    private String getDefaultFilterValue() {
        switch(this.mType) {
            case DISTRICT:
                return getResources().getString(R.string.clear_district_filter_entry);
            case VILLAGE:
                return getResources().getString(R.string.clear_village_filter_entry);
            case SUBVILLAGE:
                return getResources().getString((R.string.all_subsector));
            case SECTOR:
                return getResources().getString((R.string.clear_sector_filter_entry));
            default:
                break;
        }
        return null;
    }

    @Override
    public void sendBackResult(Object result) {
        if (result instanceof FilterResult) {
            FilterResult filterResult = (FilterResult) result;
            Log.i("info", filterResult.getValue());
            if (filterResult.getId() == null) {
                this.mViewModel.setFilterResult(new FilterResult(filterResult.getType(), null, null));
            } else {
                this.mViewModel.setFilterResult(filterResult);
            }
//            // assume filterResult.mType == this.mType
//            if (filterResult.getValue() == null) {
//                this.mParentFragment.sendBackResult(new FilterResult(filterResult.getType(), getDefaultFilterValue(), null));
//            } else {
//                this.mParentFragment.sendBackResult(filterResult);
//            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();

        Window window = getDialog().getWindow();
        Point size = new Point();
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);

        params.width = (int) (size.x * 0.85);
        params.height = (int) (size.y * 0.85);
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }
}
