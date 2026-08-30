package com.autosentry.app.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AgentLogDao {
    @Insert
    long insert(AgentLog log);

    @Query("SELECT * FROM agent_logs ORDER BY timestamp DESC LIMIT :limit")
    List<AgentLog> latest(int limit);

    @Query("SELECT * FROM agent_logs ORDER BY timestamp DESC LIMIT :limit")
    List<AgentLog> getLatest(int limit);
}
