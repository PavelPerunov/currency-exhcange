package com.perunovpavel.dao;

import com.perunovpavel.entity.Currency;
import com.perunovpavel.exception.CurrencyAlreadyExistsException;
import com.perunovpavel.exception.CurrencyNotFoundException;
import com.perunovpavel.exception.DatabaseUnavailableException;
import com.perunovpavel.util.ConnectionManager;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyDao {
    private static final CurrencyDao INSTANCE = new CurrencyDao();

    private static final String FIND_ALL_CURRENCIES = """
            SELECT * FROM Currencies
            """;

    private static final String FIND_BY_CODE = """
            SELECT * FROM Currencies where code = ?
            """;

    private static final String INSERT = """
            INSERT INTO Currencies (code, fullName, sign) VALUES (?,?,?) RETURNING id;
            """;

    private CurrencyDao() {
    }

    public static CurrencyDao getInstance() {
        return INSTANCE;
    }

    public Currency build(ResultSet resultSet) throws SQLException {
        return new Currency(resultSet.getInt("id"),
                resultSet.getString("code"),
                resultSet.getString("fullName"),
                resultSet.getString("sign"));
    }

    public List<Currency> getAllCurrencies() {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL_CURRENCIES)) {
            List<Currency> currencies = new ArrayList<>();
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                currencies.add(build(resultSet));
            }
            return currencies;
        } catch (SQLException exception) {
            throw new DatabaseUnavailableException("Database unavailable");
        }
    }

    public Optional<Currency> getCurrencyByCode(String code) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_CODE)) {
            preparedStatement.setString(1, code);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return Optional.empty();
//                throw new CurrencyNotFoundException("Currency with " + code + " not found");
            }
            return Optional.of(build(resultSet));
        } catch (SQLException exception) {
            throw new DatabaseUnavailableException("Database Unavailable");
        }
    }

    public Currency save(Currency currency) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT)) {

            preparedStatement.setString(1, currency.getCode());
            preparedStatement.setString(2, currency.getFullName());
            preparedStatement.setString(3, currency.getSign());

            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                throw new DatabaseUnavailableException("Failed to save currency with code " + currency.getCode() + " to the database");
            }

            currency.setId(resultSet.getInt("id"));

            return currency;
        } catch (SQLException exception) {
            if (exception instanceof SQLiteException) {
                if (((SQLiteException) exception).getResultCode().code == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE.code) {
                    throw new CurrencyAlreadyExistsException("Currency with " + currency.getCode() + " already exists");
                }
            }
            throw new DatabaseUnavailableException("Database Unavailable");
        }
    }

}
