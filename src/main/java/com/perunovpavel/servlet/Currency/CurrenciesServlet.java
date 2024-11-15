package com.perunovpavel.servlet.Currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.perunovpavel.dto.CurrencyDto.CurrencyRequestDto;
import com.perunovpavel.dto.CurrencyDto.CurrencyResponseDto;
import com.perunovpavel.entity.Currency;
import com.perunovpavel.exception.CurrencyAlreadyExistsException;
import com.perunovpavel.exception.DatabaseUnavailableException;
import com.perunovpavel.mapper.CurrencyMapper;
import com.perunovpavel.service.CurrencyService;
import com.perunovpavel.util.ServletUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;


@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {
    private final CurrencyService currencyService = new CurrencyService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<CurrencyResponseDto> currencyResponseDtos = currencyService.getAllCurrencies();

            ServletUtil.setResponseHeaders(resp);

            mapper.writeValue(resp.getWriter(), currencyResponseDtos);
        } catch (DatabaseUnavailableException exception) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String code = req.getParameter("code");
            String fullName = req.getParameter("name");
            String sign = req.getParameter("sign");

            if (code.isBlank() || fullName.isBlank() || sign.isBlank()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters");
                return;
            }

            ServletUtil.setResponseHeaders(resp);

            CurrencyRequestDto requestDto = CurrencyMapper.INSTANCE.toRequestDto(
                    new Currency(code, fullName, sign));

            CurrencyResponseDto currencyResponseDto = currencyService.save(requestDto);
            mapper.writeValue(resp.getWriter(), currencyResponseDto);
        } catch (DatabaseUnavailableException exception) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
        } catch (CurrencyAlreadyExistsException exception) {
            resp.sendError(HttpServletResponse.SC_CONFLICT, exception.getMessage());
        }
    }
}
