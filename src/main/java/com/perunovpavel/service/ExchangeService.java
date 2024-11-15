package com.perunovpavel.service;

import com.perunovpavel.dao.ExchangeRateDao;
import com.perunovpavel.dto.ExchangeRateDto.ExchangeRequestDto;
import com.perunovpavel.dto.ExchangeRateDto.ExchangeResponseDto;
import com.perunovpavel.entity.ExchangeRate;
import com.perunovpavel.exception.ExchangeRatePairNotFoundException;
import com.perunovpavel.mapper.CurrencyMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class ExchangeService {
    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();
    private final CurrencyMapper mapper = CurrencyMapper.INSTANCE;

    public ExchangeResponseDto exchange(ExchangeRequestDto exchangeRequestDto) {
        ExchangeRate exchangeRate = findExchangeRate(exchangeRequestDto).orElseThrow(() -> new ExchangeRatePairNotFoundException("Exchange rate pair not found in database"));

        BigDecimal amount = exchangeRequestDto.getAmount();
        BigDecimal convertedAmount = amount.multiply(exchangeRate.getRate()).setScale(2, RoundingMode.HALF_UP);

        return new ExchangeResponseDto(mapper.toResponseDto(exchangeRate.getBaseCurrency()), mapper.toResponseDto(exchangeRate.getTargetCurrency()), exchangeRate.getRate(), amount, convertedAmount);
    }

    private Optional<ExchangeRate> findExchangeRate(ExchangeRequestDto exchangeRequestDto) {
        Optional<ExchangeRate> exchangeRate = exchangeRateDao.getExchangeRateByCodes(exchangeRequestDto.getFromCurrencyCode(), exchangeRequestDto.getToCurrencyCode());

        if (exchangeRate.isEmpty()) {
            exchangeRate = findReverseRate(exchangeRequestDto);
        }

        if (exchangeRate.isEmpty()) {
            exchangeRate = findCrossRate(exchangeRequestDto);
        }

        return exchangeRate;
    }


    private Optional<ExchangeRate> findReverseRate(ExchangeRequestDto exchangeRequestDto) {
        Optional<ExchangeRate> exchangeRateOptional = exchangeRateDao.getExchangeRateByCodes(exchangeRequestDto.getToCurrencyCode(), exchangeRequestDto.getFromCurrencyCode());

        if (exchangeRateOptional.isEmpty()) {
            return Optional.empty();
        }

        ExchangeRate exchangeRate = exchangeRateOptional.get();

        BigDecimal rate = BigDecimal.ONE.divide(exchangeRate.getRate()).setScale(2, RoundingMode.HALF_UP);

        return Optional.of(new ExchangeRate(exchangeRate.getTargetCurrency(), exchangeRate.getBaseCurrency(), rate));
    }

    private Optional<ExchangeRate> findCrossRate(ExchangeRequestDto exchangeRequestDto) {
        Optional<ExchangeRate> usdToBaseOptional = exchangeRateDao.getExchangeRateByCodes("USD", exchangeRequestDto.getFromCurrencyCode());
        Optional<ExchangeRate> usdToTargetOptional = exchangeRateDao.getExchangeRateByCodes("USD", exchangeRequestDto.getToCurrencyCode());

        if (usdToBaseOptional.isEmpty() || usdToTargetOptional.isEmpty()) {
            return Optional.empty();
        }

        ExchangeRate usdToBase = usdToBaseOptional.get();
        ExchangeRate usdToTarget = usdToTargetOptional.get();

        BigDecimal rate = usdToTarget.getRate().divide(usdToBase.getRate(), 2, RoundingMode.HALF_UP);

        return Optional.of(new ExchangeRate(usdToBase.getTargetCurrency(),
                usdToTarget.getTargetCurrency(),
                rate));
    }


}
