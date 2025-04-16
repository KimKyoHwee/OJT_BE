package com.kyohwee.ojt.domain.service;

import com.kyohwee.ojt.domain.dto.UserRequestAndResponse;
import com.kyohwee.ojt.domain.entity.User;
import com.kyohwee.ojt.domain.repository.UserRepository;
import com.kyohwee.ojt.global.exception.GlobalErrorCode;
import com.kyohwee.ojt.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserRequestAndResponse registerUser(UserRequestAndResponse request) {
        // 중복 사용자 확인
        userRepository.findByUsernameAndPassword(request.getUsername(), request.getPassword())
                .ifPresent(user -> {
                    throw new GlobalException(GlobalErrorCode.USER_EXIST);
                });

        // 새로운 사용자 생성
        User user = User.fromDto(request);

        // 사용자 저장
        User savedUser = userRepository.save(user);

        // 응답 객체 생성
        UserRequestAndResponse response = new UserRequestAndResponse();
        response.setId(savedUser.getId());
        response.setUsername(savedUser.getUsername());
        return response;
    }

    public UserRequestAndResponse loginUser(UserRequestAndResponse request) {
        // 사용자 인증
        User user = userRepository.findByUsernameAndPassword(request.getUsername(), request.getPassword())
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.INVALID_CREDENTIALS));

        // 응답 객체 생성
        UserRequestAndResponse response = new UserRequestAndResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        return response;
    }
}