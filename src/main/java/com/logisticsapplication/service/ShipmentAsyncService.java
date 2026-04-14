package com.logisticsapplication.service;

import com.logisticsapplication.dto.request.ShipmentRequest;
import com.logisticsapplication.dto.response.AsyncShipmentTaskStatusResponse;
import java.util.List;

public interface ShipmentAsyncService {

    Long submitBulkCreateTask(List<ShipmentRequest> requests);

    AsyncShipmentTaskStatusResponse getTaskStatus(Long taskId);
}
