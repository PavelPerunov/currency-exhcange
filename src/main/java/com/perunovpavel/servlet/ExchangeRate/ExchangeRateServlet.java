package com.perunovpavel.servlet.ExchangeRate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.perunovpavel.dto.ExchangeRateDto.ExchangeRateRequestDto;
import com.perunovpavel.dto.ExchangeRateDto.ExchangeRateResponseDto;
import com.perunovpavel.exception.CurrencyNotFoundException;
import com.perunovpavel.exception.DatabaseUnavailableException;
import com.perunovpavel.exception.ExchangeRatePairNotFoundException;
import com.perunovpavel.service.ExchangeRateService;
import com.perunovpavel.util.ServletUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private final ExchangeRateService exchangeRateService = new ExchangeRateService();
    private final ObjectMapper mapper = new ObjectMapper();


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getMethod().equalsIgnoreCase("PATCH")) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String currencyCodes = req.getPathInfo().replaceFirst("/", "");

            if (currencyCodes.length() != 6) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Currency codes are either not provided or provided in an incorrect format");
                return;
            }

            String baseCurrency = currencyCodes.substring(0, 3);
            String targetCurrency = currencyCodes.substring(3, 6);

            ServletUtil.setResponseHeaders(resp);

            ExchangeRateResponseDto exchangeRate = exchangeRateService.getExchangeRateByCodes(baseCurrency, targetCurrency);

            mapper.writeValue(resp.getWriter(), exchangeRate);

        } catch (DatabaseUnavailableException exception) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
        } catch (ExchangeRatePairNotFoundException exception) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, exception.getMessage());
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {

            String currencyCodes = req.getPathInfo().replaceFirst("/", "");

            String parameter = req.getReader().readLine();

            if (parameter == null || !parameter.contains("rate")) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter");
                return;
            }

            String rateParam = parameter.replace("rate=", "");

            if (rateParam.isBlank()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter");
                return;
            }

            ServletUtil.setResponseHeaders(resp);

            BigDecimal rate = new BigDecimal(rateParam);
            String baseCurrency = currencyCodes.substring(0, 3);
            String targetCurrency = currencyCodes.substring(3, 6);

            ExchangeRateRequestDto exchangeRateRequestDto = new ExchangeRateRequestDto(baseCurrency, targetCurrency, rate);
            ExchangeRateResponseDto exchangeRateResponseDto = exchangeRateService.update(exchangeRateRequestDto);

            mapper.writeValue(resp.getWriter(), exchangeRateResponseDto);
        } catch (DatabaseUnavailableException exception) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
        } catch (CurrencyNotFoundException exception) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, exception.getMessage());
        }
    }
}
