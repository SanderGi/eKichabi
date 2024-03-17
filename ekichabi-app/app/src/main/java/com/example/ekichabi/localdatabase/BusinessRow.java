package com.example.ekichabi.localdatabase;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = LocalDatabase.BusinessTable.TABLE_NAME)
public class BusinessRow {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = LocalDatabase.BusinessTable.ID)
    private final long id;

    @NonNull
    @ColumnInfo(name = LocalDatabase.BusinessTable.NAME)
    private final String name;

    @NonNull
    @ColumnInfo(name = LocalDatabase.BusinessTable.COORDS)
    private final String coordinates;

    @NonNull
    @ColumnInfo(name = LocalDatabase.BusinessTable.CATEGORY)
    private final long category;

    @NonNull
    @ColumnInfo(name = LocalDatabase.BusinessTable.SUBCATEGORY)
    private final long subcategory;

    @NonNull
    @ColumnInfo(name = LocalDatabase.BusinessTable.DISTRICT)
    private final long district;

    @NonNull
    @ColumnInfo(name = LocalDatabase.BusinessTable.VILLAGE)
    private final long village;

    @NonNull
    @ColumnInfo(name = LocalDatabase.BusinessTable.SUBVILLAGE)
    private final long subvillage;

    @NonNull
    @ColumnInfo(name = LocalDatabase.BusinessTable.ENGLISH_DESCRIPTION)
    private final String englishDescription;

    @NonNull
    @ColumnInfo(name = LocalDatabase.BusinessTable.SWAHILI_DESCRIPTION)
    private final String swahiliDescription;

    @ColumnInfo(name = LocalDatabase.BusinessTable.PHONE)
    private final String phone;

    @ColumnInfo(name = LocalDatabase.BusinessTable.OWNER)
    private final String owner;

    @NonNull
    @ColumnInfo(name = LocalDatabase.BusinessTable.IS_FAVORITE)
    public boolean isFavorite;

    public BusinessRow(@NonNull long id, @NonNull String name,
                       @NonNull String coordinates, @NonNull long category,
                       @NonNull long subcategory, @NonNull long district,
                       @NonNull long village, @NonNull long subvillage,
                       @NonNull String englishDescription, @NonNull String swahiliDescription,
                       String phone, boolean isFavorite, String owner) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.category = category;
        this.subcategory = subcategory;
        this.subvillage = subvillage;
        this.district = district;
        this.village = village;
        this.englishDescription = englishDescription;
        this.swahiliDescription = swahiliDescription;
        this.phone = phone;
        this.isFavorite = isFavorite;
        this.owner = owner;
    }

    public long getId() {
        return this.id;
    }

    @NonNull
    public String getName() {
        return this.name;
    }

    @NonNull
    public String getCoordinates() {
        return this.coordinates;
    }

    public long getCategory() {
        return this.category;
    }

    public long getSubcategory() {
        return this.subcategory;
    }

    public long getSubvillage() {
        return this.subvillage;
    }

    public long getDistrict() {
        return this.district;
    }

    public long getVillage() {
        return this.village;
    }

    public String getPhone() {
        return this.phone;
    }

    @NonNull
    public String getEnglishDescription() { return this.englishDescription; }

    @NonNull
    public String getSwahiliDescription() { return this.swahiliDescription; }

    public String getOwner() { return this.owner; }

    public boolean isFavorite() {
        return isFavorite;
    }
}
