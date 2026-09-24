package com.marketrisk.controller.auth;

import com.marketrisk.dto.auth.LoginRequest;
import com.marketrisk.dto.auth.SignupRequest;
import com.marketrisk.dto.auth.TokenResponse;
import com.marketrisk.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // [아이디 중복확인 API]
    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkId(@RequestParam(name = "loginId", required = false) String loginId) {
        if (loginId == null || loginId.trim().isEmpty()) {
            return ResponseEntity.ok(true);
        }
        
        boolean isDuplicate = authService.checkLoginIdDuplicate(loginId.trim());
        return ResponseEntity.ok(isDuplicate);
    }

    // [회원가입 API]
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            String result = authService.signup(request);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // [로그인 API]
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // 💡 [회원 탈퇴 API 추가]
    @DeleteMapping("/withdraw")
    public ResponseEntity<String> withdraw(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("인증 정보가 유효하지 않습니다.");
        }

        authService.deleteAccount(userDetails.getUsername());
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }
}