package com.marketrisk.common; // 패키지명은 회원님 프로젝트에 맞게 수정해주세요!

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// 💡 프로젝트 전체의 에러를 감지해서 예쁘게 포장해주는 안내원!
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String, String> errorResponse = new HashMap<>();
        
        // 에러 상태와 우리가 서비스에서 적어둔 메시지를 예쁘게 담습니다.
        errorResponse.put("status", "ERROR");
        errorResponse.put("message", e.getMessage());

        // 400 Bad Request (니가 요청을 잘못했어!) 상태코드로 반환
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}