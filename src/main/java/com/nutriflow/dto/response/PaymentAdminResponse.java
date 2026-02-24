package com.nutriflow.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentAdminResponse {

    private Long id;
    private String userEmail;
    private String userFirstName;
    private String userLastName;
    private Double amount;
    private String currency;
    private String status;
    private LocalDateTime paymentDate;
    private String transactionId;
    private Long subscriptionId;

}