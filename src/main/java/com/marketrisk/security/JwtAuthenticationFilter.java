package com.marketrisk.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    // 💡 사용자가 요청을 보낼 때마다 무조건 여기를 거쳐갑니다!
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 1. 사용자가 가져온 요청 헤더에서 통행증(Token)만 쏙 빼냅니다.
        String token = resolveToken(request);

        // 2. 통행증이 있고, 그게 위조되지 않은 진짜라면?
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 통행증에서 정보를 읽어와서 스프링 시큐리티 시스템(SecurityContext)에 "이 사람 인증됨!" 하고 등록합니다.
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // 3. 통과! 다음 단계로 가세요~
        filterChain.doFilter(request, response);
    }

    // 통행증 껍데기("Bearer ")를 벗기고 알맹이만 가져오는 헬퍼 메서드
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 뒷부분(알맹이)만 자릅니다.
        }
        return null;
    }
}