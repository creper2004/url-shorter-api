package org.example.url_shorter_test_task_gorokhov.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ShortCodeGenerationException extends RuntimeException {
    public ShortCodeGenerationException(String message) {
        super(message);
    }
}
