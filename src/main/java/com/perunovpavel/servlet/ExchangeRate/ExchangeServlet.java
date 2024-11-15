package com.perunovpavel.servlet.ExchangeRate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.perunovpavel.dto.ExchangeRateDto.ExchangeRateRequestDto;
import com.perunovpavel.dto.ExchangeRateDto.ExchangeRequestDto;
import com.perunovpavel.dto.ExchangeRateDto.ExchangeResponseDto;
import com.perunovpavel.exception.CurrencyNotFoundException;
import com.perunovpavel.exception.DatabaseUnavailableException;
import com.perunovpavel.exception.ExchangeRatePairNotFoundException;
import com.perunovpavel.service.ExchangeService;
import com.perunovpavel.util.ServletUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {
    private final ExchangeService exchangeService = new ExchangeService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            String fromCurrency = req.getParameter("from");
            String toCurrency = req.getParameter("to");
            String amountParam = req.getParameter("amount");

            if (amountParam == null || amountParam.isBlank()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter: amount");
                return;
            }

            ServletUtil.setResponseHeaders(resp);

            ExchangeRequestDto exchangeRequestDto = new ExchangeRequestDto(fromCurrency, toCurrency, new BigDecimal(amountParam));

            ExchangeResponseDto exchangeResponseDto = exchangeService.exchange(exchangeRequestDto);

            mapper.writeValue(resp.getWriter(), exchangeResponseDto);
        } catch (DatabaseUnavailableException exception) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
        } catch (CurrencyNotFoundException exception) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (ExchangeRatePairNotFoundException exception) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, exception.getMessage());
        }

    }
}
