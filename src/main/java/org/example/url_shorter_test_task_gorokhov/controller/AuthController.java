package org.example.url_shorter_test_task_gorokhov.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.example.url_shorter_test_task_gorokhov.dto.ErrorResponse;
import org.example.url_shorter_test_task_gorokhov.dto.LoginRequest;
import org.example.url_shorter_test_task_gorokhov.dto.RegisterRequest;
import org.example.url_shorter_test_task_gorokhov.dto.SuccessAuthResponse;
import org.example.url_shorter_test_task_gorokhov.jwt.JwtResponse;
import org.example.url_shorter_test_task_gorokhov.jwt.JwtUtils;
import org.example.url_shorter_test_task_gorokhov.repository.UserEntity;
import org.example.url_shorter_test_task_gorokhov.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/clk/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Регистрация нового пользователя. Если username занят, вернём 400 Bad Request.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для регистрации",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Пример запроса",
                                    summary = "Простой пример",
                                    value = """
                    {
                    
                       "username": "user",
                       "password": "MysecurityPassword"
                   
                    }
                    """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Пользователь зарегистрирован успешно",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Успешный ответ",
                                            summary = "Пользователь зарегистрирован успешно",
                                            value = """
                                                   {
                                                    "message": "Successfully registered!"
                                                   }
                                                           """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Некорректный запрос\n"
                                    + "Невалидные данные регистрации",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "Ошибка валидации (пример 1)",
                                                    summary = "Пустое имя пользователя",
                                                    value = """
                    {
                      "violations": [
                        {
                          "fieldName": "username",
                          "message": "Username cannot be blank"
                        }
                      ]
                    }
                    """
                                            ),
                                            @ExampleObject(
                                                    name = "Ошибка валидации (пример 2)",
                                                    summary = "Короткий пароль",
                                                    value = """
                    {
                      "violations": [
                        {
                          "fieldName": "password",
                          "message": "Password should be between 6 and 100 characters"
                        }
                      ]
                    }
                    """
                                            ),
                                            @ExampleObject(
                                                    name = "Ошибка валидации (пример 3)",
                                                    summary = "Занятый логин",
                                                    value = """
                                                   {
                                                    "error": "Register error",
                                                    "message": "Username already exist"
                                                   }
                                                           """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Ошибка при регистрации",
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
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("Register error", "Username already exist"));
        }
        UserEntity user = UserEntity.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .build();
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessAuthResponse("Successfully registered!"));
    }

    /**
     * Логин: принимает username/password, пытается аутентифицировать, и выдаёт JWT.
     * В случае неверных данных – 401 Unauthorized.
     */
    @Operation(
            summary = "Логин пользователя для получения JWT",
            description = "Логин пользователя. В случае неверных данных – 401 Unauthorized",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для регистрации",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Пример запроса",
                                    summary = "Простой пример",
                                    value = """
                    {
                    
                       "username": "user",
                       "password": "MysecurityPassword"
                   
                    }
                    """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Пользователь авторизован успешно",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Успешный ответ",
                                            summary = "Пользователь зарегистрирован успешно",
                                            value = """
                                                    {
                                                      "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhYm9iYSIsImlhdCI6MTc0ODE3Nzk3MSwiZXhwIjoxNzQ4MTgxNTcxfQ.bAq1i264NVTYVqSeFWPw94wUfRvA0N7GgmLSMswgI1k",
                                                      "type": "Bearer"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Некорректный запрос\n"
                                    + "Невалидные данные регистрации",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "Ошибка валидации",
                                                    summary = "Пустое имя пользователя",
                                                    value = """
                    {
                      "violations": [
                        {
                          "fieldName": "username",
                          "message": "Username cannot be blank"
                        }
                      ]
                    }
                    """
                                            ),
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Некорректный запрос\n"
                                    + "Неверный логин или пароль",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "Неверный логин или пароль",
                                                    summary = "Неверный логин или пароль",
                                                    value = """
                                                   {
                                                    "error": "Auth error",
                                                    "message": "Invalid username or password"
                                                   }
                                                           """
                                            ),
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Ошибка при регистрации",
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
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            return ResponseEntity
                    .status(401)
                    .body(new ErrorResponse("Auth error", "Invalid username or password"));
        }
        String jwt = jwtUtils.generateJwtToken(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        return ResponseEntity.ok(new JwtResponse(jwt, "Bearer"));
    }
}
