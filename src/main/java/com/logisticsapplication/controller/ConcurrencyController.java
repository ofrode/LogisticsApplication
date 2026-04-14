package com.logisticsapplication.controller;

import com.logisticsapplication.dto.response.CounterSnapshotResponse;
import com.logisticsapplication.dto.response.RaceConditionDemoResponse;
import com.logisticsapplication.service.ConcurrencyDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@Tag(name = "Concurrency", description = "Демонстрация потокобезопасности и race condition")
@RequestMapping("/api/concurrency")
public class ConcurrencyController {

    private final ConcurrencyDemoService concurrencyDemoService;

    public ConcurrencyController(ConcurrencyDemoService concurrencyDemoService) {
        this.concurrencyDemoService = concurrencyDemoService;
    }

    @PostMapping("/counter/atomic/increment")
    @Operation(summary = "Увеличить потокобезопасный Atomic-счётчик")
    public ResponseEntity<CounterSnapshotResponse> incrementAtomicCounter(
            @RequestParam(defaultValue = "1") @Min(1) int times
    ) {
        return ResponseEntity.ok(concurrencyDemoService.incrementAtomicCounter(times));
    }

    @PostMapping("/counter/synchronized/increment")
    @Operation(summary = "Увеличить потокобезопасный synchronized-счётчик")
    public ResponseEntity<CounterSnapshotResponse> incrementSynchronizedCounter(
            @RequestParam(defaultValue = "1") @Min(1) int times
    ) {
        return ResponseEntity.ok(concurrencyDemoService.incrementSynchronizedCounter(times));
    }

    @GetMapping("/race-condition")
    @Operation(summary = "Показать race condition и его решение")
    public ResponseEntity<RaceConditionDemoResponse> runRaceConditionDemo(
            @RequestParam(defaultValue = "64") @Min(50) int threads,
            @RequestParam(defaultValue = "5000") @Min(1) int incrementsPerThread
    ) {
        return ResponseEntity.ok(
                concurrencyDemoService.runRaceConditionDemo(threads, incrementsPerThread)
        );
    }
}
