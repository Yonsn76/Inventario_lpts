package com.san_pedrito.myapplication.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object for the Laptop entity.
 * Defines methods for accessing and manipulating laptop data in the database.
 */
@Dao
public interface LaptopDao {
    
    /**
     * Insert a new laptop into the database.
     * 
     * @param laptop The laptop to insert
     * @return The ID of the inserted laptop
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Laptop laptop);
    
    /**
     * Update an existing laptop in the database.
     * 
     * @param laptop The laptop to update
     */
    @Update
    void update(Laptop laptop);
    
    /**
     * Delete a laptop from the database.
     * 
     * @param laptop The laptop to delete
     */
    @Delete
    void delete(Laptop laptop);
    
    /**
     * Get a laptop by its ID.
     * 
     * @param id The ID of the laptop to retrieve
     * @return The laptop with the specified ID
     */
    @Query("SELECT * FROM portatiles WHERE id = :id")
    LiveData<Laptop> getLaptopById(int id);
    
    /**
     * Get all laptops from the database.
     * 
     * @return A list of all laptops
     */
    @Query("SELECT * FROM portatiles")
    LiveData<List<Laptop>> getAllLaptops();
    
    /**
     * Get laptops by their status.
     * 
     * @param estado The status to filter by
     * @return A list of laptops with the specified status
     */
    @Query("SELECT * FROM portatiles WHERE estado = :estado")
    LiveData<List<Laptop>> getLaptopsByStatus(String estado);
    
    /**
     * Get laptops by their location.
     * 
     * @param ubicacion The location to filter by
     * @return A list of laptops at the specified location
     */
    @Query("SELECT * FROM portatiles WHERE ubicacion = :ubicacion")
    LiveData<List<Laptop>> getLaptopsByLocation(String ubicacion);
    
    /**
     * Search laptops by serial number.
     * 
     * @param numeroSerie The serial number to search for
     * @return A list of laptops matching the search criteria
     */
    @Query("SELECT * FROM portatiles WHERE numero_serie LIKE '%' || :numeroSerie || '%'")
    LiveData<List<Laptop>> searchLaptopsBySerialNumber(String numeroSerie);
    
    /**
     * Check if a laptop with the given serial number exists.
     * 
     * @param numeroSerie The serial number to check
     * @return True if a laptop with the serial number exists, false otherwise
     */
    @Query("SELECT EXISTS(SELECT 1 FROM portatiles WHERE numero_serie = :numeroSerie)")
    boolean laptopExists(String numeroSerie);
}