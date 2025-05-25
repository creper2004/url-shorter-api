package org.example.url_shorter_test_task_gorokhov.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.url_shorter_test_task_gorokhov.dto.ExpandRequest;
import org.example.url_shorter_test_task_gorokhov.dto.ExpandResponse;
import org.example.url_shorter_test_task_gorokhov.dto.ShortenRequest;
import org.example.url_shorter_test_task_gorokhov.dto.ShortenResponse;
import org.example.url_shorter_test_task_gorokhov.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/clk")
@Tag(name = "Short Link API", description = "API для управления короткими ссылками (создание, продление, редирект)")
public class UrlController {

    private UrlService urlService;

    @PostMapping("/shorten")
    @Operation(
            summary = "Создание короткой ссылки",
            description = "Генерирует короткую ссылку на основе полного URL. Если ссылка уже существует — возвращает существующую короткую ссылку.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Оригинальный URL, который нужно сократить",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Пример запроса",
                                    summary = "Простой пример",
                                    value = "{ \"fullUrl\": \"https://example.com/page\" }"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Сокращённая ссылка успешно создана",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Успешный ответ",
                                            summary = "Короткая ссылка",
                                            value = "{ \"shortUrl\": \"http://localhost:8080/clk/abc12345\" }"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Некорректный запрос\n"
                                    + "- Неверный формат URL",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "Ошибка валидации (пример 1)",
                                                    summary = "Невалидный URL",
                                                    value = """
                    {
                      "violations": [
                        {
                          "fieldName": "fullUrl",
                          "message": "Invalid URL format"
                        }
                      ]
                    }
                    """
                                            ),
                                            @ExampleObject(
                                                    name = "Ошибка валидации (пример 2)",
                                                    summary = "Пустой URL",
                                                    value = """
                    {
                      "violations": [
                        {
                          "fieldName": "fullUrl",
                          "message": "Full URL is blank"
                        }
                      ]
                    }
                    """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Ошибка при генерации короткой ссылки",
                            content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Ошибка сервера",
                                    summary = "Ошибка сервера",
                                    value = "{ \"message\": \"Server error\" }"
                            )
                    )
                    )
            }
    )
    public ResponseEntity<ShortenResponse> shorten(@RequestBody @Valid ShortenRequest request) {
        log.info("Received request to shorten URL: {}", request.fullUrl());
        String code = urlService.shorten(request.fullUrl());
        log.info("Shortened URL generated: {}", code);
        return ResponseEntity.ok(new ShortenResponse(code));
    }

    @Operation(
            summary = "Получение полной ссылки",
            description = "Получение полной ссылки по короткой, даже если срок ее действия истек.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Сокращенный URL, по которому нужно получить полную ссылку",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Пример запроса",
                                    summary = "Простой пример",
                                    value = "{ \"shortUrl\": \"http://localhost:8080/clk/abc12345\" }"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Полная ссылка успешно получена",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Успешный ответ",
                                            summary = "Короткая ссылка",
                                            value = "{ \"fullUrl\": \"https://example.com/page\" }"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Некорректный запрос\n"
                                    + "- Неверный формат URL",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "Ошибка валидации (пример 1)",
                                                    summary = "Невалидный URL",
                                                    value = """
                    {
                      "violations": [
                        {
                          "fieldName": "shortUrl",
                          "message": "Invalid URL format"
                        }
                      ]
                    }
                    """
                                            ),
                                            @ExampleObject(
                                                    name = "Ошибка валидации (пример 2)",
                                                    summary = "Пустой URL",
                                                    value = """
                    {
                      "violations": [
                        {
                          "fieldName": "shortUrl",
                          "message": "Short URL is blank"
                        }
                      ]
                    }
                    """
                                            ),
                                            @ExampleObject(
                                                    name = "Ошибка валидации (пример 3)",
                                                    summary = "Невалидный атрибут ссылки",
                                                    value = """
                    {
                    
                       "error": "ParseShortCodeException",
                       "message": "Invalid host. Expected: localhost."
                   
                    }
                    """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Некорректный запрос\n"
                                    + "- Несуществующая ссылка",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "Некорректный запрос",
                                                    summary = "Несуществующая ссылка",
                                                    value = """
                    
                            {
                              "error": "EntityNotFoundException",
                              "message": "Short link not found: abc12345"
                            }
                    
                    """
                                            ),
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Ошибка при получении полной ссылки",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Ошибка сервера",
                                            summary = "Ошибка сервера",
                                            value = "{ \"message\": \"Server error\" }"
                                    )
                            )
                    )
            }
    )
    @PostMapping("/expand")
    public ResponseEntity<ExpandResponse> expand(@RequestBody @Valid ExpandRequest expandRequest) {
        log.info("Received request to expand short URL: {}", expandRequest.shortUrl());
        String fullUrl = urlService.expandByShortUrl(expandRequest.shortUrl());
        log.info("Expanded full URL: {}", fullUrl);
        return ResponseEntity.ok(new ExpandResponse(fullUrl));
    }

    @Operation(
            summary = "Продлить время работы ссылки",
            description = "Продлить время работы ссылки: ссылка актуальна на время TTL, указанное в настройках проекта (по умолчанию - 10 минут)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Сокращенный URL, время которого нужно продлить",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Пример запроса",
                                    summary = "Простой пример",
                                    value = "{ \"shortUrl\": \"http://localhost:8080/clk/abc12345\" }"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Сокращенная ссылка успещно продлена"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Некорректный запрос\n"
                                    + "- Неверный формат URL",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "Ошибка валидации (пример 1)",
                                                    summary = "Невалидный URL",
                                                    value = """
                    {
                      "violations": [
                        {
                          "fieldName": "shortUrl",
                          "message": "Invalid URL format"
                        }
                      ]
                    }
                    """
                                            ),
                                            @ExampleObject(
                                                    name = "Ошибка валидации (пример 2)",
                                                    summary = "Пустой URL",
                                                    value = """
                    {
                      "violations": [
                        {
                          "fieldName": "shortUrl",
                          "message": "Short URL is blank"
                        }
                      ]
                    }
                    """
                                            ),
                                            @ExampleObject(
                                                    name = "Ошибка валидации (пример 3)",
                                                    summary = "Невалидный атрибут ссылки",
                                                    value = """
                    {
                    
                       "error": "ParseShortCodeException",
                       "message": "Invalid host. Expected: localhost."
                   
                    }
                    """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Некорректный запрос\n"
                                    + "- Несуществующая ссылка",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "Некорректный запрос",
                                                    summary = "Несуществующая ссылка",
                                                    value = """
                    
                            {
                              "error": "EntityNotFoundException",
                              "message": "Short link not found: abc12345"
                            }
                    
                    """
                                            ),
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Ошибка при получении полной ссылки",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Ошибка сервера",
                                            summary = "Ошибка сервера",
                                            value = "{ \"message\": \"Server error\" }"
                                    )
                            )
                    )
            }
    )
    @PostMapping("/prolong")
    public ResponseEntity<Void> prolong(@RequestBody @Valid ExpandRequest expandRequest) {
        log.info("Received request to prolong short URL: {}", expandRequest.shortUrl());
        urlService.prolongShortUrl(expandRequest.shortUrl());
        log.info("Short URL prolonged successfully.");
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(
            summary = "Редирект по короткой ссылке",
            description = "Перенаправляет пользователя на оригинальный URL, соответствующий короткому коду. " +
                    "Ссылка может быть просроченной (403) или не существовать (404).",
            responses = {
                    @ApiResponse(
                            responseCode = "301",
                            description = "Успешный редирект на оригинальный URL",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Невалидный код (должен состоять только из символов латинского алфавита и цифр). Длина - 8.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Невалидный код",
                                            summary = "Невалидный код",
                                            value = """
            {
              "error": "IllegalArgumentException",
              "message": "Short code must be exactly 8 characters long and contain only letters and digits: !2w`sd20q///"
            }
            """
                                    )
                            )
                    ),

                    @ApiResponse(
                            responseCode = "403",
                            description = "Срок действия короткой ссылки истёк",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Срок действия ссылки истёк",
                                            summary = "Срок действия ссылки истёк",
                                            value = """
                                    {
                                      "error": "LinkExpiredException",
                                      "message": "This link has expired"
                                    }
                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Короткая ссылка не найдена",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Ссылка не найдена",
                                            summary = "Ссылка не найдена",
                                            value = """
                                                {
                                                  "error": "EntityNotFoundException",
                                                  "message": "Short link not found: abcde123"
                                                }
                            """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @Parameter(
                    name = "shortCode",
                    description = "Короткий код, соответствующий оригинальному URL",
                    required = true,
                    example = "abc12345"
            )
            @PathVariable String shortCode
    ) {
        log.info("Received redirect request for short code: {}", shortCode);
        String url = urlService.expandForRedirect(shortCode);
        log.info("Redirecting to full URL: {}", url);
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(URI.create(url))
                .build();
    }

}