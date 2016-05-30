package com.orhanobut.hawk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import android.util.Pair;

import java.util.Arrays;
import java.util.List;

class SqliteStorage implements Storage {

  private static final String DB_NAME = "Hawk";

  private final SqliteHelper helper;

  SqliteStorage(Context context) {
    HawkUtils.checkNull("Context", context);

    helper = new SqliteHelper(context, DB_NAME);
  }

  SqliteStorage(SqliteHelper sqliteHelper) {
    helper = sqliteHelper;
  }

  @Override public <T> boolean put(String key, T value) {
    HawkUtils.checkNullOrEmpty("key", key);
    return helper.put(key, String.valueOf(value));
  }

  @Override public boolean put(List<Pair<String, ?>> items) {
    return helper.put(items);
  }

  @SuppressWarnings("unchecked")
  @Override public <T> T get(String key) {
    HawkUtils.checkNullOrEmpty("key", key);
    return (T) helper.get(key);
  }

  @SuppressWarnings("SimplifiableIfStatement")
  @Override public boolean remove(String key) {
    if (HawkUtils.isEmpty(key)) {
      return true;
    }
    return helper.delete(key);
  }

  @Override public boolean remove(String... keys) {
    return helper.delete(keys);
  }

  @Override
  public boolean clear() {
    return helper.clearAll();
  }

  @Override
  public long count() {
    return helper.count();
  }

  @SuppressWarnings("SimplifiableIfStatement")
  @Override public boolean contains(String key) {
    if (key == null) {
      return false;
    }
    return helper.contains(key);
  }

  static class SqliteHelper extends SQLiteOpenHelper {

    private static final String TABLE_NAME = "hawk";
    private static final String COL_KEY = "hawk_key";
    private static final String COL_VALUE = "hawk_value";
    private static final int VERSION = 1;

    private static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
            " ( " + COL_KEY + " text primary key not null, " + COL_VALUE + " text null);";
    private static final String SQL_CLEAR_ALL = "DELETE FROM " + TABLE_NAME;
    private static final String SQL_DELETE_BY_KEY = COL_KEY + "=?";
    private static final String SQL_DELETE_IN_KEYS = COL_KEY + " IN(%s)";
    private static final String SQL_SELECT_BY_KEY = COL_KEY + "=?";


    public SqliteHelper(Context context, String dbName) {
      super(context, dbName, null, VERSION);
    }

    @Override public void onCreate(SQLiteDatabase db) {
      SQLiteStatement statement = db.compileStatement(SQL_CREATE_TABLE);
      statement.execute();
      statement.close();
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public synchronized boolean put(String key, String value) {
      SQLiteDatabase db = this.getWritableDatabase();
      ContentValues valueMap = new ContentValues();
      valueMap.put(COL_KEY, key);
      valueMap.put(COL_VALUE, value);
      return db.insertWithOnConflict(TABLE_NAME, null, valueMap, SQLiteDatabase.CONFLICT_REPLACE) != -1L;
    }

    public synchronized boolean put(List<Pair<String, ?>> list) {
      SQLiteDatabase db = this.getWritableDatabase();
      boolean result = true;
      ContentValues valueMap = new ContentValues();
      try {
        db.beginTransaction();
        for (Pair<String, ?> pair : list) {
          valueMap.clear();
          valueMap.put(COL_KEY, pair.first);
          valueMap.put(COL_VALUE, String.valueOf(pair.second));
          db.insertWithOnConflict(TABLE_NAME, null, valueMap, SQLiteDatabase.CONFLICT_REPLACE);
        }
        db.setTransactionSuccessful();
      } catch (Exception e) {
        result = false;
      } finally {
        db.endTransaction();
        db.close();
      }

      return result;
    }

    public synchronized boolean delete(String key) {
      SQLiteDatabase db = this.getWritableDatabase();
      int count = 0;
      try {
        count = db.delete(TABLE_NAME, SQL_DELETE_BY_KEY, new String[]{key});
      } finally {
        db.close();
      }
      return count != 0;
    }

    public synchronized boolean delete(String... keys) {
      if (keys == null || keys.length == 0) {
        return false;
      }

      if (keys.length == 1) {
        return delete(keys[0]);
      }

      String[] preparedHolder = new String[keys.length];
      Arrays.fill(preparedHolder, "?");
      SQLiteDatabase db = this.getWritableDatabase();
      boolean result = true;

      try {
        db.beginTransaction();
        db.delete(TABLE_NAME, String.format(SQL_DELETE_IN_KEYS, TextUtils.join(",", preparedHolder)), keys);
        db.setTransactionSuccessful();
      } catch (Exception e) {
        result = false;
      } finally {
        db.endTransaction();
        db.close();
      }
      return result;
    }

    public synchronized boolean contains(String key) {
      return get(key) != null;
    }

    public synchronized String get(String key) {
      SQLiteDatabase db = this.getReadableDatabase();
      Cursor cursor = null;
      String value = null;
      try {
        cursor = db.query(TABLE_NAME, null, SQL_SELECT_BY_KEY, new String[]{key}, null, null, COL_KEY);
        if (cursor == null) {
          return null;
        }
        if (!cursor.moveToFirst()) {
          return null;
        }
        value = cursor.getString(1);
      } finally {
        if (cursor != null) {
          cursor.close();
        }
        db.close();
      }
      return value;
    }

    public synchronized boolean clearAll() {
      SQLiteDatabase db = this.getWritableDatabase();
      SQLiteStatement statement = db.compileStatement(SQL_CLEAR_ALL);
      try {
          statement.execute();
      } catch (Exception ignored) {
        return false;
      } finally {
        statement.close();
        db.close();
      }
      return true;
    }

    public synchronized long count() {
      SQLiteDatabase db = this.getWritableDatabase();
      long count = 0;
      try {
        count = DatabaseUtils.queryNumEntries(db, TABLE_NAME);
      } finally {
        db.close();
      }
      return count;
    }
  }
}
