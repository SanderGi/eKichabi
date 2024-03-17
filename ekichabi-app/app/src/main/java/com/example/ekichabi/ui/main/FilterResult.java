package com.example.ekichabi.ui.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FilterResult {
    @NonNull
    private final MainViewModel.FILTER_TYPES mType;
    @Nullable
    private final String mValue;
    @Nullable private final Long mId;

    public FilterResult(@NonNull MainViewModel.FILTER_TYPES filterType, @Nullable String filterValue, Long filterId) {
        this.mType = filterType;
        this.mValue = filterValue;
        this.mId = filterId;
    }

    @NonNull
    public MainViewModel.FILTER_TYPES getType() {
        return mType;
    }

    @Nullable
    public String getValue() {
        return mValue;
    }

    @Nullable
    public Long getId() {
        return mId;
    }

    @NonNull
    public String toString() {
        if (mId == null) {
            return mType.name() + " " + mValue + " null";
        }
        return mType.name() + " " + mValue + " " + mId.toString();
    }
}
