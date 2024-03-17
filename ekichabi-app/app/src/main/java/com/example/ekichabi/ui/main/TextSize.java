package com.example.ekichabi.ui.main;

import com.example.temp.R;

public enum TextSize {
    Small(R.style.FontStyle_Small),
    Medium(R.style.FontStyle_Medium),
    Large(R.style.FontStyle_Large),
    XLarge(R.style.FontStyle_XLarge);

    public int resId;
    TextSize(int res) {
        this.resId = res;
    }
}
