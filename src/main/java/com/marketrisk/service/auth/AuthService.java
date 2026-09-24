package com.marketrisk.service.auth;

import com.marketrisk.dto.auth.LoginRequest;
import com.marketrisk.dto.auth.SignupRequest;
import com.marketrisk.dto.auth.TokenResponse;
import com.marketrisk.entity.user.User;
import com.marketrisk.repository.user.UserRepository;
import com.marketrisk.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public boolean checkLoginIdDuplicate(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }

    @Transactional
    public String signup(SignupRequest request) {
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        String userName = (request.getName() != null && !request.getName().trim().isEmpty()) 
                            ? request.getName() 
                            : request.getLoginId();

        User newUser = User.builder()
                .loginId(request.getLoginId())
                .password(encodedPassword)
                .name(userName)
                .role(User.Role.ROLE_USER)
                .build();

        userRepository.save(newUser);
        return "회원가입이 완료되었습니다.";
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 아이디입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        String token = jwtTokenProvider.createToken(user.getLoginId(), user.getRole().name());
        return new TokenResponse(token);
    }

    // 💡 [회원 탈퇴 기능 추가]
    @Transactional
    public void deleteAccount(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        userRepository.delete(user);
    }
}