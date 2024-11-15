package com.perunovpavel.mapper;


import com.perunovpavel.dto.ExchangeRateDto.ExchangeRateRequestDto;
import com.perunovpavel.dto.ExchangeRateDto.ExchangeRateResponseDto;
import com.perunovpavel.entity.ExchangeRate;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = CurrencyMapper.class)
public interface ExchangeRateMapper {
    ExchangeRateMapper INSTANCE = Mappers.getMapper(ExchangeRateMapper.class);

    ExchangeRateResponseDto toResponseDto(ExchangeRate exchangeRate);

}
