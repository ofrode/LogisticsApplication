package com.logisticsapplication.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ShipmentSearchCacheKeyTest {

    @Test
    void equalsAndHashCodeDependOnCriteriaAndPage() {
        ShipmentSearchCacheKey.SearchCriteria criteria =
                new ShipmentSearchCacheKey.SearchCriteria(
                        "customer@example.com",
                        "Paper",
                        LocalDateTime.of(2026, 4, 1, 10, 0),
                        LocalDateTime.of(2026, 4, 2, 12, 0),
                        "JPQL"
                );
        ShipmentSearchCacheKey.PageDescriptor page =
                new ShipmentSearchCacheKey.PageDescriptor(0, 10, "arrivalAt,asc");
        ShipmentSearchCacheKey first = new ShipmentSearchCacheKey(criteria, page);
        ShipmentSearchCacheKey second = new ShipmentSearchCacheKey(criteria, page);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first.toString()).contains("customer@example.com", "arrivalAt,asc");
    }

    @Test
    void equalsReturnsFalseForDifferentValuesAndDifferentTypes() {
        ShipmentSearchCacheKey first = new ShipmentSearchCacheKey(
                new ShipmentSearchCacheKey.SearchCriteria("a", "b", null, null, "JPQL"),
                new ShipmentSearchCacheKey.PageDescriptor(0, 10, "id,asc")
        );
        ShipmentSearchCacheKey second = new ShipmentSearchCacheKey(
                new ShipmentSearchCacheKey.SearchCriteria("x", "b", null, null, "JPQL"),
                new ShipmentSearchCacheKey.PageDescriptor(0, 10, "id,asc")
        );

        assertThat(first).isNotEqualTo(second);
        assertThat(first).isEqualTo(first);
        assertThat(first.equals(null)).isFalse();
        assertThat(first.equals("not-a-cache-key")).isFalse();
    }

    @Test
    void equalsReturnsFalseWhenOnlyPageDescriptorDiffers() {
        ShipmentSearchCacheKey.SearchCriteria criteria =
                new ShipmentSearchCacheKey.SearchCriteria("a", "b", null, null, "JPQL");
        ShipmentSearchCacheKey first = new ShipmentSearchCacheKey(
                criteria,
                new ShipmentSearchCacheKey.PageDescriptor(0, 10, "id,asc")
        );
        ShipmentSearchCacheKey second = new ShipmentSearchCacheKey(
                criteria,
                new ShipmentSearchCacheKey.PageDescriptor(1, 10, "id,asc")
        );

        assertThat(first).isNotEqualTo(second);
    }
}
