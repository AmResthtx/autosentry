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
public final class SessionDao_Impl implements SessionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Session> __insertionAdapterOfSession;

  private final EntityDeletionOrUpdateAdapter<Session> __updateAdapterOfSession;

  public SessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSession = new EntityInsertionAdapter<Session>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `sessions` (`id`,`startTime`,`endTime`,`adapterType`,`vehicleVin`,`notes`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Session entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.startTime);
        statement.bindLong(3, entity.endTime);
        if (entity.adapterType == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.adapterType);
        }
        if (entity.vehicleVin == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.vehicleVin);
        }
        if (entity.notes == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.notes);
        }
      }
    };
    this.__updateAdapterOfSession = new EntityDeletionOrUpdateAdapter<Session>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `sessions` SET `id` = ?,`startTime` = ?,`endTime` = ?,`adapterType` = ?,`vehicleVin` = ?,`notes` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Session entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.startTime);
        statement.bindLong(3, entity.endTime);
        if (entity.adapterType == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.adapterType);
        }
        if (entity.vehicleVin == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.vehicleVin);
        }
        if (entity.notes == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.notes);
        }
        statement.bindLong(7, entity.id);
      }
    };
  }

  @Override
  public long insert(final Session session) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfSession.insertAndReturnId(session);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final Session session) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfSession.handle(session);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<Session> latest(final int limit) {
    final String _sql = "SELECT * FROM sessions ORDER BY startTime DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
      final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
      final int _cursorIndexOfAdapterType = CursorUtil.getColumnIndexOrThrow(_cursor, "adapterType");
      final int _cursorIndexOfVehicleVin = CursorUtil.getColumnIndexOrThrow(_cursor, "vehicleVin");
      final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
      final List<Session> _result = new ArrayList<Session>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Session _item;
        _item = new Session();
        _item.id = _cursor.getLong(_cursorIndexOfId);
        _item.startTime = _cursor.getLong(_cursorIndexOfStartTime);
        _item.endTime = _cursor.getLong(_cursorIndexOfEndTime);
        if (_cursor.isNull(_cursorIndexOfAdapterType)) {
          _item.adapterType = null;
        } else {
          _item.adapterType = _cursor.getString(_cursorIndexOfAdapterType);
        }
        if (_cursor.isNull(_cursorIndexOfVehicleVin)) {
          _item.vehicleVin = null;
        } else {
          _item.vehicleVin = _cursor.getString(_cursorIndexOfVehicleVin);
        }
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
  public Session getById(final long id) {
    final String _sql = "SELECT * FROM sessions WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
      final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
      final int _cursorIndexOfAdapterType = CursorUtil.getColumnIndexOrThrow(_cursor, "adapterType");
      final int _cursorIndexOfVehicleVin = CursorUtil.getColumnIndexOrThrow(_cursor, "vehicleVin");
      final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
      final Session _result;
      if (_cursor.moveToFirst()) {
        _result = new Session();
        _result.id = _cursor.getLong(_cursorIndexOfId);
        _result.startTime = _cursor.getLong(_cursorIndexOfStartTime);
        _result.endTime = _cursor.getLong(_cursorIndexOfEndTime);
        if (_cursor.isNull(_cursorIndexOfAdapterType)) {
          _result.adapterType = null;
        } else {
          _result.adapterType = _cursor.getString(_cursorIndexOfAdapterType);
        }
        if (_cursor.isNull(_cursorIndexOfVehicleVin)) {
          _result.vehicleVin = null;
        } else {
          _result.vehicleVin = _cursor.getString(_cursorIndexOfVehicleVin);
        }
        if (_cursor.isNull(_cursorIndexOfNotes)) {
          _result.notes = null;
        } else {
          _result.notes = _cursor.getString(_cursorIndexOfNotes);
        }
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<Session> getAll() {
    final String _sql = "SELECT * FROM sessions ORDER BY startTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
      final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
      final int _cursorIndexOfAdapterType = CursorUtil.getColumnIndexOrThrow(_cursor, "adapterType");
      final int _cursorIndexOfVehicleVin = CursorUtil.getColumnIndexOrThrow(_cursor, "vehicleVin");
      final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
      final List<Session> _result = new ArrayList<Session>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Session _item;
        _item = new Session();
        _item.id = _cursor.getLong(_cursorIndexOfId);
        _item.startTime = _cursor.getLong(_cursorIndexOfStartTime);
        _item.endTime = _cursor.getLong(_cursorIndexOfEndTime);
        if (_cursor.isNull(_cursorIndexOfAdapterType)) {
          _item.adapterType = null;
        } else {
          _item.adapterType = _cursor.getString(_cursorIndexOfAdapterType);
        }
        if (_cursor.isNull(_cursorIndexOfVehicleVin)) {
          _item.vehicleVin = null;
        } else {
          _item.vehicleVin = _cursor.getString(_cursorIndexOfVehicleVin);
        }
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
