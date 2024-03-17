package com.example.ekichabi.localdatabase;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = LocalDatabase.DistrictTable.TABLE_NAME)
public class DistrictRow {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = LocalDatabase.DistrictTable.ID)
    public long id;

    @NonNull
    @ColumnInfo(name = LocalDatabase.DistrictTable.NAME)
    public String name;

    @NonNull
    @ColumnInfo(name = LocalDatabase.DistrictTable.COORDS)
    public String coordinates;

    public DistrictRow(long id, @NonNull String name, @NonNull String coordinates) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
    }
    @Override
    public boolean equals(Object other) {
        if (other instanceof DistrictRow) {
            return this.id == ((DistrictRow) other).id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) this.id;
    }
}
