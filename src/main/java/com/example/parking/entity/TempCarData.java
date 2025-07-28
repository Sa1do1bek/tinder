package com.example.parking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "temp_car_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TempCarData {
    @Id
    private Long chatId;
    
    private String licensePlate;
    private String brand;
    private String model;
    private String color;
}
