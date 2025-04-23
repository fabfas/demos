package com.fabfas;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CustomThreadPool {
	private final BlockingQueue<Runnable> taskQueue;
	private final WorkerThread[] workerThreads;
	private boolean isStopped = false;

	public CustomThreadPool(int numberOfThreads) {
		taskQueue = new LinkedBlockingQueue<>();
		workerThreads = new WorkerThread[numberOfThreads];

		for (int i = 0; i < numberOfThreads; i++) {
			workerThreads[i] = new WorkerThread();
			workerThreads[i].start();
		}
	}

	public synchronized void execute(Runnable task) throws IllegalStateException {
		if (isStopped)
			throw new IllegalStateException("ThreadPool is stopped");
		
		taskQueue.offer(task);
	}

	public synchronized void stop() {
		isStopped = true;
		for (WorkerThread worker : workerThreads) {
			worker.interrupt();
		}
	}

	private class WorkerThread extends Thread {
		@Override
		public void run() {
			while (!isStopped) {
				try {
					Runnable task = taskQueue.take(); // Blocks if no tasks are available
					task.run();
				} catch (InterruptedException e) {
					// Thread was interrupted, exit the loop
					if (isStopped)
						break;
				}
			}
		}
	}

	public static void main(String[] args) {
		CustomThreadPool threadPool = new CustomThreadPool(3);

		// Submit tasks to the thread pool
		for (int i = 0; i < 10; i++) {
			final int taskId = i;
			threadPool.execute(() -> {
				System.out.println("Executing task " + taskId + " in " + Thread.currentThread().getName());
				try {
					Thread.sleep(1000); // Simulate work
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			});
		}

		// Stop the thread pool after some time
		try {
			Thread.sleep(5000); // Let the tasks run for a while
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		threadPool.stop();
		System.out.println("Thread pool stopped.");
	}
}
