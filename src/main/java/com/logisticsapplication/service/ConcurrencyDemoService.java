package com.logisticsapplication.service;

import com.logisticsapplication.dto.response.CounterSnapshotResponse;
import com.logisticsapplication.dto.response.RaceConditionDemoResponse;

public interface ConcurrencyDemoService {

    CounterSnapshotResponse incrementAtomicCounter(int times);

    CounterSnapshotResponse incrementSynchronizedCounter(int times);

    RaceConditionDemoResponse runRaceConditionDemo(int threadCount, int incrementsPerThread);
}
