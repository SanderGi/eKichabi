package com.example.ekichabi.localdatabase;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = LocalDatabase.CategoryTable.TABLE_NAME)
public class CategoryRow {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = LocalDatabase.CategoryTable.ID)
    public long id;

    @NonNull
    @ColumnInfo(name = LocalDatabase.CategoryTable.ENGLISH_NAME)
    public String englishName;

    @NonNull
    @ColumnInfo(name = LocalDatabase.CategoryTable.SWAHILI_NAME)
    public String swahiliName;

    public CategoryRow(long id, @NonNull String englishName, @NonNull String swahiliName) {
        this.id = id;
        this.englishName = englishName;
        this.swahiliName = swahiliName;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof CategoryRow) {
            return this.id == ((CategoryRow) other).id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) this.id;
    }
}
