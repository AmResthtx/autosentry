package com.autosentry.app.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SessionDao {
    @Insert
    long insert(Session session);

    @Update
    void update(Session session);

    @Delete
    void delete(Session session);

    @Query("SELECT * FROM sessions ORDER BY startTime DESC LIMIT :limit")
    List<Session> latest(int limit);

    @Query("SELECT * FROM sessions WHERE id = :id")
    Session getById(long id);

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    List<Session> getAll();

    @Query("SELECT * FROM sessions WHERE notes LIKE '%' || :query || '%' OR tripPurpose LIKE '%' || :query || '%' ORDER BY startTime DESC")
    List<Session> searchTrips(String query);

    @Query("DELETE FROM sessions")
    void deleteAll();
}
