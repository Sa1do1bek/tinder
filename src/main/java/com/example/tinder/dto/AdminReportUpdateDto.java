package com.example.tinder.dto;



import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminReportUpdateDto {
    @NotBlank(message = "admin.must.write.what.he.did.for.solving.problem")
    private String comment;
}
