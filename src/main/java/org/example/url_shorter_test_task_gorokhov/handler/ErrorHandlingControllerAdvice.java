package org.example.url_shorter_test_task_gorokhov.handler;

import jakarta.persistence.EntityNotFoundException;
import org.example.url_shorter_test_task_gorokhov.dto.ErrorResponse;
import org.example.url_shorter_test_task_gorokhov.exception.LinkExpiredException;
import org.example.url_shorter_test_task_gorokhov.exception.ParseCodeErrorResponse;
import org.example.url_shorter_test_task_gorokhov.exception.ParseShortCodeException;
import org.example.url_shorter_test_task_gorokhov.exception.ShortCodeGenerationException;
import org.example.url_shorter_test_task_gorokhov.exception.TooManyRequestsException;
import org.example.url_shorter_test_task_gorokhov.exception.AuthException;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@ControllerAdvice()
public class ErrorHandlingControllerAdvice {

    @ExceptionHandler(ParseShortCodeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ParseCodeErrorResponse onParseShortCodeException(ParseShortCodeException ex){
        return new ParseCodeErrorResponse("ParseShortCodeException", ex.toString());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorResponse handleValidationErrors(MethodArgumentNotValidException ex) {
        final List<Violation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new Violation(error.getField(), error.getDefaultMessage()))
                .toList();
        return new ValidationErrorResponse(violations);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> onIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("IllegalArgumentException", ex.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> onEntityNotFoundException(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("EntityNotFoundException", ex.getMessage()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> onEntityNotFoundException(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("UsernameNotFoundException", ex.getMessage()));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> onAuthException(AuthException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("AuthException", ex.getMessage()));
    }

    @ExceptionHandler(ShortCodeGenerationException.class)
    public ResponseEntity<ErrorResponse> handleShortCodeGenerationException(ShortCodeGenerationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("ShortCodeGenerationException ", ex.getMessage()));
    }

    @ExceptionHandler(LinkExpiredException.class)
    public ResponseEntity<ErrorResponse> handleLinkExpired(LinkExpiredException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("LinkExpiredException", ex.getMessage()));
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyRequests(TooManyRequestsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ErrorResponse("TooManyRequestsException", ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("RuntimeException", "Unexpected error: "+ ex.getMessage()));
    }

}