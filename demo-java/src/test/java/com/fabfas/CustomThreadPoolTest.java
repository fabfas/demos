package com.fabfas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class CustomThreadPoolTest {
    private CustomThreadPool threadPool;

    @BeforeEach
    void setUp() {
        // Initialize a thread pool with 2 threads before each test
        threadPool = new CustomThreadPool(2);
    }

    @Test
    void testConstructorWithZeroThreads() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new CustomThreadPool(0);
        }, "Should throw IllegalArgumentException for zero threads");
        
        assertTrue(exception.getMessage().contains("numberOfThreads must be greater than 0") || exception.getMessage().isEmpty(), 
                   "Exception message should indicate invalid thread count");
    }

    @Test
    void testConstructorWithNegativeThreads() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new CustomThreadPool(-1);
        }, "Should throw IllegalArgumentException for negative threads");
        assertTrue(exception.getMessage().contains("numberOfThreads must be greater than 0") || exception.getMessage().isEmpty(), 
                   "Exception message should indicate invalid thread count");
    }

    @Test
    void testSubmitAndExecuteSingleTask() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger counter = new AtomicInteger(0);

        threadPool.execute(() -> {
            counter.incrementAndGet();
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Task should complete within 2 seconds");
        assertEquals(1, counter.get(), "Task should have been executed once");
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testSubmitAndExecuteMultipleTasks() throws InterruptedException {
        int taskCount = 10;
        CountDownLatch latch = new CountDownLatch(taskCount);
        AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < taskCount; i++) {
            threadPool.execute(() -> {
                counter.incrementAndGet();
                latch.countDown();
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "All tasks should complete within 5 seconds");
        assertEquals(taskCount, counter.get(), "All tasks should have been executed");
    }

    @Test
    void testExecuteAfterStop() {
        threadPool.stop();
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            threadPool.execute(() -> {});
        }, "Should throw IllegalStateException when executing task after stop");
        assertEquals("ThreadPool is stopped", exception.getMessage());
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testStopPreventsNewTasks() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger counter = new AtomicInteger(0);

        threadPool.execute(() -> {
            counter.incrementAndGet();
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Task should complete before stop");
        assertEquals(1, counter.get(), "Task should have been executed");

        threadPool.stop();
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            threadPool.execute(() -> counter.incrementAndGet());
        }, "Should throw IllegalStateException after stop");
        assertEquals("ThreadPool is stopped", exception.getMessage());
        assertEquals(1, counter.get(), "No new tasks should be executed after stop");
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testTaskExecutionWithException() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger counter = new AtomicInteger(0);

        // Submit a task that throws an exception
        threadPool.execute(() -> {
            latch.countDown();
            throw new RuntimeException("Test exception");
        });

        // Submit a normal task to ensure pool continues working
        threadPool.execute(() -> {
            counter.incrementAndGet();
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Tasks should complete within 2 seconds");
        assertEquals(1, counter.get(), "Second task should still execute despite exception in first task");
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testWorkerThreadsStopAfterInterrupt() throws InterruptedException {
        // Submit a task to keep at least one worker busy
        CountDownLatch latch = new CountDownLatch(1);
        
        threadPool.execute(() -> {
            try {
                Thread.sleep(1000); // Simulate long-running task
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            latch.countDown();
        });

        // Stop the pool to interrupt workers
        threadPool.stop();

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Task should complete or be interrupted within 2 seconds");
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            threadPool.execute(() -> {});
        }, "Should not accept new tasks after stop");
        assertEquals("ThreadPool is stopped", exception.getMessage());
    }
}