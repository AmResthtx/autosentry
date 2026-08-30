package com.autosentry.app.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
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
public final class PIDRecordDao_Impl implements PIDRecordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PIDRecord> __insertionAdapterOfPIDRecord;

  public PIDRecordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPIDRecord = new EntityInsertionAdapter<PIDRecord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `pid_records` (`id`,`pidName`,`pidValue`,`timestamp`,`sessionId`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final PIDRecord entity) {
        statement.bindLong(1, entity.id);
        if (entity.pidName == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.pidName);
        }
        statement.bindLong(3, entity.pidValue);
        statement.bindLong(4, entity.timestamp);
        statement.bindLong(5, entity.sessionId);
      }
    };
  }

  @Override
  public long insert(final PIDRecord record) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfPIDRecord.insertAndReturnId(record);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<PIDRecord> latest(final int limit) {
    final String _sql = "SELECT * FROM pid_records ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfPidName = CursorUtil.getColumnIndexOrThrow(_cursor, "pidName");
      final int _cursorIndexOfPidValue = CursorUtil.getColumnIndexOrThrow(_cursor, "pidValue");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
      final List<PIDRecord> _result = new ArrayList<PIDRecord>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final PIDRecord _item;
        _item = new PIDRecord();
        _item.id = _cursor.getLong(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfPidName)) {
          _item.pidName = null;
        } else {
          _item.pidName = _cursor.getString(_cursorIndexOfPidName);
        }
        _item.pidValue = _cursor.getInt(_cursorIndexOfPidValue);
        _item.timestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _item.sessionId = _cursor.getLong(_cursorIndexOfSessionId);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<PIDRecord> latestByName(final String name, final int limit) {
    final String _sql = "SELECT * FROM pid_records WHERE pidName = ? ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (name == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, name);
    }
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfPidName = CursorUtil.getColumnIndexOrThrow(_cursor, "pidName");
      final int _cursorIndexOfPidValue = CursorUtil.getColumnIndexOrThrow(_cursor, "pidValue");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
      final List<PIDRecord> _result = new ArrayList<PIDRecord>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final PIDRecord _item;
        _item = new PIDRecord();
        _item.id = _cursor.getLong(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfPidName)) {
          _item.pidName = null;
        } else {
          _item.pidName = _cursor.getString(_cursorIndexOfPidName);
        }
        _item.pidValue = _cursor.getInt(_cursorIndexOfPidValue);
        _item.timestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _item.sessionId = _cursor.getLong(_cursorIndexOfSessionId);
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
