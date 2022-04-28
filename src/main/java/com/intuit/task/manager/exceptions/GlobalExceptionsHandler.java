package com.intuit.task.manager.exceptions;

import com.intuit.task.manager.dto.ErrorResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import javax.validation.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionsHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponseData> handleException(ProcessNotFoundException exception) {
        return new ResponseEntity<>(getResponse(exception.getMessage()), HttpStatus.NOT_FOUND);
    }

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

    private ErrorResponseData getResponse(String message) {
        log.error(message);
        return new ErrorResponseData(message);
    }


}
