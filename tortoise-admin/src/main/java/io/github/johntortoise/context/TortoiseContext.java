package io.github.johntortoise.context;



public class TortoiseContext {

    private static final ThreadLocal<Long> USER_ID_THREAD_LOCAL = new ThreadLocal<>();


    
    public static void setCurrentUserId(Long userId) {
        USER_ID_THREAD_LOCAL.set(userId);
    }

    
    public static Long getCurrentUserId() {
        return USER_ID_THREAD_LOCAL.get();
    }




    
    public static void clear() {
        USER_ID_THREAD_LOCAL.remove();
    }
}