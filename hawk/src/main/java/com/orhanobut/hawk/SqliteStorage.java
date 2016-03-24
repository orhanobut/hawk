package com.orhanobut.hawk;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

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

    public SqliteHelper(Context context, String dbName) {
      super(context, dbName, null, VERSION);
    }

    @Override public void onCreate(SQLiteDatabase db) {
      db.execSQL("CREATE TABLE " + TABLE_NAME +
          " ( " + COL_KEY + " text primary key not null, " +
          COL_VALUE + " text null);");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public synchronized boolean put(String key, String value) {
      SQLiteDatabase db = this.getWritableDatabase();
      try {
        db.execSQL("INSERT OR REPLACE INTO " + TABLE_NAME +
                " (" + COL_KEY + ", " + COL_VALUE + ") " +
                " VALUES('" + key + "', '" + value + "')");
      } catch (SQLException ignored){
        return false;
      } finally {
        db.close();
      }
      return true;
    }

    public synchronized boolean put(List<Pair<String, ?>> list) {
      SQLiteDatabase db = this.getWritableDatabase();
      boolean result = true;
      try {
        db.beginTransaction();
        for (Pair<String, ?> pair : list) {
          db.execSQL("INSERT OR REPLACE INTO " + TABLE_NAME +
              " (" + COL_KEY + ", " + COL_VALUE + ") " +
              " VALUES('" + pair.first + "', '" + String.valueOf(pair.second) + "')");
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
        count = db.delete(TABLE_NAME, COL_KEY + "='" + key + "'", null);
      } finally {
        db.close();
      }
      return count != 0;
    }

    public synchronized boolean delete(String... keys) {
      SQLiteDatabase db = this.getWritableDatabase();
      boolean result = true;
      try {
        db.beginTransaction();
        for (String key : keys) {
          if (key == null) {
            continue;
          }
          db.delete(TABLE_NAME, COL_KEY + "='" + key + "'", null);
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

    public synchronized boolean contains(String key) {
      return get(key) != null;
    }

    public synchronized String get(String key) {
      SQLiteDatabase db = this.getReadableDatabase();
      Cursor cursor = null;
      String value = null;
      try {
        cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME +
                " WHERE " + COL_KEY + " = '" + key + "'", null);
        if (cursor == null) {
          return null;
        }
        if (!cursor.moveToFirst()) {
          return null;
        }
        value = cursor.getString(1);
      } finally {
        if(cursor != null) {
          cursor.close();
        }
        db.close();
      }
      return value;
    }

    public synchronized boolean clearAll() {
      SQLiteDatabase db = this.getWritableDatabase();
      try {
        db.execSQL("DELETE FROM " + TABLE_NAME);
      } catch (SQLException ignored){
        return false;
      } finally {
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
