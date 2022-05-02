package com.intuit.task.manager.exceptions;

import com.intuit.task.manager.dto.ErrorResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import javax.validation.*;

/**
 * Class for Global exception handling
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionsHandler {

    /**
     * Handling an error when the requested process does not exist
     *
     * @param exception is a ProcessNotFoundException
     * @return a ResponseEntity with an error message containing the request ID and 404 status
     */
    @ExceptionHandler
    public ResponseEntity<ErrorResponseData> handleException(ProcessNotFoundException exception) {
        return new ResponseEntity<>(getResponse(exception.getMessage()), HttpStatus.NOT_FOUND);
    }

    /**
     * Handling exceptions specified in the method annotation related to request
     * validation problems and exceeding the allowed capacity
     *
     * @param exception one of the listed exception types
     * @return a ResponseEntity with an error message and 400 status
     */
    @ExceptionHandler({
            MaximumCapacityExceededException.class,
            UnableToApplyPriorityOrderException.class,
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            ValidationException.class
    })
    public ResponseEntity<ErrorResponseData> handleException(Exception exception) {
        return new ResponseEntity<>(getResponse(exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * Helper method that logs the error and generates the body for the response
     *
     * @param message this is the error message
     * @return a DTO with the error message and the current timestamp
     * @see ErrorResponseData
     */
    private ErrorResponseData getResponse(String message) {
        log.error(message);
        return new ErrorResponseData(message);
    }


}
