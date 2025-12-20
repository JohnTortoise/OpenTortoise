package io.github.johntortoise.generator;

import java.lang.management.ManagementFactory;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;


public class UniqueIdGenerator {
    

    private static final long START_TIMESTAMP = 1704067200000L;
    

    private static final long WORKER_ID_BITS = 5L;
    

    private static final long DATACENTER_ID_BITS = 5L;
    

    private static final long SEQUENCE_BITS = 12L;
    

    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    

    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    

    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    

    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    

    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    

    private final long workerId;
    

    private final long datacenterId;
    

    private long sequence = 0L;
    

    private long lastTimestamp = -1L;
    
    private static final AtomicLong ATOMIC_SEQUENCE = new AtomicLong(0);
    

    private static volatile UniqueIdGenerator instance;
    

    public UniqueIdGenerator(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                String.format("worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(
                String.format("datacenter Id can't be greater than %d or less than 0", MAX_DATACENTER_ID));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }
    

    public static UniqueIdGenerator getInstance() {
        if (instance == null) {
            synchronized (UniqueIdGenerator.class) {
                if (instance == null) {
                    long workerId = getWorkerId();
                    long datacenterId = getDatacenterId();
                    instance = new UniqueIdGenerator(workerId, datacenterId);
                }
            }
        }
        return instance;
    }
    

    public synchronized long nextId() {
        long timestamp = timeGen();
        

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
                    lastTimestamp - timestamp));
        }
        

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;

            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {

            sequence = 0L;
        }
        

        lastTimestamp = timestamp;
        

        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }
    

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }
    

    protected long timeGen() {
        return System.currentTimeMillis();
    }
    

    private static long getWorkerId() {
        try {
            String macAddress = getMacAddress();
            int hashCode = macAddress.hashCode();
            return (hashCode & 0xFF) % (MAX_WORKER_ID + 1);
        } catch (Exception e) {

            return System.currentTimeMillis() % (MAX_WORKER_ID + 1);
        }
    }
    

    private static long getDatacenterId() {
        try {
            String processName = ManagementFactory.getRuntimeMXBean().getName();
            if (processName.contains("@")) {
                return Long.parseLong(processName.substring(0, processName.indexOf("@"))) % (MAX_DATACENTER_ID + 1);
            }
            return processName.hashCode() % (MAX_DATACENTER_ID + 1);
        } catch (Exception e) {
            return System.currentTimeMillis() % (MAX_DATACENTER_ID + 1);
        }
    }
    

    private static String getMacAddress() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X", mac[i]));
                    }
                    return sb.toString();
                }
            }
        }
        return "00:00:00:00:00:00";
    }
    

    public static String generateId() {
        return getInstance().nextId()+"";
    }
    

    public static IdInfo parseId(long id) {
        long timestamp = (id >> TIMESTAMP_LEFT_SHIFT) + START_TIMESTAMP;
        long datacenterId = (id >> DATACENTER_ID_SHIFT) & MAX_DATACENTER_ID;
        long workerId = (id >> WORKER_ID_SHIFT) & MAX_WORKER_ID;
        long sequence = id & SEQUENCE_MASK;
        
        return new IdInfo(timestamp, datacenterId, workerId, sequence);
    }
    

    public static class IdInfo {
        private final long timestamp;
        private final long datacenterId;
        private final long workerId;
        private final long sequence;
        
        public IdInfo(long timestamp, long datacenterId, long workerId, long sequence) {
            this.timestamp = timestamp;
            this.datacenterId = datacenterId;
            this.workerId = workerId;
            this.sequence = sequence;
        }
        

        public long getTimestamp() { return timestamp; }
        public long getDatacenterId() { return datacenterId; }
        public long getWorkerId() { return workerId; }
        public long getSequence() { return sequence; }
        
        @Override
        public String toString() {
            return String.format("IdInfo{timestamp=%d, datacenterId=%d, workerId=%d, sequence=%d}",
                timestamp, datacenterId, workerId, sequence);
        }
    }
}