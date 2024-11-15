package com.perunovpavel.dto.ExchangeRateDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRequestDto {
    private String fromCurrencyCode;
    private String toCurrencyCode;
    private BigDecimal amount;
}
