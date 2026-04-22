package com.logisticsapplication.service.impl;

import com.logisticsapplication.dto.request.ShipmentRequest;
import com.logisticsapplication.dto.response.AsyncShipmentTaskOverviewResponse;
import com.logisticsapplication.dto.response.AsyncShipmentTaskStatusResponse;
import com.logisticsapplication.service.ShipmentAsyncService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ShipmentAsyncServiceImpl implements ShipmentAsyncService {

    private final AsyncShipmentTaskRegistry taskRegistry;
    private final ShipmentAsyncWorker shipmentAsyncWorker;

    public ShipmentAsyncServiceImpl(
            AsyncShipmentTaskRegistry taskRegistry,
            ShipmentAsyncWorker shipmentAsyncWorker
    ) {
        this.taskRegistry = taskRegistry;
        this.shipmentAsyncWorker = shipmentAsyncWorker;
    }

    @Override
    public Long submitBulkCreateTask(List<ShipmentRequest> requests) {
        AsyncShipmentTaskStatusResponse task = taskRegistry.createTask(requests);
        shipmentAsyncWorker.processBulkCreation(task.getTaskId(), List.copyOf(requests));
        return task.getTaskId();
    }

    @Override
    public AsyncShipmentTaskStatusResponse getTaskStatus(Long taskId) {
        return taskRegistry.getTask(taskId);
    }

    @Override
    public AsyncShipmentTaskOverviewResponse getAllTaskStatuses() {
        return taskRegistry.getAllTasks();
    }
}
