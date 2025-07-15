package com.luv2code.ecommerce.dto;

import lombok.Data;

@Data
public class PaymentInfo {

//    we used int because if the amount is 12.55$ , then stripe will automatically take it as 1255cents
    private int amount;
    private String currency;
    private String receiptEmail;
}
