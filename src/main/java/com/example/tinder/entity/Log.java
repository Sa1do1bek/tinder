package com.example.tinder.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Log {

    @Id
    private Long id;

    private String text;

    private LocalDateTime date;
}
