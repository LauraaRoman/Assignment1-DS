package com.utcn.deviceservice.services;

import com.utcn.deviceservice.controllers.handlers.exceptions.model.ResourceNotFoundException;
import com.utcn.deviceservice.dtos.DeviceDTO;
import com.utcn.deviceservice.dtos.DeviceInsertDTO;
import com.utcn.deviceservice.dtos.builders.DeviceBuilder;
import com.utcn.deviceservice.entities.Device;
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

    // Am scos RestTemplate din constructor, nu mai avem nevoie de el aici
    public DeviceService(DeviceRepository deviceRepository, UserReferenceRepository userReferenceRepository) {
        this.deviceRepository = deviceRepository;
        this.userReferenceRepository = userReferenceRepository;
    }

    private void validateUserExists(UUID userId) {
        if (!userReferenceRepository.existsById(userId)) {
            // În loc să aruncăm eroarea 404, sincronizăm/salvăm automat utilizatorul
            // în tabela locală a microserviciului de dispozitive.
            com.utcn.deviceservice.entities.UserReference missingUser = new com.utcn.deviceservice.entities.UserReference(userId);
            userReferenceRepository.save(missingUser);
        }
    }

    public DeviceDTO insert(DeviceInsertDTO deviceDTO) {
        validateUserExists(deviceDTO.getUserId());

        Device device = DeviceBuilder.toEntity(deviceDTO);
        device = deviceRepository.save(device);
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