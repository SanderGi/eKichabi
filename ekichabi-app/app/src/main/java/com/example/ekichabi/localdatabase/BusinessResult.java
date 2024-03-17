package com.example.ekichabi.localdatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BusinessResult {
    @NonNull public String name;
    public String coordinates;
    public String category;
    public String district;
    public String village;
    @Nullable public String phone;
    public String subvillage;
    public String subcategory;
    public String description;
    public String owner;
    public boolean isFavorite;
    public long id;

    @NonNull
    public String toString() {
        return "[\"name\"=\"" + name +
                "\", \"district\"=\"" + district +
                "\", \"category\"=\"" + category + "\"]";
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof BusinessResult) {
            return this.id == ((BusinessResult) other).id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) this.id;
    }
}
