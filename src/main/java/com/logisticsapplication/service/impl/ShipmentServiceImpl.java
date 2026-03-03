package com.logisticsapplication.service.impl;

import com.logisticsapplication.dto.request.ShipmentRequest;
import com.logisticsapplication.dto.response.ShipmentResponse;
import com.logisticsapplication.mapper.ShipmentMapper;
import com.logisticsapplication.model.Shipment;
import com.logisticsapplication.repository.ShipmentRepository;
import com.logisticsapplication.service.ShipmentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;

    @Override
    public ShipmentResponse create(ShipmentRequest request) {
        Shipment shipment = new Shipment();
        ShipmentMapper.updateEntity(shipment, request);
        return ShipmentMapper.toResponse(shipmentRepository.save(shipment));
    }

    @Override
    public ShipmentResponse update(Integer id, ShipmentRequest request) {
        Shipment shipment = shipmentRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found: " + id
                )
        );
        ShipmentMapper.updateEntity(shipment, request);
        return ShipmentMapper.toResponse(shipmentRepository.save(shipment));
    }

    @Override
    public ShipmentResponse getById(Integer id) {
        Shipment shipment = shipmentRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found: " + id
                )
        );
        return ShipmentMapper.toResponse(shipment);
    }

    @Override
    public List<ShipmentResponse> getAll(String status) {
        List<Shipment> shipments;
        if (status == null || status.isBlank()) {
            shipments = shipmentRepository.findAll();
        } else {
            shipments = shipmentRepository.findByStatusIgnoreCase(status);
        }
        return shipments.stream().map(ShipmentMapper::toResponse).toList();
    }

    @Override
    public void delete(Integer id) {
        if (!shipmentRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Shipment not found: " + id
            );
        }
        shipmentRepository.deleteById(id);
    }
}
