package io.github.johntortoise.core.utils;

import java.util.Collection;
import java.util.Map;


public class EmptyUtil {
    
    private EmptyUtil() {
    }
    

    public static boolean isStringEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    

    public static boolean isStringNotEmpty(String str) {
        return !isStringEmpty(str);
    }
    

    public static boolean isCollectionEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
    

    public static boolean isCollectionNotEmpty(Collection<?> collection) {
        return !isCollectionEmpty(collection);
    }
    

    public static boolean isArrayEmpty(Object[] array) {
        return array == null || array.length == 0;
    }
    

    public static boolean isArrayNotEmpty(Object[] array) {
        return !isArrayEmpty(array);
    }
    

    public static boolean isMapEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
    

    public static boolean isMapNotEmpty(Map<?, ?> map) {
        return !isMapEmpty(map);
    }
    

    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        
        if (obj instanceof String) {
            return isStringEmpty((String) obj);
        }
        
        if (obj instanceof Collection) {
            return isCollectionEmpty((Collection<?>) obj);
        }
        
        if (obj instanceof Map) {
            return isMapEmpty((Map<?, ?>) obj);
        }
        
        if (obj.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(obj) == 0;
        }
        
        return false;
    }
    

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }
}