package com.example.ekichabi.localdatabase;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = LocalDatabase.SubcategoryTable.TABLE_NAME)
public class SubcategoryRow {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = LocalDatabase.SubcategoryTable.ID)
    public long id;

    @ColumnInfo(name = LocalDatabase.SubcategoryTable.ENGLISH_NAME)
    public String englishName;

    @NonNull
    @ColumnInfo(name = LocalDatabase.SubcategoryTable.SWAHILI_NAME)
    public String swahiliName;

    @NonNull
    @ColumnInfo(name = LocalDatabase.SubcategoryTable.CATEGORY)
    public long category;

    public SubcategoryRow(long id, @NonNull String englishName, @NonNull String swahiliName, long category) {
        this.id = id;
        this.englishName = englishName;
        this.swahiliName = swahiliName;
        this.category = category;
    }
    @Override
    public boolean equals(Object other) {
        if (other instanceof SubcategoryRow) {
            return this.id == ((SubcategoryRow) other).id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) this.id;
    }
}