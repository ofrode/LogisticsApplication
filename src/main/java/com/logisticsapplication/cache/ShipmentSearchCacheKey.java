package com.logisticsapplication.cache;

import java.time.LocalDateTime;
import java.util.Objects;

public final class ShipmentSearchCacheKey {

    private final String customerEmail;
    private final String cargoName;
    private final LocalDateTime arrivalFrom;
    private final LocalDateTime arrivalTo;
    private final String queryType;
    private final int pageNumber;
    private final int pageSize;
    private final String sort;

    public ShipmentSearchCacheKey(
            String customerEmail,
            String cargoName,
            LocalDateTime arrivalFrom,
            LocalDateTime arrivalTo,
            String queryType,
            int pageNumber,
            int pageSize,
            String sort
    ) {
        this.customerEmail = customerEmail;
        this.cargoName = cargoName;
        this.arrivalFrom = arrivalFrom;
        this.arrivalTo = arrivalTo;
        this.queryType = queryType;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sort = sort;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ShipmentSearchCacheKey other)) {
            return false;
        }
        return pageNumber == other.pageNumber
                && pageSize == other.pageSize
                && Objects.equals(customerEmail, other.customerEmail)
                && Objects.equals(cargoName, other.cargoName)
                && Objects.equals(arrivalFrom, other.arrivalFrom)
                && Objects.equals(arrivalTo, other.arrivalTo)
                && Objects.equals(queryType, other.queryType)
                && Objects.equals(sort, other.sort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                customerEmail,
                cargoName,
                arrivalFrom,
                arrivalTo,
                queryType,
                pageNumber,
                pageSize,
                sort
        );
    }
}
