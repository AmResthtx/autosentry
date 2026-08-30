package com.autosentry.app.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MaintenanceDao_Impl implements MaintenanceDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MaintenanceEvent> __insertionAdapterOfMaintenanceEvent;

  private final EntityDeletionOrUpdateAdapter<MaintenanceEvent> __deletionAdapterOfMaintenanceEvent;

  private final EntityDeletionOrUpdateAdapter<MaintenanceEvent> __updateAdapterOfMaintenanceEvent;

  public MaintenanceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMaintenanceEvent = new EntityInsertionAdapter<MaintenanceEvent>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `maintenance_events` (`id`,`title`,`eventType`,`eventTime`,`lastServiceAt`,`nextServiceAt`,`intervalKm`,`intervalMonths`,`odometerAtLastService`,`notes`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final MaintenanceEvent entity) {
        statement.bindLong(1, entity.id);
        if (entity.title == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.title);
        }
        if (entity.eventType == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.eventType);
        }
        statement.bindLong(4, entity.eventTime);
        statement.bindLong(5, entity.lastServiceAt);
        statement.bindLong(6, entity.nextServiceAt);
        statement.bindLong(7, entity.intervalKm);
        statement.bindLong(8, entity.intervalMonths);
        statement.bindLong(9, entity.odometerAtLastService);
        if (entity.notes == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.notes);
        }
      }
    };
    this.__deletionAdapterOfMaintenanceEvent = new EntityDeletionOrUpdateAdapter<MaintenanceEvent>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `maintenance_events` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final MaintenanceEvent entity) {
        statement.bindLong(1, entity.id);
      }
    };
    this.__updateAdapterOfMaintenanceEvent = new EntityDeletionOrUpdateAdapter<MaintenanceEvent>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `maintenance_events` SET `id` = ?,`title` = ?,`eventType` = ?,`eventTime` = ?,`lastServiceAt` = ?,`nextServiceAt` = ?,`intervalKm` = ?,`intervalMonths` = ?,`odometerAtLastService` = ?,`notes` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final MaintenanceEvent entity) {
        statement.bindLong(1, entity.id);
        if (entity.title == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.title);
        }
        if (entity.eventType == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.eventType);
        }
        statement.bindLong(4, entity.eventTime);
        statement.bindLong(5, entity.lastServiceAt);
        statement.bindLong(6, entity.nextServiceAt);
        statement.bindLong(7, entity.intervalKm);
        statement.bindLong(8, entity.intervalMonths);
        statement.bindLong(9, entity.odometerAtLastService);
        if (entity.notes == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.notes);
        }
        statement.bindLong(11, entity.id);
      }
    };
  }

  @Override
  public long insert(final MaintenanceEvent evt) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfMaintenanceEvent.insertAndReturnId(evt);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final MaintenanceEvent evt) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfMaintenanceEvent.handle(evt);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final MaintenanceEvent evt) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfMaintenanceEvent.handle(evt);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<MaintenanceEvent> dueSoon() {
    final String _sql = "SELECT * FROM maintenance_events ORDER BY nextServiceAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfEventType = CursorUtil.getColumnIndexOrThrow(_cursor, "eventType");
      final int _cursorIndexOfEventTime = CursorUtil.getColumnIndexOrThrow(_cursor, "eventTime");
      final int _cursorIndexOfLastServiceAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastServiceAt");
      final int _cursorIndexOfNextServiceAt = CursorUtil.getColumnIndexOrThrow(_cursor, "nextServiceAt");
      final int _cursorIndexOfIntervalKm = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalKm");
      final int _cursorIndexOfIntervalMonths = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalMonths");
      final int _cursorIndexOfOdometerAtLastService = CursorUtil.getColumnIndexOrThrow(_cursor, "odometerAtLastService");
      final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
      final List<MaintenanceEvent> _result = new ArrayList<MaintenanceEvent>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final MaintenanceEvent _item;
        _item = new MaintenanceEvent();
        _item.id = _cursor.getLong(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfTitle)) {
          _item.title = null;
        } else {
          _item.title = _cursor.getString(_cursorIndexOfTitle);
        }
        if (_cursor.isNull(_cursorIndexOfEventType)) {
          _item.eventType = null;
        } else {
          _item.eventType = _cursor.getString(_cursorIndexOfEventType);
        }
        _item.eventTime = _cursor.getLong(_cursorIndexOfEventTime);
        _item.lastServiceAt = _cursor.getLong(_cursorIndexOfLastServiceAt);
        _item.nextServiceAt = _cursor.getLong(_cursorIndexOfNextServiceAt);
        _item.intervalKm = _cursor.getInt(_cursorIndexOfIntervalKm);
        _item.intervalMonths = _cursor.getInt(_cursorIndexOfIntervalMonths);
        _item.odometerAtLastService = _cursor.getInt(_cursorIndexOfOdometerAtLastService);
        if (_cursor.isNull(_cursorIndexOfNotes)) {
          _item.notes = null;
        } else {
          _item.notes = _cursor.getString(_cursorIndexOfNotes);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<MaintenanceEvent> getLatestByType(final String eventType) {
    final String _sql = "SELECT * FROM maintenance_events WHERE eventType = ? ORDER BY eventTime DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (eventType == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, eventType);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfEventType = CursorUtil.getColumnIndexOrThrow(_cursor, "eventType");
      final int _cursorIndexOfEventTime = CursorUtil.getColumnIndexOrThrow(_cursor, "eventTime");
      final int _cursorIndexOfLastServiceAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastServiceAt");
      final int _cursorIndexOfNextServiceAt = CursorUtil.getColumnIndexOrThrow(_cursor, "nextServiceAt");
      final int _cursorIndexOfIntervalKm = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalKm");
      final int _cursorIndexOfIntervalMonths = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalMonths");
      final int _cursorIndexOfOdometerAtLastService = CursorUtil.getColumnIndexOrThrow(_cursor, "odometerAtLastService");
      final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
      final List<MaintenanceEvent> _result = new ArrayList<MaintenanceEvent>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final MaintenanceEvent _item;
        _item = new MaintenanceEvent();
        _item.id = _cursor.getLong(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfTitle)) {
          _item.title = null;
        } else {
          _item.title = _cursor.getString(_cursorIndexOfTitle);
        }
        if (_cursor.isNull(_cursorIndexOfEventType)) {
          _item.eventType = null;
        } else {
          _item.eventType = _cursor.getString(_cursorIndexOfEventType);
        }
        _item.eventTime = _cursor.getLong(_cursorIndexOfEventTime);
        _item.lastServiceAt = _cursor.getLong(_cursorIndexOfLastServiceAt);
        _item.nextServiceAt = _cursor.getLong(_cursorIndexOfNextServiceAt);
        _item.intervalKm = _cursor.getInt(_cursorIndexOfIntervalKm);
        _item.intervalMonths = _cursor.getInt(_cursorIndexOfIntervalMonths);
        _item.odometerAtLastService = _cursor.getInt(_cursorIndexOfOdometerAtLastService);
        if (_cursor.isNull(_cursorIndexOfNotes)) {
          _item.notes = null;
        } else {
          _item.notes = _cursor.getString(_cursorIndexOfNotes);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
