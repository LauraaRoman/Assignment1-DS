package com.utcn.deviceservice.services;

import com.utcn.deviceservice.controllers.handlers.exceptions.model.ResourceNotFoundException;
import com.utcn.deviceservice.dtos.DeviceDTO;
import com.utcn.deviceservice.dtos.DeviceInsertDTO;
import com.utcn.deviceservice.dtos.builders.DeviceBuilder;
import com.utcn.deviceservice.entities.Device;
import com.utcn.deviceservice.entities.UserReference;
import com.utcn.deviceservice.messaging.SyncPublisher;
import com.utcn.deviceservice.repositories.DeviceRepository;
import com.utcn.deviceservice.repositories.UserReferenceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserReferenceRepository userReferenceRepository;
    private final SyncPublisher syncPublisher;

    public DeviceService(DeviceRepository deviceRepository,
                         UserReferenceRepository userReferenceRepository,
                         SyncPublisher syncPublisher) {
        this.deviceRepository = deviceRepository;
        this.userReferenceRepository = userReferenceRepository;
        this.syncPublisher = syncPublisher;
    }

    private void validateUserExists(UUID userId) {
        if (!userReferenceRepository.existsById(userId)) {
            userReferenceRepository.save(new UserReference(userId));
        }
    }

    public DeviceDTO insert(DeviceInsertDTO deviceDTO) {
        validateUserExists(deviceDTO.getUserId());

        Device device = DeviceBuilder.toEntity(deviceDTO);
        device = deviceRepository.save(device);

        syncPublisher.publishDeviceSync(device.getId(), device.getUserId(),
                device.getMaxHourlyEnergyConsumption());

        return DeviceBuilder.toDeviceDTO(device);
    }

    public List<DeviceDTO> findDevices() {
        List<Device> deviceList = deviceRepository.findAll();
        return deviceList.stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    public DeviceDTO findDeviceById(UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id));
        return DeviceBuilder.toDeviceDTO(device);
    }

    public List<DeviceDTO> findDevicesByUserId(UUID userId) {
        List<Device> deviceList = deviceRepository.findByUserId(userId);
        return deviceList.stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    public DeviceDTO update(UUID id, DeviceInsertDTO deviceDTO) {
        Device existingDevice = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id));

        validateUserExists(deviceDTO.getUserId());

        existingDevice.setDescription(deviceDTO.getDescription());
        existingDevice.setAddress(deviceDTO.getAddress());
        existingDevice.setMaxHourlyEnergyConsumption(deviceDTO.getMaxHourlyEnergyConsumption());
        existingDevice.setUserId(deviceDTO.getUserId());

        existingDevice = deviceRepository.save(existingDevice);

        // Re-sync so monitoring-service gets updated max limit
        syncPublisher.publishDeviceSync(existingDevice.getId(), existingDevice.getUserId(),
                existingDevice.getMaxHourlyEnergyConsumption());

        return DeviceBuilder.toDeviceDTO(existingDevice);
    }

    public void delete(UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id));
        deviceRepository.delete(device);
    }

    public void deleteDevicesByUserId(UUID userId) {
        List<Device> devices = deviceRepository.findByUserId(userId);
        deviceRepository.deleteAll(devices);
    }
}
