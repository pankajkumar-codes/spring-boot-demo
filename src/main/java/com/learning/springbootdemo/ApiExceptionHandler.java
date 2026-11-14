package com.learning.springbootdemo;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Turns client mistakes into 4xx problem responses and hides internals from 5xx responses. */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler({MethodArgumentNotValidException.class, HandlerMethodValidationException.class,
            ConstraintViolationException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class,
            DataIntegrityViolationException.class, MultipartException.class})
    public ProblemDetail badRequest(Exception ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request");
    }

    /** The database is unreachable: tell the client to retry later instead of reporting a bug. */
    @ExceptionHandler({CannotCreateTransactionException.class, DataAccessResourceFailureException.class})
    public org.springframework.http.ResponseEntity<ProblemDetail> databaseUnavailable(Exception ex) {
        log.error("Database unavailable", ex);
        return org.springframework.http.ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", "5")
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable"));
    }

    /**
     * A query that hit the JDBC socket timeout leaves a dead connection, and the rollback that follows fails too.
     * Spring wraps that as a generic JpaSystemException, so look at the cause chain before calling it a bug.
     */
    @ExceptionHandler({QueryTimeoutException.class, JpaSystemException.class})
    public org.springframework.http.ResponseEntity<ProblemDetail> databaseTimeoutOrFailure(Exception ex) {
        if (ex instanceof QueryTimeoutException || isConnectionFailure(ex)) {
            return databaseUnavailable(ex);
        }
        log.error("Unhandled error", ex);
        return org.springframework.http.ResponseEntity.internalServerError()
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"));
    }

    private static boolean isConnectionFailure(Throwable ex) {
        for (Throwable t = ex; t != null; t = t.getCause() == t ? null : t.getCause()) {
            if (t instanceof java.net.SocketTimeoutException) {
                return true;
            }
            if (t instanceof java.sql.SQLException sql && (hasConnectionState(sql) || isClosedConnection(sql))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasConnectionState(java.sql.SQLException sql) {
        return sql.getSQLState() != null && sql.getSQLState().startsWith("08");
    }

    /** The pool reports a connection it already dropped without any SQLState, only a message. */
    private static boolean isClosedConnection(java.sql.SQLException sql) {
        String message = sql.getMessage() == null ? "" : sql.getMessage().toLowerCase(java.util.Locale.ROOT);
        return message.contains("connection is closed") || message.contains("connection has been closed");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail unexpected(Exception ex) throws Exception {
        if (ex instanceof org.springframework.web.ErrorResponse) {
            throw ex; // let Spring handle 404/405/415 and similar with their own status
        }
        log.error("Unhandled error", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }
}
