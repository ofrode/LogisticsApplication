package com.logisticsapplication.cache;

import com.logisticsapplication.dto.response.PageResponse;
import com.logisticsapplication.dto.response.ShipmentResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ShipmentSearchIndex {

    private final Map<ShipmentSearchCacheKey, PageResponse<ShipmentResponse>> index =
            new HashMap<>();

    public synchronized Optional<PageResponse<ShipmentResponse>> get(ShipmentSearchCacheKey key) {
        return Optional.ofNullable(index.get(key));
    }

    public synchronized void put(
            ShipmentSearchCacheKey key,
            PageResponse<ShipmentResponse> value
    ) {
        index.put(key, value);
    }

    public synchronized void invalidateAll() {
        index.clear();
    }
}
