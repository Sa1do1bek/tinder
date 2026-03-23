package com.example.tinder.dto;


import com.example.tinder.enums.ReportReason;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateReportUserDto {
    @NotBlank(message = "reported.id.can.not.be.empty.or.null")
    private Long reportedUserId;
    @NotBlank(message = "report.reason.can.not.be.null.or.empty")
    private ReportReason reason;
    private String description;
}
