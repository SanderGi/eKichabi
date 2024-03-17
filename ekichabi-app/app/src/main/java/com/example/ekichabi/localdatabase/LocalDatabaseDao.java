package com.example.ekichabi.localdatabase;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ekichabi.ui.main.MainViewModel;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;

@Dao
public abstract class LocalDatabaseDao {

    @Insert(entity=CategoryRow.class, onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertCategory(CategoryRow category);

    @Insert(entity=BusinessRow.class, onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertBusiness(BusinessRow business);

    @Insert(entity=VillageRow.class, onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertVillage(VillageRow village);

    @Insert(entity=DistrictRow.class, onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertDistrict(DistrictRow district);

    @Insert(entity=SubcategoryRow.class, onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertSubcategory(SubcategoryRow subcategory);

    @Insert(entity=SubvillageRow.class, onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertSubvillage(SubvillageRow subvillage);

    @Query("UPDATE `businesses` SET `id` = :businessId,`isFavorite` = :favorite WHERE `id` = :businessId")
    public abstract ListenableFuture<Integer> updateFavorite(long businessId, boolean favorite);

    @Query("SELECT * FROM businesses WHERE id = :id")
    public abstract BusinessRow getBusinessById(long id);


    @Query("SELECT b.name AS name, b.coordinates AS coordinates, c.englishName as category," +
            "d.name AS district, v.name AS village, b.phone AS phone, sv.name as subvillage," +
            "sc.englishName AS subcategory, b.id AS id, b.isFavorite AS isFavorite," +
            "b.englishDescription AS description, b.owner AS owner" +
            "   FROM businesses AS b, categories AS c, villages AS v, subcategories AS sc, subvillages AS sv," +
            "   districts as d" +
            "   WHERE (:id IS NULL OR b.id = :id)" +
          "       AND (:name IS NULL OR b.name = :name)" +
          "       AND (:coords IS NULL OR b.coordinates = :coords)" +
          "       AND (:category IS NULL OR b.category = :category)" +
          "       AND (:subcategory IS NULL OR b.subcategory = :subcategory)" +
          "       AND (:district IS NULL OR b.district = :district)" +
          "       AND (:village IS NULL OR b.village = :village)" +
          "       AND (:subvillage IS NULL OR b.subvillage = :subvillage)" +
          "       AND b.category = c.id AND b.village = v.id AND b.subvillage = sv.id" +
          "       AND b.district = d.id AND b.subcategory = sc.id" +
          "     ORDER BY name")
    public abstract LiveData<List<BusinessResult>> getBusinessesFromFilters(Long id, String name,
                                                                            String coords, Long category, Long subcategory,
                                                                            Long district, Long village, Long subvillage);

    @Query("SELECT b.name AS name, b.coordinates AS coordinates, c.englishName as category," +
            "d.name AS district, v.name AS village, b.phone AS phone, sv.name as subvillage," +
            "sc.englishName AS subcategory, b.id AS id, b.isFavorite AS isFavorite," +
            "b.englishDescription AS description, b.owner AS owner" +
            "   FROM businesses AS b, categories AS c, villages AS v, subcategories AS sc, subvillages AS sv," +
            "   districts as d" +
            "   WHERE b.category = c.id AND b.village = v.id AND b.subvillage = sv.id" +
            "   AND b.district = d.id AND b.subcategory = sc.id" +
            "   ORDER BY name")
    public abstract LiveData<List<BusinessResult>> getAllBusinessesEnglish();

    @Query("SELECT b.name AS name, b.coordinates AS coordinates, c.swahiliName as category," +
            "d.name AS district, v.name AS village, b.phone AS phone, sv.name as subvillage," +
            "sc.swahiliName AS subcategory, b.id AS id, b.isFavorite AS isFavorite," +
            "b.swahiliDescription AS description, b.owner AS owner" +
            "   FROM businesses AS b, categories AS c, villages AS v, subcategories AS sc, subvillages AS sv," +
            "   districts as d" +
            "   WHERE b.category = c.id AND b.village = v.id AND b.subvillage = sv.id" +
            "   AND b.district = d.id AND b.subcategory = sc.id" +
            "   ORDER BY name")
    public abstract LiveData<List<BusinessResult>> getAllBusinessesSwahili();

    public LiveData<List<BusinessResult>> getAllBusinesses(MainViewModel.Language language) {
        if (language == MainViewModel.Language.ENGLISH) {
            return getAllBusinessesEnglish();
        } else {
            return getAllBusinessesSwahili();
        }
    }

    @Query("SELECT englishName FROM categories WHERE id = :id")
    public abstract LiveData<String> getCategoryNameByIdEnglish(Long id);

    @Query("SELECT swahiliName FROM categories WHERE id = :id")
    public abstract LiveData<String> getCategoryNameByIdSwahili(Long id);

    @Query("SELECT id FROM categories WHERE englishName = :englishName")
    public abstract LiveData<Long> getIdByNameEnglish(String englishName);

    @Query("SELECT id FROM categories WHERE swahiliName = :swahiliName")
    public abstract LiveData<Long> getIdByNameSwahili(String swahiliName);

    public LiveData<String> getCategoryNameById(Long id, MainViewModel.Language language) {
        if (language == MainViewModel.Language.ENGLISH) {
            return getCategoryNameByIdEnglish(id);
        } else {
            return getCategoryNameByIdSwahili(id);
        }
    }

    public LiveData<Long> getIdByCategoryName(String name, MainViewModel.Language language) {
        if (language == MainViewModel.Language.ENGLISH) {
            return getIdByNameEnglish(name);
        } else {
            return getIdByNameSwahili(name);
        }
    }


    @Query("SELECT COUNT(*) from businesses")
    public abstract int getBusinessCount();

    @Query("SELECT * FROM villages")
    public abstract LiveData<List<VillageRow>> getAllVillages();

    @Query("SELECT * FROM districts")
    public abstract LiveData<List<DistrictRow>> getAllDistricts();

    @Query("SELECT * FROM categories")
    public abstract LiveData<List<CategoryRow>> getAllCategories();

    @Query("SELECT * FROM subcategories")
    public abstract LiveData<List<SubcategoryRow>> getAllSubcategories();

    @Query("SELECT * FROM subvillages")
    public abstract LiveData<List<SubvillageRow>> getAllSubvillages();

    @Query("SELECT * FROM villages WHERE district = :districtId")
    public abstract LiveData<List<VillageRow>> getVillagesInDistrict(long districtId);

    @Query("SELECT * FROM subvillages WHERE village = :villageId")
    public abstract LiveData<List<SubvillageRow>> getSubvillagesInVillage(long villageId);

    @Query("SELECT * FROM subcategories WHERE category = :categoryId")
    public abstract LiveData<List<SubcategoryRow>> getSubcategoryInCategory(long categoryId);
}