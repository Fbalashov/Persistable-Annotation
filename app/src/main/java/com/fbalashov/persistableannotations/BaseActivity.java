package com.fbalashov.persistableannotations;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Fuad.Balashov on 6/22/2016.
 */
public class BaseActivity extends AppCompatActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Right after super.onCreate we get the retained instance state and reinitiate any persisted fields.
    if (getLastCustomNonConfigurationInstance() instanceof Map) {
      reinitializePersistedFields(((Map<String, Object>) getLastCustomNonConfigurationInstance()));
    }

  }

  /**
   * Reinitializes any fields that have the Persistable annotation after state change
   */
  private void reinitializePersistedFields(Map<String, Object> persistedObjects) {
    // get all fields with the Persistable annotation
    List<Field> annotatedFields = getAllFieldsWithAnnotation(new ArrayList<Field>(), Persistable.class, this.getClass(), BaseActivity.class);
    for (Field field : annotatedFields) {
      // for each field, make the field accessible (in case it's private) and then set it's value based on the value in the map
      field.setAccessible(true);
      try {
        field.set(this, persistedObjects.get(field.getName()));
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Any field marked with this annotation will be persisted on state changes.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  protected @interface Persistable{}

  @Override
  public final Object onRetainCustomNonConfigurationInstance() {
    Map<String, Object> objectsToPersist = new HashMap<>();
    // get all fields with the Persistable annotation
    List<Field> annotatedFields = getAllFieldsWithAnnotation(new ArrayList<Field>(), Persistable.class, this.getClass(), BaseActivity.class);
    for (Field field : annotatedFields) {
      // for each field, make the field accessible (in case it's private) and then write its value into the map
      field.setAccessible(true);
      try {
        objectsToPersist.put(field.getName(), field.get(this));
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    return objectsToPersist;
  }

  /**
   * Finds all the fields with the given annotation (public and private0 in the given class. Recursively repeats
   * this for super classes until reaching the given endClass.
   * @param annotatedFields an empty list that will be populated with any fields that match the annotationClazz
   * @param annotationClass the annotation to look for
   * @param startClazz the starting subclass to look for fields in
   * @param endClazz the super class at which to stop looking for fields
   * @return all fields with the given annotation
   */
  private List<Field> getAllFieldsWithAnnotation(List<Field> annotatedFields, Class<Persistable> annotationClass, Class startClazz, Class<BaseActivity> endClazz) {
    Field[] fields = startClazz.getDeclaredFields();
    for (Field field : fields) {
      if(field.isAnnotationPresent(annotationClass)) {
        annotatedFields.add(field);
      }
    }
    if (endClazz.equals(startClazz)) {
      return annotatedFields;
    }
    return getAllFieldsWithAnnotation(annotatedFields, annotationClass, startClazz.getSuperclass(), endClazz);
  }
}
