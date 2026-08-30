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
public final class PIDDefinitionDao_Impl implements PIDDefinitionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PIDDefinition> __insertionAdapterOfPIDDefinition;

  private final EntityDeletionOrUpdateAdapter<PIDDefinition> __deletionAdapterOfPIDDefinition;

  private final EntityDeletionOrUpdateAdapter<PIDDefinition> __updateAdapterOfPIDDefinition;

  public PIDDefinitionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPIDDefinition = new EntityInsertionAdapter<PIDDefinition>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `pid_definitions` (`id`,`name`,`command`,`pollIntervalSeconds`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final PIDDefinition entity) {
        statement.bindLong(1, entity.id);
        if (entity.name == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.name);
        }
        if (entity.command == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.command);
        }
        statement.bindLong(4, entity.pollIntervalSeconds);
      }
    };
    this.__deletionAdapterOfPIDDefinition = new EntityDeletionOrUpdateAdapter<PIDDefinition>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `pid_definitions` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final PIDDefinition entity) {
        statement.bindLong(1, entity.id);
      }
    };
    this.__updateAdapterOfPIDDefinition = new EntityDeletionOrUpdateAdapter<PIDDefinition>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `pid_definitions` SET `id` = ?,`name` = ?,`command` = ?,`pollIntervalSeconds` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final PIDDefinition entity) {
        statement.bindLong(1, entity.id);
        if (entity.name == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.name);
        }
        if (entity.command == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.command);
        }
        statement.bindLong(4, entity.pollIntervalSeconds);
        statement.bindLong(5, entity.id);
      }
    };
  }

  @Override
  public long insert(final PIDDefinition def) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfPIDDefinition.insertAndReturnId(def);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final PIDDefinition def) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfPIDDefinition.handle(def);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final PIDDefinition def) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfPIDDefinition.handle(def);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<PIDDefinition> all() {
    final String _sql = "SELECT * FROM pid_definitions ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfCommand = CursorUtil.getColumnIndexOrThrow(_cursor, "command");
      final int _cursorIndexOfPollIntervalSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "pollIntervalSeconds");
      final List<PIDDefinition> _result = new ArrayList<PIDDefinition>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final PIDDefinition _item;
        _item = new PIDDefinition();
        _item.id = _cursor.getLong(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfName)) {
          _item.name = null;
        } else {
          _item.name = _cursor.getString(_cursorIndexOfName);
        }
        if (_cursor.isNull(_cursorIndexOfCommand)) {
          _item.command = null;
        } else {
          _item.command = _cursor.getString(_cursorIndexOfCommand);
        }
        _item.pollIntervalSeconds = _cursor.getInt(_cursorIndexOfPollIntervalSeconds);
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
