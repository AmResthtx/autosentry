package com.autosentry.app.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface MaintenanceDao {
    @Insert
    long insert(MaintenanceEvent evt);

    @Update
    void update(MaintenanceEvent evt);

    @Delete
    void delete(MaintenanceEvent evt);

    @Query("SELECT * FROM maintenance_events ORDER BY nextServiceAt ASC")
    List<MaintenanceEvent> dueSoon();

    @Query("SELECT * FROM maintenance_events WHERE eventType = :eventType ORDER BY eventTime DESC LIMIT 1")
    List<MaintenanceEvent> getLatestByType(String eventType);
}
