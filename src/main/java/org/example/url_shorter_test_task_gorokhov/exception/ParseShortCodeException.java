package org.example.url_shorter_test_task_gorokhov.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParseShortCodeException extends RuntimeException{

    private final String expectedAtr;

    public ParseShortCodeException(String expectedAtr, String message) {
        super(message);
        this.expectedAtr = expectedAtr;

    }

    @Override
    public String toString() {
        return getMessage() + ". " +
                (expectedAtr.isEmpty() ? "" : "Expected: " + expectedAtr + ".");
    }
}