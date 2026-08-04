package com.orhanobut.hawk.sample;

import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.LogInterceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal demo of the Hawk Next API, written in Java to prove Java compatibility.
 */
public class MainActivity extends AppCompatActivity {

  private TextView logView;
  private LogInterceptor logInterceptor;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    logView = findViewById(R.id.log);

    logInterceptor = new LogInterceptor() {
      @Override public void onLog(String message) {
        log("Hawk: " + message);
      }
    };

    Hawk.init(this)
        .setLogInterceptor(logInterceptor)
        .build();
    log("Hawk initialized. Encryption: " + encryptionName());

    ((Button) findViewById(R.id.put)).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        demoPutGet();
      }
    });
    ((Button) findViewById(R.id.contains_count_keys)).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        demoContainsCountKeys();
      }
    });
    ((Button) findViewById(R.id.delete)).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Hawk.delete("name");
        log("Hawk.delete(\"name\") -> contains(name) = " + Hawk.contains("name"));
      }
    });
    ((Button) findViewById(R.id.delete_all)).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Hawk.deleteAll();
        log("Hawk.deleteAll() -> count = " + Hawk.count());
      }
    });
    ((Button) findViewById(R.id.migrate)).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        demoMigration();
      }
    });
  }

  private void demoPutGet() {
    Hawk.put("name", "Jack");
    Hawk.put("age", 30);
    Hawk.put("enabled", true);
    Hawk.put("user", new User("Hawk", 2));

    List<String> tags = new ArrayList<>();
    tags.add("one");
    tags.add("two");
    Hawk.put("tags", tags);

    log("put: name=Jack, age=30, enabled=true, user=User{Hawk,2}, tags=[one,two]");
    log("get name  = " + Hawk.<String>get("name"));
    log("get age   = " + Hawk.<Integer>get("age"));
    log("get user  = " + Hawk.<User>get("user"));
    log("get tags  = " + Hawk.<List<String>>get("tags"));
    log("get missing (default) = " + Hawk.get("missing", "fallback"));
  }

  private void demoContainsCountKeys() {
    log("contains(name) = " + Hawk.contains("name"));
    log("count() = " + Hawk.count());
    log("keys() = " + Hawk.keys());
  }

  private void demoMigration() {
    // Simulate data written by Hawk 2.x (NoEncryption format: Base64 of the Gson JSON).
    String legacySerialized = "java.lang.String##0V@"
        + Base64.encodeToString("\"Hello from Hawk 2.x\"".getBytes(), Base64.DEFAULT);
    getSharedPreferences("Hawk2", MODE_PRIVATE)
        .edit()
        .putString("legacyGreeting", legacySerialized)
        .commit();

    // Clear the migration flag so the one-time migration runs again for this demo.
    getSharedPreferences("Hawk2Migration", MODE_PRIVATE)
        .edit()
        .remove("migrated")
        .commit();

    log("Seeded a legacy Hawk 2.x entry in SharedPreferences, re-initializing Hawk...");
    Hawk.init(this)
        .setLogInterceptor(logInterceptor)
        .build();
    log("After migration: legacyGreeting = " + Hawk.<String>get("legacyGreeting"));
  }

  private String encryptionName() {
    // Keystore AES-GCM on real devices; NoEncryption fallback in emulators/tests without Keystore.
    return "Android Keystore + AES-GCM (NoEncryption fallback if unavailable)";
  }

  private void log(String message) {
    String current = logView.getText().toString();
    logView.setText(current.length() == 0 ? message : current + "\n" + message);
  }

  public static class User {
    private String name;
    private int id;

    public User() {
    }

    public User(String name, int id) {
      this.name = name;
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public int getId() {
      return id;
    }

    @Override public String toString() {
      return "User{" + name + "," + id + "}";
    }
  }
}
