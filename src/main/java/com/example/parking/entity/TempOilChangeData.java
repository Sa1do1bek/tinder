package com.example.parking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "temp_oil_change_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TempOilChangeData {

    @Id
    private Long chatId;

    private LocalDate lastChangeDate;
    private Integer lastChangeMileage;
    private String oilBrand;
    private Integer recommendedKm;
    private Integer dailyDistance;
}