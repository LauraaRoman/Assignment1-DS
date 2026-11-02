package com.utcn.deviceservice.dtos.builders;


import com.utcn.deviceservice.dtos.DeviceDTO;
import com.utcn.deviceservice.dtos.DeviceInsertDTO;
import com.utcn.deviceservice.entities.Device;

public class DeviceBuilder {

    private DeviceBuilder() {
    }

    public static DeviceDTO toDeviceDTO(Device device) {
        return new DeviceDTO(
                device.getId(),
                device.getDescription(),
                device.getAddress(),
                device.getMaxHourlyEnergyConsumption(),
                device.getUserId()
        );
    }

    public static Device toEntity(DeviceInsertDTO deviceInsertDTO) {
        return new Device(
                deviceInsertDTO.getDescription(),
                deviceInsertDTO.getAddress(),
                deviceInsertDTO.getMaxHourlyEnergyConsumption(),
                deviceInsertDTO.getUserId()
        );
    }
}