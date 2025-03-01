package com.san_pedrito.myapplication.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Main database class for the application.
 * This class defines the database configuration and serves as the main access point
 * for the underlying connection.
 */
@Database(entities = {Laptop.class, HistoryRecord.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    
    // DAOs
    public abstract LaptopDao laptopDao();
    public abstract HistoryRecordDao historyRecordDao();
    
    // Singleton instance
    private static volatile AppDatabase INSTANCE;
    
    /**
     * Gets the database instance, creating it if it doesn't exist.
     * 
     * @param context The application context
     * @return The singleton database instance
     */
    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "inventario_portatiles_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}