package com.logisticsapplication.cache;

import com.logisticsapplication.dto.response.PageResponse;
import com.logisticsapplication.dto.response.ShipmentResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ShipmentSearchIndex {

    private final Map<ShipmentSearchCacheKey, PageResponse<ShipmentResponse>> index =
            new HashMap<>();

    public synchronized Optional<PageResponse<ShipmentResponse>> get(ShipmentSearchCacheKey key) {
        PageResponse<ShipmentResponse> value = index.get(key);
        if (value == null) {
            log.info("CACHE MISS: {}", key);
            return Optional.empty();
        }
        log.info("CACHE HIT: {}", key);
        return Optional.of(value);
    }

    public synchronized void put(
            ShipmentSearchCacheKey key,
            PageResponse<ShipmentResponse> value
    ) {
        index.put(key, value);
        log.info("CACHE PUT: key={}, cacheSize={}", key, index.size());
    }

    public synchronized void invalidateAll() {
        int previousSize = index.size();
        index.clear();
        log.info("CACHE INVALIDATE ALL: removedEntries={}", previousSize);
    }
}
