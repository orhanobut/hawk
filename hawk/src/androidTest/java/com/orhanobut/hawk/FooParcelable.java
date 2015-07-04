package com.orhanobut.hawk;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Orhan Obut
 */
public class FooParcelable implements Parcelable {

  private String foo = "test";

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.foo);
  }

  public FooParcelable() {
  }

  private FooParcelable(Parcel in) {
    this.foo = in.readString();
  }

  public static final Creator<FooParcelable> CREATOR = new Creator<FooParcelable>() {
    public FooParcelable createFromParcel(Parcel source) {
      return new FooParcelable(source);
    }

    public FooParcelable[] newArray(int size) {
      return new FooParcelable[size];
    }
  };
}
