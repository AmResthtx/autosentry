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
public final class AgentLogDao_Impl implements AgentLogDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AgentLog> __insertionAdapterOfAgentLog;

  public AgentLogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAgentLog = new EntityInsertionAdapter<AgentLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `agent_logs` (`id`,`timestamp`,`type`,`message`,`metadata`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final AgentLog entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.timestamp);
        if (entity.type == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.type);
        }
        if (entity.message == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.message);
        }
        if (entity.metadata == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.metadata);
        }
      }
    };
  }

  @Override
  public long insert(final AgentLog log) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfAgentLog.insertAndReturnId(log);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<AgentLog> latest(final int limit) {
    final String _sql = "SELECT * FROM agent_logs ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
      final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
      final List<AgentLog> _result = new ArrayList<AgentLog>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final AgentLog _item;
        _item = new AgentLog();
        _item.id = _cursor.getLong(_cursorIndexOfId);
        _item.timestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        if (_cursor.isNull(_cursorIndexOfType)) {
          _item.type = null;
        } else {
          _item.type = _cursor.getString(_cursorIndexOfType);
        }
        if (_cursor.isNull(_cursorIndexOfMessage)) {
          _item.message = null;
        } else {
          _item.message = _cursor.getString(_cursorIndexOfMessage);
        }
        if (_cursor.isNull(_cursorIndexOfMetadata)) {
          _item.metadata = null;
        } else {
          _item.metadata = _cursor.getString(_cursorIndexOfMetadata);
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
  public List<AgentLog> getLatest(final int limit) {
    final String _sql = "SELECT * FROM agent_logs ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
      final int _cursorIndexOfMetadata = CursorUtil.getColumnIndexOrThrow(_cursor, "metadata");
      final List<AgentLog> _result = new ArrayList<AgentLog>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final AgentLog _item;
        _item = new AgentLog();
        _item.id = _cursor.getLong(_cursorIndexOfId);
        _item.timestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        if (_cursor.isNull(_cursorIndexOfType)) {
          _item.type = null;
        } else {
          _item.type = _cursor.getString(_cursorIndexOfType);
        }
        if (_cursor.isNull(_cursorIndexOfMessage)) {
          _item.message = null;
        } else {
          _item.message = _cursor.getString(_cursorIndexOfMessage);
        }
        if (_cursor.isNull(_cursorIndexOfMetadata)) {
          _item.metadata = null;
        } else {
          _item.metadata = _cursor.getString(_cursorIndexOfMetadata);
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
