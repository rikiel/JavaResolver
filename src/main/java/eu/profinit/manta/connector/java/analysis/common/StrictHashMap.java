package eu.profinit.manta.connector.java.analysis.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;

public class StrictHashMap<K, V> extends HashMap<K, V> {
    public StrictHashMap() {
    }

    public StrictHashMap(Map<? extends K, ? extends V> map) {
        super(map);
    }

    @Override
    public V get(Object key) {
        Validate.isTrue(containsKey(key), "Key %s is not in map!", key);
        return super.get(key);
    }

    @Override
    public V put(K key, V value) {
        Validate.isTrue(!containsKey(key), "Key %s is already in map!", key);
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (K key : map.keySet()) {
            Validate.isTrue(!containsKey(key), "Key %s is already in map!", key);
        }
        super.putAll(map);
    }
}
