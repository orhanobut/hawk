package com.orhanobut.hawk;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SqliteStorage extends Storage {

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

  @Override public synchronized Map<String, ?> getAll() {
    return helper.getAll();
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

  @Override public boolean clear() {
    return helper.clearAll();
  }

  @Override public long count() {
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
      db.execSQL("INSERT OR REPLACE INTO " + TABLE_NAME +
              " (" + COL_KEY + ", " + COL_VALUE + ") " +
              " VALUES('" + key + "', '" + value + "')");
      db.close();
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
      int count = db.delete(TABLE_NAME, COL_KEY + "='" + key + "'", null);
      db.close();
      return count != -1;
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
      Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME +
              " WHERE " + COL_KEY + " = '" + key + "'", null);
      if (cursor == null) {
        return null;
      }
      cursor.moveToFirst();
      if (cursor.getCount() == 0) {
        return null;
      }
      String value = cursor.getString(1);
      cursor.close();
      db.close();
      return value;
    }

    public synchronized Map<String, String> getAll() {
      SQLiteDatabase db = this.getReadableDatabase();
      Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
      if (cursor == null) {
        return null;
      }
      cursor.moveToFirst();
      if (cursor.getCount() == 0) {
        return null;
      }

      final Map<String, String> results = new HashMap();

      do {
        results.put(cursor.getString(0), cursor.getString(1));
      } while (cursor.moveToNext());

      cursor.close();
      db.close();
      return results;
    }

    public synchronized boolean clearAll() {
      SQLiteDatabase db = this.getWritableDatabase();
      db.execSQL("DELETE FROM " + TABLE_NAME);
      db.close();
      return true;
    }

    public synchronized long count() {
      SQLiteDatabase db = this.getWritableDatabase();
      long count = DatabaseUtils.queryNumEntries(db, TABLE_NAME);
      db.close();
      return count;
    }
  }
}
