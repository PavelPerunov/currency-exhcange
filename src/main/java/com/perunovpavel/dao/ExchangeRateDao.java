package com.perunovpavel.dao;

import com.perunovpavel.dto.ExchangeRateDto.ExchangeRateResponseDto;
import com.perunovpavel.entity.Currency;
import com.perunovpavel.entity.ExchangeRate;
import com.perunovpavel.exception.CurrencyAlreadyExistsException;
import com.perunovpavel.exception.DatabaseUnavailableException;
import com.perunovpavel.exception.ExchangeRateAlreadyExistsException;
import com.perunovpavel.exception.ExchangeRatePairNotFoundException;
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

public class ExchangeRateDao {
    private static final ExchangeRateDao INSTANCE = new ExchangeRateDao();

    private static final String FIND_ALL_RATES = """
             SELECT
                       e.ID,
                       b.id AS base_id,
                       b.fullName AS base_fullName,
                       b.code AS base_code,
                       b.sign AS base_sign,
                       t.id AS target_id,
                       t.fullName AS target_fullName,
                       t.code AS target_code,
                       t.sign AS target_sign,
                       e.rate
                   FROM\s
                       ExchangeRates e
                   JOIN\s
                       Currencies b ON e.baseCurrencyId = b.ID
                   JOIN\s
                       Currencies t ON e.targetCurrencyId = t.ID
            """;

    private static final String FIND_RATE_BY_CODES = """
            SELECT 
                e.id,
                b.id AS base_id,
                b.fullName AS base_fullName,
                b.code AS base_code,
                b.sign AS base_sign,
                t.id AS target_id,
                t.fullName AS target_fullName,
                t.code AS target_code,
                t.sign AS target_sign,
                e.rate
            FROM\s
                ExchangeRates e
            JOIN\s
                Currencies b ON e.baseCurrencyId = b.id
            JOIN\s
                Currencies t ON e.targetCurrencyId = t.id
            WHERE\s
                b.code = ? and t.code = ?
            """;


    private static final String INSERT = """
            INSERT INTO ExchangeRates  (baseCurrencyId,targetCurrencyId,rate)
            VALUES (?,?,?) RETURNING id
            """;

    private static final String UPDATE = """
            UPDATE ExchangeRates SET rate = ?
                     WHERE baseCurrencyId = ? and targetCurrencyId = ?
            RETURNING *
            """;

    private ExchangeRateDao() {
    }

    public static ExchangeRateDao getInstance() {
        return INSTANCE;
    }

    public List<ExchangeRate> getAllRates() {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_RATES)) {
            List<ExchangeRate> exchangeRates = new ArrayList<>();
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                exchangeRates.add(build(resultSet));
            }
            return exchangeRates;
        } catch (SQLException e) {
            throw new DatabaseUnavailableException("Database unavailable");
        }
    }


    public ExchangeRate build(ResultSet resultSet) throws SQLException {
        return new ExchangeRate(
                resultSet.getInt("id"),
                new Currency(resultSet.getInt("base_id"),
                        resultSet.getString("base_fullName"),
                        resultSet.getString("base_code"),
                        resultSet.getString("base_sign")),
                new Currency(resultSet.getInt("target_id"),
                        resultSet.getString("target_fullName"),
                        resultSet.getString("target_code"),
                        resultSet.getString("target_sign")),
                resultSet.getBigDecimal("rate")
        );
    }

    public Optional<ExchangeRate> getExchangeRateByCodes(String baseCurrency, String targetCurrency) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_RATE_BY_CODES)) {
            preparedStatement.setString(1, baseCurrency);
            preparedStatement.setString(2, targetCurrency);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return Optional.empty();
            }
            return Optional.of(build(resultSet));
        } catch (SQLException exception) {
            throw new DatabaseUnavailableException("Database unavailable");
        }
    }

    public ExchangeRate save(ExchangeRate exchangeRate) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT)) {
            preparedStatement.setString(1, exchangeRate.getBaseCurrency().getCode());
            preparedStatement.setString(2, exchangeRate.getTargetCurrency().getCode());
            preparedStatement.setBigDecimal(3, exchangeRate.getRate());

            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                throw new DatabaseUnavailableException("Failed to save exchange rate");
            }
            exchangeRate.setId(resultSet.getInt("id"));
            return exchangeRate;
        } catch (SQLException exception) {
            if (exception instanceof SQLiteException) {
                if (((SQLiteException) exception).getResultCode().code == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE.code) {
                    throw new ExchangeRateAlreadyExistsException("Exchange rate already exists");
                }
            }
            throw new DatabaseUnavailableException("Database Unavailable");
        }
    }

    public ExchangeRate update(ExchangeRate exchangeRate) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE)) {

            preparedStatement.setBigDecimal(1, exchangeRate.getRate());
            preparedStatement.setInt(2, exchangeRate.getBaseCurrency().getId());
            preparedStatement.setInt(3, exchangeRate.getTargetCurrency().getId());

            ResultSet resultSet = preparedStatement.executeQuery();

            exchangeRate.setRate(resultSet.getBigDecimal("rate"));
            exchangeRate.setId(resultSet.getInt("id"));

            return exchangeRate;
        } catch (SQLException exception) {
            throw new DatabaseUnavailableException("Database unavailable");
        }
    }
}
