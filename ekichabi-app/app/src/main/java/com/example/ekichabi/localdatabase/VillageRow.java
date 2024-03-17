package com.example.ekichabi.localdatabase;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = LocalDatabase.VillageTable.TABLE_NAME)
public class VillageRow {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = LocalDatabase.VillageTable.ID)
    public long id;

    @NonNull
    @ColumnInfo(name = LocalDatabase.VillageTable.NAME)
    public String name;

    @NonNull
    @ColumnInfo(name = LocalDatabase.VillageTable.DISTRICT)
    public long district;

    @NonNull
    @ColumnInfo(name = LocalDatabase.VillageTable.COORDS)
    public String coordinates;

    public VillageRow(long id, @NonNull String name, long district, @NonNull String coordinates) {
        this.id = id;
        this.name = name;
        this.district = district;
        this.coordinates = coordinates;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof VillageRow) {
            return this.id == ((VillageRow) other).id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) this.id;
    }
}
