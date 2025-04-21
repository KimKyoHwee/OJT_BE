package com.kyohwee.ojt.domain.dto;

import lombok.Getter;
import lombok.Setter;

//유저등록, 로그인 결과 반환
@Getter
@Setter
public class UserRequestAndResponse {
    private String username;
    private String password;
    private Long id;
}
