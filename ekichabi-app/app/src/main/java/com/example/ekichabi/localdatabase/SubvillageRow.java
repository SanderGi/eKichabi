package com.example.ekichabi.localdatabase;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.ekichabi.QueryHandler;

@Entity(tableName = LocalDatabase.SubvillageTable.TABLE_NAME)
public class SubvillageRow {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = LocalDatabase.SubvillageTable.ID)
    public long id;

    @NonNull
    @ColumnInfo(name = LocalDatabase.SubvillageTable.NAME)
    public String name;

    @NonNull
    @ColumnInfo(name = LocalDatabase.SubvillageTable.VILLAGE)
    public long village;

    @NonNull
    @ColumnInfo(name = LocalDatabase.SubvillageTable.COORDS)
    public String coordinates;

    public SubvillageRow(long id, @NonNull String name, long village, @NonNull String coordinates) {
        this.id = id;
        this.name = name;
        this.village = village;
        this.coordinates = coordinates;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SubvillageRow) {
            return this.id == ((SubvillageRow) other).id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) this.id;
    }
}