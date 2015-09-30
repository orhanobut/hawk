package com.orhanobut.hawk;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Pair;

import java.util.List;

class SqliteStorage implements Storage {

  private final SqliteHelper helper;

  SqliteStorage(Context context) {
    if (context == null) {
      throw new NullPointerException("Context should not be null");
    }
    helper = new SqliteHelper(context);
  }

  @Override public <T> boolean put(String key, T value) {
    checkKey(key);
    return helper.put(key, String.valueOf(value));
  }

  @Override public boolean put(List<Pair<String, ?>> items) {
    return helper.put(items);
  }

  @SuppressWarnings("unchecked")
  @Override public <T> T get(String key) {
    checkKey(key);
    return (T) helper.get(key);
  }

  @SuppressWarnings("SimplifiableIfStatement")
  @Override public boolean remove(String key) {
    if (TextUtils.isEmpty(key)) {
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

  private void checkKey(String key) {
    if (TextUtils.isEmpty(key)) {
      throw new NullPointerException("Key cannot be null or empty");
    }
  }

  private static class SqliteHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "Hawk";
    private static final String TABLE_NAME = "hawk";
    private static final String COL_KEY = "hawk_key";
    private static final String COL_VALUE = "hawk_value";
    private static final int VERSION = 1;

    public SqliteHelper(Context context) {
      super(context, DB_NAME, null, VERSION);
    }

    @Override public void onCreate(SQLiteDatabase db) {
      db.execSQL("CREATE TABLE " + TABLE_NAME +
          " ( " + COL_KEY + " text primary key not null, " +
          COL_VALUE + " text null);");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public boolean put(String key, String value) {
      SQLiteDatabase db = this.getWritableDatabase();
      db.execSQL("INSERT OR REPLACE INTO " + TABLE_NAME +
          " (" + COL_KEY + ", " + COL_VALUE + ") " +
          " VALUES('" + key + "', '" + value + "')");
      db.close();
      return true;
    }

    public boolean put(List<Pair<String, ?>> list) {
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

    public boolean delete(String key) {
      SQLiteDatabase db = this.getWritableDatabase();
      int count = db.delete(TABLE_NAME, COL_KEY + "='" + key + "'", null);
      db.close();
      return count != -1;
    }

    public boolean delete(String... keys) {
      SQLiteDatabase db = this.getWritableDatabase();
      boolean result = true;
      try {
        db.beginTransaction();
        for (String key : keys) {
          if (key == null) {
            continue;
          }
          int count = db.delete(TABLE_NAME, COL_KEY + "='" + key + "'", null);
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

    public boolean contains(String key) {
      return get(key) != null;
    }

    public String get(String key) {
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
      db.close();
      return value;
    }

    public boolean clearAll() {
      SQLiteDatabase db = this.getWritableDatabase();
      db.execSQL("DELETE FROM " + TABLE_NAME);
      db.close();
      return true;
    }

    public long count() {
      SQLiteDatabase db = this.getWritableDatabase();
      long count = DatabaseUtils.queryNumEntries(db, TABLE_NAME);
      db.close();
      return count;
    }
  }
}
