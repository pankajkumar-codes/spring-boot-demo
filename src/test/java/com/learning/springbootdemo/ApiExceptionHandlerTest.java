package com.learning.springbootdemo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.SocketTimeoutException;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.jpa.JpaSystemException;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void socketTimeoutDuringRollbackIs503WithRetryAfter() {
        var cause = new SQLException("Read timed out", "08006", new SocketTimeoutException("Read timed out"));
        var response = handler.databaseTimeoutOrFailure(
                new JpaSystemException(new jakarta.persistence.PersistenceException("Unable to rollback", cause)));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("5", response.getHeaders().getFirst("Retry-After"));
    }

    @Test
    void rollbackOnClosedConnectionIs503() {
        var cause = new SQLException("Connection is closed");
        var response = handler.databaseTimeoutOrFailure(
                new JpaSystemException(new jakarta.persistence.PersistenceException("Unable to rollback", cause)));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    void queryTimeoutIs503() {
        var response = handler.databaseTimeoutOrFailure(new QueryTimeoutException("timeout"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    void otherJpaSystemErrorsStay500() {
        var response = handler.databaseTimeoutOrFailure(
                new JpaSystemException(new jakarta.persistence.PersistenceException("boom", new SQLException("bad", "XX000"))));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
