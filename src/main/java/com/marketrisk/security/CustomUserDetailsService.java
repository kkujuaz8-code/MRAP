package com.marketrisk.security;

import com.marketrisk.entity.user.User;             
import com.marketrisk.repository.user.UserRepository; 
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        // 💡 findByEmail 대신 findByLoginId 사용
        User user = userRepository.findByLoginId(loginId)
            .orElseThrow(() -> new UsernameNotFoundException("해당 아이디를 찾을 수 없습니다: " + loginId));
        
        return new CustomUserDetails(user);
    }
}