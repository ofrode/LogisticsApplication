package com.logisticsapplication.cache;

import java.time.LocalDateTime;
import java.util.Objects;

public final class ShipmentSearchCacheKey {

    private final SearchCriteria criteria;
    private final PageDescriptor page;

    public ShipmentSearchCacheKey(SearchCriteria criteria, PageDescriptor page) {
        this.criteria = criteria;
        this.page = page;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ShipmentSearchCacheKey other)) {
            return false;
        }
        return Objects.equals(criteria, other.criteria)
                && Objects.equals(page, other.page);
    }

    @Override
    public int hashCode() {
        return Objects.hash(criteria, page);
    }

    @Override
    public String toString() {
        return "ShipmentSearchCacheKey{"
                + "criteria=" + criteria
                + ", page=" + page
                + '}';
    }

    public record SearchCriteria(
            String customerEmail,
            String cargoName,
            LocalDateTime arrivalFrom,
            LocalDateTime arrivalTo,
            String queryType
    ) {
    }

    public record PageDescriptor(
            int pageNumber,
            int pageSize,
            String sort
    ) {
    }
}
