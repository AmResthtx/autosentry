package com.autosentry.app.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SessionDao {
    @Insert
    void insert(Session session);

    @Query("SELECT * FROM sessions ORDER BY startedAt DESC LIMIT :limit")
    List<Session> latest(int limit);
}
