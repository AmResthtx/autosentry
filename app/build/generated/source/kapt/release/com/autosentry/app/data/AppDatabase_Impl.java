package com.autosentry.app.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile SessionDao _sessionDao;

  private volatile PIDRecordDao _pIDRecordDao;

  private volatile PIDDefinitionDao _pIDDefinitionDao;

  private volatile AgentLogDao _agentLogDao;

  private volatile MaintenanceDao _maintenanceDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, `adapterType` TEXT, `vehicleVin` TEXT, `notes` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `pid_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `pidName` TEXT, `pidValue` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `sessionId` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `pid_definitions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `command` TEXT, `pollIntervalSeconds` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `agent_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `type` TEXT, `message` TEXT, `metadata` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `maintenance_events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT, `eventType` TEXT, `eventTime` INTEGER NOT NULL, `lastServiceAt` INTEGER NOT NULL, `nextServiceAt` INTEGER NOT NULL, `intervalKm` INTEGER NOT NULL, `intervalMonths` INTEGER NOT NULL, `odometerAtLastService` INTEGER NOT NULL, `notes` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'c74889258f1ed5c38c35f53b57c90229')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `sessions`");
        db.execSQL("DROP TABLE IF EXISTS `pid_records`");
        db.execSQL("DROP TABLE IF EXISTS `pid_definitions`");
        db.execSQL("DROP TABLE IF EXISTS `agent_logs`");
        db.execSQL("DROP TABLE IF EXISTS `maintenance_events`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsSessions = new HashMap<String, TableInfo.Column>(6);
        _columnsSessions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessions.put("startTime", new TableInfo.Column("startTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessions.put("endTime", new TableInfo.Column("endTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessions.put("adapterType", new TableInfo.Column("adapterType", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessions.put("vehicleVin", new TableInfo.Column("vehicleVin", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessions.put("notes", new TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSessions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSessions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSessions = new TableInfo("sessions", _columnsSessions, _foreignKeysSessions, _indicesSessions);
        final TableInfo _existingSessions = TableInfo.read(db, "sessions");
        if (!_infoSessions.equals(_existingSessions)) {
          return new RoomOpenHelper.ValidationResult(false, "sessions(com.autosentry.app.data.Session).\n"
                  + " Expected:\n" + _infoSessions + "\n"
                  + " Found:\n" + _existingSessions);
        }
        final HashMap<String, TableInfo.Column> _columnsPidRecords = new HashMap<String, TableInfo.Column>(5);
        _columnsPidRecords.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPidRecords.put("pidName", new TableInfo.Column("pidName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPidRecords.put("pidValue", new TableInfo.Column("pidValue", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPidRecords.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPidRecords.put("sessionId", new TableInfo.Column("sessionId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPidRecords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPidRecords = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPidRecords = new TableInfo("pid_records", _columnsPidRecords, _foreignKeysPidRecords, _indicesPidRecords);
        final TableInfo _existingPidRecords = TableInfo.read(db, "pid_records");
        if (!_infoPidRecords.equals(_existingPidRecords)) {
          return new RoomOpenHelper.ValidationResult(false, "pid_records(com.autosentry.app.data.PIDRecord).\n"
                  + " Expected:\n" + _infoPidRecords + "\n"
                  + " Found:\n" + _existingPidRecords);
        }
        final HashMap<String, TableInfo.Column> _columnsPidDefinitions = new HashMap<String, TableInfo.Column>(4);
        _columnsPidDefinitions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPidDefinitions.put("name", new TableInfo.Column("name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPidDefinitions.put("command", new TableInfo.Column("command", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPidDefinitions.put("pollIntervalSeconds", new TableInfo.Column("pollIntervalSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPidDefinitions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPidDefinitions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPidDefinitions = new TableInfo("pid_definitions", _columnsPidDefinitions, _foreignKeysPidDefinitions, _indicesPidDefinitions);
        final TableInfo _existingPidDefinitions = TableInfo.read(db, "pid_definitions");
        if (!_infoPidDefinitions.equals(_existingPidDefinitions)) {
          return new RoomOpenHelper.ValidationResult(false, "pid_definitions(com.autosentry.app.data.PIDDefinition).\n"
                  + " Expected:\n" + _infoPidDefinitions + "\n"
                  + " Found:\n" + _existingPidDefinitions);
        }
        final HashMap<String, TableInfo.Column> _columnsAgentLogs = new HashMap<String, TableInfo.Column>(5);
        _columnsAgentLogs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAgentLogs.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAgentLogs.put("type", new TableInfo.Column("type", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAgentLogs.put("message", new TableInfo.Column("message", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAgentLogs.put("metadata", new TableInfo.Column("metadata", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAgentLogs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAgentLogs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAgentLogs = new TableInfo("agent_logs", _columnsAgentLogs, _foreignKeysAgentLogs, _indicesAgentLogs);
        final TableInfo _existingAgentLogs = TableInfo.read(db, "agent_logs");
        if (!_infoAgentLogs.equals(_existingAgentLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "agent_logs(com.autosentry.app.data.AgentLog).\n"
                  + " Expected:\n" + _infoAgentLogs + "\n"
                  + " Found:\n" + _existingAgentLogs);
        }
        final HashMap<String, TableInfo.Column> _columnsMaintenanceEvents = new HashMap<String, TableInfo.Column>(10);
        _columnsMaintenanceEvents.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceEvents.put("title", new TableInfo.Column("title", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceEvents.put("eventType", new TableInfo.Column("eventType", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceEvents.put("eventTime", new TableInfo.Column("eventTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceEvents.put("lastServiceAt", new TableInfo.Column("lastServiceAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceEvents.put("nextServiceAt", new TableInfo.Column("nextServiceAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceEvents.put("intervalKm", new TableInfo.Column("intervalKm", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceEvents.put("intervalMonths", new TableInfo.Column("intervalMonths", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceEvents.put("odometerAtLastService", new TableInfo.Column("odometerAtLastService", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceEvents.put("notes", new TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMaintenanceEvents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMaintenanceEvents = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMaintenanceEvents = new TableInfo("maintenance_events", _columnsMaintenanceEvents, _foreignKeysMaintenanceEvents, _indicesMaintenanceEvents);
        final TableInfo _existingMaintenanceEvents = TableInfo.read(db, "maintenance_events");
        if (!_infoMaintenanceEvents.equals(_existingMaintenanceEvents)) {
          return new RoomOpenHelper.ValidationResult(false, "maintenance_events(com.autosentry.app.data.MaintenanceEvent).\n"
                  + " Expected:\n" + _infoMaintenanceEvents + "\n"
                  + " Found:\n" + _existingMaintenanceEvents);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "c74889258f1ed5c38c35f53b57c90229", "bb26481d4704cdca067e02b07da47ec8");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "sessions","pid_records","pid_definitions","agent_logs","maintenance_events");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `sessions`");
      _db.execSQL("DELETE FROM `pid_records`");
      _db.execSQL("DELETE FROM `pid_definitions`");
      _db.execSQL("DELETE FROM `agent_logs`");
      _db.execSQL("DELETE FROM `maintenance_events`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(SessionDao.class, SessionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PIDRecordDao.class, PIDRecordDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PIDDefinitionDao.class, PIDDefinitionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AgentLogDao.class, AgentLogDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MaintenanceDao.class, MaintenanceDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public SessionDao sessionDao() {
    if (_sessionDao != null) {
      return _sessionDao;
    } else {
      synchronized(this) {
        if(_sessionDao == null) {
          _sessionDao = new SessionDao_Impl(this);
        }
        return _sessionDao;
      }
    }
  }

  @Override
  public PIDRecordDao pidRecordDao() {
    if (_pIDRecordDao != null) {
      return _pIDRecordDao;
    } else {
      synchronized(this) {
        if(_pIDRecordDao == null) {
          _pIDRecordDao = new PIDRecordDao_Impl(this);
        }
        return _pIDRecordDao;
      }
    }
  }

  @Override
  public PIDDefinitionDao pidDefinitionDao() {
    if (_pIDDefinitionDao != null) {
      return _pIDDefinitionDao;
    } else {
      synchronized(this) {
        if(_pIDDefinitionDao == null) {
          _pIDDefinitionDao = new PIDDefinitionDao_Impl(this);
        }
        return _pIDDefinitionDao;
      }
    }
  }

  @Override
  public AgentLogDao agentLogDao() {
    if (_agentLogDao != null) {
      return _agentLogDao;
    } else {
      synchronized(this) {
        if(_agentLogDao == null) {
          _agentLogDao = new AgentLogDao_Impl(this);
        }
        return _agentLogDao;
      }
    }
  }

  @Override
  public MaintenanceDao maintenanceDao() {
    if (_maintenanceDao != null) {
      return _maintenanceDao;
    } else {
      synchronized(this) {
        if(_maintenanceDao == null) {
          _maintenanceDao = new MaintenanceDao_Impl(this);
        }
        return _maintenanceDao;
      }
    }
  }
}
