package by.training.impls;

import by.training.interfaces.Cache;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;


public class LRUCache<K, V> implements Cache<K, V> {
    private final int capacity;
    private final Map<K, V> map;

    public LRUCache(final int capacity) {
        this.capacity = capacity;
        map = Collections.synchronizedMap(new LinkedHashMap<K, V>(capacity, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > capacity;
            }
        });
    }

    @Override
    public synchronized void put(K key, V value) {
        map.put(key, value);
    }

    @Override
    public synchronized V get(K key) {
            return map.get(key);
    }

    @Override
    public synchronized int size() {
        return map.size();
    }

    @Override
    public synchronized String toString() {
        return printItems(map);
    }
}
