package com.autosentry.app.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {
        Session.class,
        PIDRecord.class,
        PIDDefinition.class,
        AgentLog.class,
        MaintenanceEvent.class
}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DB_NAME = "autosentry.db";
    private static volatile AppDatabase INSTANCE;

    public abstract SessionDao sessionDao();
    public abstract PIDRecordDao pidRecordDao();
    public abstract PIDDefinitionDao pidDefinitionDao();
    public abstract AgentLogDao agentLogDao();
    public abstract MaintenanceDao maintenanceDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DB_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
