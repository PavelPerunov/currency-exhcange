package com.perunovpavel.servlet.Currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.perunovpavel.exception.CurrencyNotFoundException;
import com.perunovpavel.exception.DatabaseUnavailableException;
import com.perunovpavel.service.CurrencyService;
import com.perunovpavel.util.ServletUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {
    private final CurrencyService currencyService = new CurrencyService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String code = req.getPathInfo().replaceFirst("/", "");

            if (code == null || code.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Currency code is missing from the address");
                return;
            }

            ServletUtil.setResponseHeaders(resp);

            mapper.writeValue(resp.getWriter(), currencyService.getCurrencyByCode(code));
        } catch (DatabaseUnavailableException exception) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
        } catch (CurrencyNotFoundException exception) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, exception.getMessage());
        }
    }
}
