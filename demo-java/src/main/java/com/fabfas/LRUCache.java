package com.fabfas;

import java.util.HashMap;
import java.util.Map;

public class LRUCache {
    // Node class for doubly linked list to maintain order of usage
    private class Node {
        int key, value;
        Node prev, next;

        Node(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    private Map<Integer, Node> cache; // HashMap for O(1) key-node lookup
    private int capacity; // Maximum number of items in cache
    private Node head, tail; // Dummy nodes for easier list manipulation

    public LRUCache(int capacity) {
        this.capacity = capacity;
        cache = new HashMap<>();
        // Initialize dummy head and tail nodes
        head = new Node(0, 0);
        tail = new Node(0, 0);
        head.next = tail;
        tail.prev = head;
    }

    // Get value by key, move node to head (most recently used), return -1 if not found
    public int get(int key) {
        if (!cache.containsKey(key)) {
            return -1;
        }
        Node node = cache.get(key);
        // Move to head to mark as most recently used
        removeNode(node);
        addToHead(node);
        return node.value;
    }

    // Add or update key-value pair, evict least recently used if capacity exceeded
    public void put(int key, int value) {
        if (cache.containsKey(key)) {
            // Update value of existing node and move to head
            Node node = cache.get(key);
            node.value = value;
            removeNode(node);
            addToHead(node);
        } else {
            // Create new node and add to cache
            Node node = new Node(key, value);
            cache.put(key, node);
            addToHead(node);
            // If capacity exceeded, remove least recently used item
            if (cache.size() > capacity) {
                Node lru = tail.prev;
                removeNode(lru);
                cache.remove(lru.key);
            }
        }
    }

    // Helper method to remove a node from the doubly linked list
    private void removeNode(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    // Helper method to add a node right after head (most recently used position)
    private void addToHead(Node node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    // Test the LRU Cache implementation with various scenarios
    public static void main(String[] args) {
        // Test Case 1: Basic operations with capacity 2
        System.out.println("Test Case 1: Basic Operations");
        LRUCache cache = new LRUCache(2);
        cache.put(1, 1); // Cache: {1=1}
        cache.put(2, 2); // Cache: {1=1, 2=2}
        System.out.println("Get 1: " + cache.get(1)); // Expected: 1
        cache.put(3, 3); // Evicts 2, Cache: {1=1, 3=3}
        System.out.println("Get 2: " + cache.get(2)); // Expected: -1 (not found)
        cache.put(4, 4); // Evicts 1, Cache: {3=3, 4=4}
        System.out.println("Get 1: " + cache.get(1)); // Expected: -1 (not found)
        System.out.println("Get 3: " + cache.get(3)); // Expected: 3
        System.out.println("Get 4: " + cache.get(4)); // Expected: 4
        System.out.println();

        // Test Case 2: Update existing key
        System.out.println("Test Case 2: Update Existing Key");
        cache = new LRUCache(2);
        cache.put(1, 1); // Cache: {1=1}
        cache.put(1, 10); // Update value, Cache: {1=10}
        System.out.println("Get 1: " + cache.get(1)); // Expected: 10
        System.out.println();

        // Test Case 3: Capacity of 1 (edge case)
        System.out.println("Test Case 3: Capacity of 1");
        cache = new LRUCache(1);
        cache.put(1, 1); // Cache: {1=1}
        cache.put(2, 2); // Evicts 1, Cache: {2=2}
        System.out.println("Get 1: " + cache.get(1)); // Expected: -1 (not found)
        System.out.println("Get 2: " + cache.get(2)); // Expected: 2
        System.out.println();

        // Test Case 4: Multiple accesses to test order
        System.out.println("Test Case 4: Multiple Accesses");
        cache = new LRUCache(2);
        cache.put(1, 1); // Cache: {1=1}
        cache.put(2, 2); // Cache: {1=1, 2=2}
        cache.get(1);    // Moves 1 to head, Cache order: {1=1, 2=2}
        cache.put(3, 3); // Evicts 2, Cache: {1=1, 3=3}
        System.out.println("Get 2: " + cache.get(2)); // Expected: -1 (not found)
        System.out.println("Get 1: " + cache.get(1)); // Expected: 1
        System.out.println("Get 3: " + cache.get(3)); // Expected: 3
        System.out.println("All test cases completed!");
    }
}