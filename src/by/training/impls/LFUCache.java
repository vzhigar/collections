package by.training.impls;

import by.training.interfaces.Cache;

import java.util.*;


public class LFUCache<K, V> implements Cache<K, V> {
    private final int minFrequency = 0;
    private final int maxFrequency;
    private final int capacity;
    private final Map<K, V> map;
    private final LinkedList<K>[] list;
    private volatile double evictionFactor = 1.0;

    /**
     *
     * @param capacity capacity of the cache must be > 0
     * If you use this constructor evictionFactor has default value 1
     */
    @SuppressWarnings("unchecked")
    public LFUCache(final int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException(Constants.CAPACITY_EXCEPTION_MESSAGE);
        }
        this.capacity = capacity;
        maxFrequency = capacity;
        map = Collections.synchronizedMap(new LinkedHashMap<K, V>(capacity));
        list = new LinkedList[capacity + 1];
        initArray();
    }

    /**
     *
     * @param capacity capacity of the cache must be > 0
     * @param evictionFactor must be greater then 0 and less or equal 1
     */
    public LFUCache(final int capacity, final double evictionFactor) {
        this(capacity);
        if (evictionFactor <= 0 || evictionFactor > 1) {
            throw new IllegalArgumentException(Constants.EVICTION_EXCEPTION_MESSAGE);
        }
        this.evictionFactor = evictionFactor;
    }

    private void initArray() {
        for (int i = minFrequency; i <= maxFrequency; i++) {
            list[i] = new LinkedList<>();
        }
    }

    private boolean eviction() {
        boolean isEvicted = false;
        int factor = (int) (capacity * evictionFactor);
        if (factor == 0) {
            factor++;
        }
        LinkedList<K> currentList;
        int counter = 0;
        K key;
        for (int i = 1; i <= maxFrequency; i++) {
            currentList = list[i];
            while (!currentList.isEmpty()) {
                if (counter == factor) {
                    return true;
                }
                key = currentList.removeFirst();
                map.remove(key);
                counter++;
                isEvicted = true;
            }
        }
        return isEvicted;
    }

    @Override
    public synchronized void put(K key, V value) {
        LinkedList<K> currentList = list[minFrequency];
        if (!map.containsKey(key)) {
            if (map.size() == capacity) {
                boolean isEvicted = eviction();
                if (!isEvicted) {
                    K removeKey = currentList.removeFirst();
                    map.remove(removeKey);
                }
            }
            currentList.add(key);
            map.put(key, value);
        } else {
            map.put(key, value);
        }
    }

    /**
     *
     * @return returns value from cache by key. If key is not present returns null.
     */
    @Override
    public synchronized V get(K key) {
        V value = null;
        LinkedList<K> currentList;
        LinkedList<K> nextList;
        for (int i = minFrequency; i <= maxFrequency; i++) {
            currentList = list[i];
            if (currentList.contains(key)) {
                if (i == maxFrequency) {
                    currentList.remove(key);
                    currentList.add(key);
                    break;
                }
                nextList = list[i + 1];
                nextList.add(key);
                currentList.remove(key);
                value = map.get(key);
                break;
            }
        }
        return value;
    }

    @Override
    public synchronized int size() {
        return map.size();
    }

    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder(printItems(map));
        final String listNumber = "List number ";
        final String whiteSpace = " ";
        final String listKey = "key: ";
        final String newLine = "\n";
        LinkedList<K> currentList;
        for (int i = minFrequency; i <= maxFrequency; i++) {
            currentList = list[i];
            if (currentList != null) {
                for (K key : currentList) {
                    sb.append(listNumber)
                            .append(i)
                            .append(whiteSpace)
                            .append(listKey)
                            .append(key)
                            .append(newLine);
                }
            }
        }
        return sb.toString();
    }

    private class Constants {
        static final String CAPACITY_EXCEPTION_MESSAGE = "Capacity of cache must be greater then 0";
        static final String EVICTION_EXCEPTION_MESSAGE = "Eviction factor must be greater then 0 and less then 1";
    }

}

