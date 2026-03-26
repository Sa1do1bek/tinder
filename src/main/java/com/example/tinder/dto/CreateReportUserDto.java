package com.example.tinder.dto;


import com.example.tinder.enums.ReportReason;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateReportUserDto {
    @NotNull
    private Long reportedUserId;
    @NotNull
    private ReportReason reason;
    private String description;
}
