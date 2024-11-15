package com.perunovpavel.dto.ExchangeRateDto;

import com.perunovpavel.dto.CurrencyDto.CurrencyResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRateRequestDto {
    private String baseCurrency;
    private String targetCurrency;
    private BigDecimal rate;
}
