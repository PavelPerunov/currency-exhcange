package com.perunovpavel.mapper;

import com.perunovpavel.dto.CurrencyDto.CurrencyRequestDto;
import com.perunovpavel.dto.CurrencyDto.CurrencyResponseDto;
import com.perunovpavel.entity.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CurrencyMapper {
    CurrencyMapper INSTANCE = Mappers.getMapper(CurrencyMapper.class);

    @Mapping(source = "fullName", target = "name")
    CurrencyResponseDto toResponseDto(Currency currency);

    @Mapping(source = "fullName", target = "name")
    CurrencyRequestDto toRequestDto(Currency currency);

    @Mapping(source = "name", target = "fullName")
    Currency toEntity(CurrencyRequestDto currencyRequestDto);
}
