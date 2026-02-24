package com.nutriflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuBatchResponse {
    private Long batchId;
    private Long userId;
    private Integer year;
    private Integer month;
    private String status;
    private String dietaryNotes;
    private String createdAt;
}