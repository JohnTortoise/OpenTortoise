package io.github.johntortoise.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class SimpleLock {
    private static final ConcurrentHashMap<String, Long> LOCK_STORE = new ConcurrentHashMap<>();
    

    private static final ScheduledExecutorService CLEANER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "SimpleLock-Cleaner");
        t.setDaemon(true);
        return t;
    });
    
    static {

        CLEANER.scheduleAtFixedRate(SimpleLock::cleanExpired, 5, 5, TimeUnit.SECONDS);
        

        Runtime.getRuntime().addShutdownHook(new Thread(CLEANER::shutdown));
    }
    

    public static boolean tryLock(String key, long expireSeconds) {
        long now = System.currentTimeMillis();
        long expireAt = now + expireSeconds * 1000;
        
        Long existing = LOCK_STORE.putIfAbsent(key, expireAt);
        
        if (existing == null) {
            return true;
        }
        

        if (existing < now) {

            return LOCK_STORE.replace(key, existing, expireAt);
        }
        
        return false;
    }
    

    public static boolean unlock(String key) {
        Long expireTime = LOCK_STORE.get(key);
        if (expireTime != null && expireTime > System.currentTimeMillis()) {
            return LOCK_STORE.remove(key, expireTime);
        }
        return false;
    }
    
    
    public static AutoLock lock(String key, long expireSeconds) {
        if (tryLock(key, expireSeconds)) {
            return new AutoLock(key);
        }
        return null;
    }
    
    
    public static boolean tryLockWithRetry(String key, long expireSeconds, 
                                          int maxRetries, long retryIntervalMs) {
        for (int i = 0; i < maxRetries; i++) {
            if (tryLock(key, expireSeconds)) {
                return true;
            }
            
            try {
                Thread.sleep(retryIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return false;
    }
    
    
    public static <T> T execute(String key, long expireSeconds, Supplier<T> task) {
        if (tryLock(key, expireSeconds)) {
            try {
                return task.get();
            } finally {
                unlock(key);
            }
        }
        throw new RuntimeException("获取锁失败: " + key);
    }
    
    
    public static void execute(String key, long expireSeconds, Runnable task) {
        if (tryLock(key, expireSeconds)) {
            try {
                task.run();
            } finally {
                unlock(key);
            }
        } else {
            throw new RuntimeException("获取锁失败: " + key);
        }
    }
    
    
    public static boolean isLocked(String key) {
        Long expireAt = LOCK_STORE.get(key);
        return expireAt != null && expireAt > System.currentTimeMillis();
    }
    
    
    public static void forceUnlock(String key) {
        LOCK_STORE.remove(key);
    }
    
    
    public static Map<String, Long> getActiveLocks() {
        long now = System.currentTimeMillis();
        return LOCK_STORE.entrySet().stream()
            .filter(entry -> entry.getValue() > now)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    
    private static void cleanExpired() {
        long now = System.currentTimeMillis();
        LOCK_STORE.entrySet().removeIf(entry -> entry.getValue() <= now);
    }
    
    
    public static class AutoLock implements AutoCloseable {
        private final String key;
        private volatile boolean released = false;
        
        private AutoLock(String key) {
            this.key = key;
        }
        
        @Override
        public void close() {
            if (!released) {
                unlock(key);
                released = true;
            }
        }
        
        public void release() {
            close();
        }
    }
}