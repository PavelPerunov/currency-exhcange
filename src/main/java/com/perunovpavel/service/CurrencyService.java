package com.perunovpavel.service;

import com.perunovpavel.dao.CurrencyDao;
import com.perunovpavel.dto.CurrencyDto.CurrencyRequestDto;
import com.perunovpavel.dto.CurrencyDto.CurrencyResponseDto;
import com.perunovpavel.entity.Currency;
import com.perunovpavel.mapper.CurrencyMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class CurrencyService {
    private final CurrencyDao currencyDao = CurrencyDao.getInstance();
    private final CurrencyMapper mapper = CurrencyMapper.INSTANCE;

    public List<CurrencyResponseDto> getAllCurrencies() {
        List<Currency> currencies = currencyDao.getAllCurrencies();
        List<CurrencyResponseDto> currencyResponseDtos = new ArrayList<>();
        for (Currency currency : currencies) {
            currencyResponseDtos.add(mapper.toResponseDto(currency));
        }
        return currencyResponseDtos;
    }

    public CurrencyResponseDto getCurrencyByCode(String code) {
        Optional<Currency> currency = currencyDao.getCurrencyByCode(code);
        return mapper.toResponseDto(currency.get());
    }

    public CurrencyResponseDto save(CurrencyRequestDto currencyRequestDto) {
        Currency currency = currencyDao.save(mapper.toEntity(currencyRequestDto));
        return mapper.toResponseDto(currency);

    }
}
