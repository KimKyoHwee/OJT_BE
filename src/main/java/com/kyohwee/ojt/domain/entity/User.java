package com.kyohwee.ojt.domain.entity;

import com.kyohwee.ojt.domain.dto.UserRequestAndResponse;
import com.kyohwee.ojt.domain.repository.UserRepository;
import com.kyohwee.ojt.global.exception.GlobalErrorCode;
import com.kyohwee.ojt.global.exception.GlobalException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 정보를 저장하는 엔티티
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본 키

    @Column(nullable = false, unique = true, length = 255)
    private String username; // 사용자 이름 (고유)

    @Column(nullable = false, length = 255)
    private String password; // 사용자 비밀번호

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 생성 시간

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now(); // 수정 시간

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now(); // 수정 시 updatedAt 갱신
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BatchJob> batchJobs;

    public static User fromDto(UserRequestAndResponse dto) {
        return User.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .build();
    }

    public static User findUser(UserRepository userRepository, Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));
    }
}