package com.orhanobut.hawk;

enum DataType {
  OBJECT('0'),
  LIST('1'),
  MAP('2'),
  SET('3');

  private final char type;

  DataType(char type) {
    this.type = type;
  }

  @Override public String toString() {
    return super.toString();
  }

  char getType() {
    return type;
  }
}