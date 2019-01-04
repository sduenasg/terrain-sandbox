package com.sdgapps.terrainsandbox.utils;

import java.lang.reflect.Field;

public class AndroidUtils {

    /**
     * Returns the id of a resource by supplying its path
     *
     * @param variableName Android resource path
     * @param c            Example: R.drawable
     */
    public static int getResId(String variableName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(variableName);
            return idField.getInt(idField);
        } catch (Exception e) {
            // e.printStackTrace();
            return -1;
        }
    }
}
