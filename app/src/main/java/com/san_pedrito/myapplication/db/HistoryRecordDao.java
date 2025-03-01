package com.san_pedrito.myapplication.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object for the HistoryRecord entity.
 * Defines methods for accessing and manipulating history records in the database.
 */
@Dao
public interface HistoryRecordDao {
    
    /**
     * Insert a new history record into the database.
     * 
     * @param historyRecord The history record to insert
     * @return The ID of the inserted record
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(HistoryRecord historyRecord);
    
    /**
     * Update an existing history record in the database.
     * 
     * @param historyRecord The history record to update
     */
    @Update
    void update(HistoryRecord historyRecord);
    
    /**
     * Delete a history record from the database.
     * 
     * @param historyRecord The history record to delete
     */
    @Delete
    void delete(HistoryRecord historyRecord);
    
    /**
     * Get a history record by its ID.
     * 
     * @param id The ID of the history record to retrieve
     * @return The history record with the specified ID
     */
    @Query("SELECT * FROM historial WHERE id = :id")
    LiveData<HistoryRecord> getHistoryRecordById(int id);
    
    /**
     * Get all history records for a specific laptop.
     * 
     * @param laptopId The ID of the laptop
     * @return A list of history records for the specified laptop
     */
    @Query("SELECT * FROM historial WHERE laptop_id = :laptopId ORDER BY fecha DESC")
    LiveData<List<HistoryRecord>> getHistoryRecordsForLaptop(int laptopId);
    
    /**
     * Get all history records from the database.
     * 
     * @return A list of all history records
     */
    @Query("SELECT * FROM historial ORDER BY fecha DESC")
    LiveData<List<HistoryRecord>> getAllHistoryRecords();
    
    /**
     * Get history records by action type.
     * 
     * @param accion The action type to filter by
     * @return A list of history records with the specified action type
     */
    @Query("SELECT * FROM historial WHERE accion = :accion ORDER BY fecha DESC")
    LiveData<List<HistoryRecord>> getHistoryRecordsByAction(String accion);
    
    /**
     * Get history records within a date range.
     * 
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return A list of history records within the specified date range
     */
    @Query("SELECT * FROM historial WHERE fecha BETWEEN :startDate AND :endDate ORDER BY fecha DESC")
    LiveData<List<HistoryRecord>> getHistoryRecordsByDateRange(Date startDate, Date endDate);
    
    /**
     * Get history records by user.
     * 
     * @param usuario The user to filter by
     * @return A list of history records for the specified user
     */
    @Query("SELECT * FROM historial WHERE usuario = :usuario ORDER BY fecha DESC")
    LiveData<List<HistoryRecord>> getHistoryRecordsByUser(String usuario);
    
    /**
     * Get the most recent history record for a laptop.
     * 
     * @param laptopId The ID of the laptop
     * @return The most recent history record for the specified laptop
     */
    @Query("SELECT * FROM historial WHERE laptop_id = :laptopId ORDER BY fecha DESC LIMIT 1")
    LiveData<HistoryRecord> getMostRecentHistoryRecordForLaptop(int laptopId);
    
    /**
     * Delete all history records for a specific laptop.
     * 
     * @param laptopId The ID of the laptop
     */
    @Query("DELETE FROM historial WHERE laptop_id = :laptopId")
    void deleteAllHistoryRecordsForLaptop(int laptopId);
}