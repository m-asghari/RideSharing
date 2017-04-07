package edu.usc.infolab.geo.model;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class MyList<T> implements ParameterizedType {

  private Class<?> wrapped;

  public MyList(Class<T> wrapped) {
    this.wrapped = wrapped;
  }

  public Type[] getActualTypeArguments() {
    return new Type[] {wrapped};
  }

  public Type getRawType() {
    return List.class;
  }

  public Type getOwnerType() {
    return null;
  }

}
