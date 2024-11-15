package com.perunovpavel.servlet.ExchangeRate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.perunovpavel.dto.ExchangeRateDto.ExchangeRateRequestDto;
import com.perunovpavel.dto.ExchangeRateDto.ExchangeRateResponseDto;
import com.perunovpavel.entity.ExchangeRate;
import com.perunovpavel.exception.CurrencyNotFoundException;
import com.perunovpavel.exception.DatabaseUnavailableException;
import com.perunovpavel.exception.ExchangeRateAlreadyExistsException;
import com.perunovpavel.mapper.ExchangeRateMapper;
import com.perunovpavel.service.ExchangeRateService;
import com.perunovpavel.util.ServletUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private final ExchangeRateService exchangeRateService = new ExchangeRateService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<ExchangeRateResponseDto> exchangeRates = exchangeRateService.getExchangeRates();

            ServletUtil.setResponseHeaders(resp);

            mapper.writeValue(resp.getWriter(), exchangeRates);

        } catch (DatabaseUnavailableException exception) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String baseCode = req.getParameter("baseCurrencyCode");
            String targetCode = req.getParameter("targetCurrencyCode");
            String rateParam = req.getParameter("rate");

            if (baseCode == null || baseCode.isBlank() || targetCode == null || targetCode.isBlank() || rateParam == null || rateParam.isBlank()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters");
                return;
            }

            ExchangeRateRequestDto exchangeRateRequestDto = new ExchangeRateRequestDto(baseCode, targetCode, convertToNumber(rateParam));
            ExchangeRateResponseDto exchangeRateResponseDto = exchangeRateService.save(exchangeRateRequestDto);

            ServletUtil.setResponseHeaders(resp);

            mapper.writeValue(resp.getWriter(), exchangeRateResponseDto);
        } catch (DatabaseUnavailableException exception) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
        } catch (ExchangeRateAlreadyExistsException exception) {
            resp.sendError(HttpServletResponse.SC_CONFLICT, exception.getMessage());
        } catch (CurrencyNotFoundException exception) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        }

    }

    private static BigDecimal convertToNumber(String rate) {
        try {
            return new BigDecimal(rate);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid rate value: " + rate);
        }
    }
}
