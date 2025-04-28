package com.fabfas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LRUCacheTest {
    private LRUCache cache;

    @BeforeEach
    void setUp() {
        // Initialize a new cache before each test
        // Default capacity will be set in individual tests as needed
    }

    @Test
    void testBasicOperations() {
        cache = new LRUCache(2);
        cache.put(1, 1); // Cache: {1=1}
        cache.put(2, 2); // Cache: {1=1, 2=2}
        assertEquals(1, cache.get(1), "Should return value 1 for key 1");
        cache.put(3, 3); // Evicts key 2, Cache: {1=1, 3=3}
        assertEquals(-1, cache.get(2), "Key 2 should be evicted");
        cache.put(4, 4); // Evicts key 1, Cache: {3=3, 4=4}
        assertEquals(-1, cache.get(1), "Key 1 should be evicted");
        assertEquals(3, cache.get(3), "Should return value 3 for key 3");
        assertEquals(4, cache.get(4), "Should return value 4 for key 4");
    }

    @Test
    void testUpdateExistingKey() {
        cache = new LRUCache(2);
        cache.put(1, 1); // Cache: {1=1}
        cache.put(1, 10); // Update value, Cache: {1=10}
        assertEquals(10, cache.get(1), "Value for key 1 should be updated to 10");
        cache.put(2, 2); // Cache: {1=10, 2=2}
        assertEquals(10, cache.get(1), "Value for key 1 should still be 10");
    }

    @Test
    void testCapacityOneEdgeCase() {
        cache = new LRUCache(1);
        cache.put(1, 1); // Cache: {1=1}
        cache.put(2, 2); // Evicts key 1, Cache: {2=2}
        
        assertEquals(-1, cache.get(1), "Key 1 should be evicted");
        assertEquals(2, cache.get(2), "Should return value 2 for key 2");
    }

    @Test
    void testAccessOrder() {
        cache = new LRUCache(2);
        cache.put(1, 1); // Cache: {1=1}
        cache.put(2, 2); // Cache: {1=1, 2=2}
        cache.get(1);    // Moves 1 to head, Order: {1=1, 2=2}
        cache.put(3, 3); // Evicts 2, Cache: {1=1, 3=3}
        
        assertEquals(-1, cache.get(2), "Key 2 should be evicted");
        assertEquals(1, cache.get(1), "Should return value 1 for key 1");
        assertEquals(3, cache.get(3), "Should return value 3 for key 3");
    }

    @Test
    void testGetNonExistentKey() {
        cache = new LRUCache(2);
        assertEquals(-1, cache.get(1), "Should return -1 for non-existent key");
        
        cache.put(1, 1);
        assertEquals(-1, cache.get(2), "Should return -1 for non-existent key");
    }

    @Test
    void testEmptyCache() {
        cache = new LRUCache(5);
        
        assertEquals(-1, cache.get(1), "Should return -1 for empty cache");
        assertEquals(-1, cache.get(2), "Should return -1 for empty cache");
    }

    @Test
    void testLargeCapacity() {
        cache = new LRUCache(3);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        
        assertEquals(1, cache.get(1), "Should return value 1 for key 1");
        assertEquals(2, cache.get(2), "Should return value 2 for key 2");
        assertEquals(3, cache.get(3), "Should return value 3 for key 3");
        
        cache.put(4, 4); // Evicts key 1, Cache: {2=2, 3=3, 4=4}
        assertEquals(-1, cache.get(1), "Key 1 should be evicted");
    }

    @Test
    void testInvalidCapacity() {
        // JUnit does not handle exceptions for constructor directly in this context
        // Assuming capacity <= 0 is invalid, but implementation doesn't throw exception
        cache = new LRUCache(0);
        cache.put(1, 1); // Behavior undefined, but should not crash
        
        assertEquals(-1, cache.get(1), "Should handle invalid capacity gracefully");
    }
}
