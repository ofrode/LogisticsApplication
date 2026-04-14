package com.logisticsapplication.service.impl;

import com.logisticsapplication.dto.response.CounterSnapshotResponse;
import com.logisticsapplication.dto.response.RaceConditionDemoResponse;
import com.logisticsapplication.service.ConcurrencyDemoService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConcurrencyDemoServiceImpl implements ConcurrencyDemoService {

    private static final int MAX_WAIT_SECONDS = 30;

    private final AtomicInteger atomicCounter = new AtomicInteger();
    private final SynchronizedCounter synchronizedCounter = new SynchronizedCounter();

    @Override
    public CounterSnapshotResponse incrementAtomicCounter(int times) {
        for (int index = 0; index < times; index++) {
            atomicCounter.incrementAndGet();
        }
        return new CounterSnapshotResponse("ATOMIC", atomicCounter.get());
    }

    @Override
    public CounterSnapshotResponse incrementSynchronizedCounter(int times) {
        for (int index = 0; index < times; index++) {
            synchronizedCounter.increment();
        }
        return new CounterSnapshotResponse(
                "SYNCHRONIZED",
                synchronizedCounter.getValue()
        );
    }

    @Override
    public RaceConditionDemoResponse runRaceConditionDemo(
            int threadCount,
            int incrementsPerThread
    ) {
        UnsafeCounter unsafeCounter = new UnsafeCounter();
        AtomicInteger demoAtomicCounter = new AtomicInteger();
        SynchronizedCounter demoSynchronizedCounter = new SynchronizedCounter();
        int expectedValue = threadCount * incrementsPerThread;

        executeConcurrentIncrements(
                threadCount,
                incrementsPerThread,
                unsafeCounter,
                demoAtomicCounter,
                demoSynchronizedCounter
        );

        return new RaceConditionDemoResponse(
                threadCount,
                incrementsPerThread,
                expectedValue,
                unsafeCounter.getValue(),
                demoAtomicCounter.get(),
                demoSynchronizedCounter.getValue(),
                expectedValue - unsafeCounter.getValue()
        );
    }

    private void executeConcurrentIncrements(
            int threadCount,
            int incrementsPerThread,
            UnsafeCounter unsafeCounter,
            AtomicInteger demoAtomicCounter,
            SynchronizedCounter demoSynchronizedCounter
    ) {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int threadIndex = 0; threadIndex < threadCount; threadIndex++) {
            executorService.execute(() -> {
                readyLatch.countDown();
                awaitLatch(startLatch);
                for (int incrementIndex = 0; incrementIndex < incrementsPerThread;
                        incrementIndex++) {
                    unsafeCounter.increment();
                    demoAtomicCounter.incrementAndGet();
                    demoSynchronizedCounter.increment();
                }
                doneLatch.countDown();
            });
        }

        awaitLatch(readyLatch);
        startLatch.countDown();
        awaitLatch(doneLatch);
        shutdownExecutor(executorService);
    }

    private void awaitLatch(CountDownLatch latch) {
        try {
            boolean completed = latch.await(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            if (!completed) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Concurrent demo timed out"
                );
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Concurrent demo interrupted"
            );
        }
    }

    private void shutdownExecutor(ExecutorService executorService) {
        executorService.shutdown();
        try {
            boolean terminated = executorService.awaitTermination(
                    MAX_WAIT_SECONDS,
                    TimeUnit.SECONDS
            );
            if (!terminated) {
                executorService.shutdownNow();
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Executor shutdown timed out"
                );
            }
        } catch (InterruptedException exception) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Executor shutdown interrupted"
            );
        }
    }

    private static final class UnsafeCounter {

        private int value;

        private void increment() {
            int currentValue = value;
            if ((currentValue & 7) == 0) {
                LockSupport.parkNanos(1L);
            }
            value = currentValue + 1;
        }

        private int getValue() {
            return value;
        }
    }

    private static final class SynchronizedCounter {

        private int value;

        private synchronized void increment() {
            value++;
        }

        private synchronized int getValue() {
            return value;
        }
    }
}
