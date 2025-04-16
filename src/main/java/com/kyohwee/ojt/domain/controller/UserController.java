package com.kyohwee.ojt.domain.controller;

import com.kyohwee.ojt.domain.dto.UserRequestAndResponse;
import com.kyohwee.ojt.domain.service.UserService;
import com.kyohwee.ojt.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.kyohwee.ojt.global.uri.RequestUri.BATCH_JOB_URI;
import static com.kyohwee.ojt.global.uri.RequestUri.USER_URI;

@RestController
@RequestMapping(USER_URI)
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "사용자 등록", description = "새로운 사용자를 등록합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "등록된 사용자 정보를 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 형식이 잘못되었습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "중복된 유저가 존재합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 에러")
    })
    public ResponseEntity<ApiResponse<UserRequestAndResponse>> registerUser(@RequestBody UserRequestAndResponse request) {
        UserRequestAndResponse userResponse = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.onSuccessCREATED(userResponse));
    }

    @PostMapping("/login")
    @Operation(summary = "사용자 로그인", description = "사용자 로그인 처리")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "아이디 또는 비밀번호가 잘못되었습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 에러")
    })
    public ResponseEntity<ApiResponse<UserRequestAndResponse>> loginUser(@RequestBody UserRequestAndResponse request) {
        UserRequestAndResponse userResponse = userService.loginUser(request);
        return ResponseEntity.ok(
                ApiResponse.onSuccess(HttpStatus.OK, "로그인 성공", userResponse)
        );
    }
}

