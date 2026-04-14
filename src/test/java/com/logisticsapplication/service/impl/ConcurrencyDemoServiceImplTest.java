package com.logisticsapplication.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.logisticsapplication.dto.response.CounterSnapshotResponse;
import com.logisticsapplication.dto.response.RaceConditionDemoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConcurrencyDemoServiceImplTest {

    private ConcurrencyDemoServiceImpl concurrencyDemoService;

    @BeforeEach
    void setUp() {
        concurrencyDemoService = new ConcurrencyDemoServiceImpl();
    }

    @Test
    void incrementMethodsUpdateThreadSafeCounters() {
        CounterSnapshotResponse atomicSnapshot =
                concurrencyDemoService.incrementAtomicCounter(5);
        CounterSnapshotResponse synchronizedSnapshot =
                concurrencyDemoService.incrementSynchronizedCounter(7);

        assertThat(atomicSnapshot.getCounterType()).isEqualTo("ATOMIC");
        assertThat(atomicSnapshot.getCurrentValue()).isEqualTo(5);
        assertThat(synchronizedSnapshot.getCounterType()).isEqualTo("SYNCHRONIZED");
        assertThat(synchronizedSnapshot.getCurrentValue()).isEqualTo(7);
    }

    @Test
    void raceConditionDemoShowsLostUpdatesForUnsafeCounter() {
        RaceConditionDemoResponse response =
                concurrencyDemoService.runRaceConditionDemo(64, 5000);

        assertThat(response.getExpectedValue()).isEqualTo(320000);
        assertThat(response.getAtomicValue()).isEqualTo(320000);
        assertThat(response.getSynchronizedValue()).isEqualTo(320000);
        assertThat(response.getUnsafeValue()).isLessThan(320000);
        assertThat(response.getLostUpdates()).isGreaterThan(0);
    }
}
