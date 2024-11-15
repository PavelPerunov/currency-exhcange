package com.perunovpavel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.perunovpavel.dao.CurrencyDao;
import com.perunovpavel.dao.ExchangeRateDao;
import com.perunovpavel.dto.ExchangeRateDto.ExchangeRateRequestDto;
import com.perunovpavel.dto.ExchangeRateDto.ExchangeRateResponseDto;
import com.perunovpavel.dto.ExchangeRateDto.ExchangeRequestDto;
import com.perunovpavel.dto.ExchangeRateDto.ExchangeResponseDto;
import com.perunovpavel.entity.Currency;
import com.perunovpavel.entity.ExchangeRate;
import com.perunovpavel.exception.CurrencyNotFoundException;
import com.perunovpavel.exception.ExchangeRatePairNotFoundException;
import com.perunovpavel.mapper.ExchangeRateMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateService {
    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();
    private final CurrencyDao currencyDao = CurrencyDao.getInstance();
    private final ExchangeRateMapper mapper = ExchangeRateMapper.INSTANCE;

    public List<ExchangeRateResponseDto> getExchangeRates() {
        List<ExchangeRate> exchangeRates = exchangeRateDao.getAllRates();
        List<ExchangeRateResponseDto> exchangeRateResponseDtos = new ArrayList<>();
        for (ExchangeRate exchangeRate : exchangeRates) {
            exchangeRateResponseDtos.add(mapper.toResponseDto(exchangeRate));
        }
        return exchangeRateResponseDtos;
    }

    public ExchangeRateResponseDto getExchangeRateByCodes(String baseCurrency, String targetCurrency) {
        ExchangeRate exchangeRate = exchangeRateDao.getExchangeRateByCodes(baseCurrency, targetCurrency).
                orElseThrow(() -> new ExchangeRatePairNotFoundException("Exchange rate for the pair was not found"));
        return mapper.toResponseDto(exchangeRate);
    }

    public ExchangeRateResponseDto save(ExchangeRateRequestDto exchangeRateRequestDto) {
        String baseCurrencyCode = exchangeRateRequestDto.getBaseCurrency();
        String targetCurrencyCode = exchangeRateRequestDto.getTargetCurrency();

        Currency baseCurrency = currencyDao.getCurrencyByCode(baseCurrencyCode).orElseThrow(
                () -> new CurrencyNotFoundException("Currency with " + baseCurrencyCode + " not found"));
        Currency targetCurrency = currencyDao.getCurrencyByCode(targetCurrencyCode).orElseThrow(
                () -> new CurrencyNotFoundException("Currency with " + targetCurrencyCode + " not found"));

        ExchangeRate exchangeRate = new ExchangeRate(baseCurrency, targetCurrency, exchangeRateRequestDto.getRate());

        return mapper.toResponseDto(exchangeRateDao.save(exchangeRate));
    }
    public ExchangeRateResponseDto update(ExchangeRateRequestDto exchangeRateRequestDto) {
        String baseCurrencyCode = exchangeRateRequestDto.getBaseCurrency();
        String targetCurrencyCode = exchangeRateRequestDto.getTargetCurrency();

        Currency baseCurrency = currencyDao.getCurrencyByCode(baseCurrencyCode).orElseThrow(
                () -> new CurrencyNotFoundException("Exchange rate with code " + baseCurrencyCode + " not found"));
        Currency targetCurrency = currencyDao.getCurrencyByCode(targetCurrencyCode).orElseThrow(
                () -> new CurrencyNotFoundException("Exchange rate with code " + targetCurrencyCode + " not found"));

        ExchangeRate exchangeRate = new ExchangeRate(baseCurrency, targetCurrency, exchangeRateRequestDto.getRate());

        return mapper.toResponseDto(exchangeRateDao.update(exchangeRate));
    }

}
