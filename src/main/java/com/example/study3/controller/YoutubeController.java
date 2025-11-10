package com.example.study3.controller;

import com.example.study3.dto.ErrorResponse;
import com.example.study3.dto.SuccessResponse;
import com.example.study3.service.YoutubeService;
import jakarta.servlet.http.HttpServletRequest; // HttpServletRequest 임포트
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/youtube")
@RequiredArgsConstructor
public class YoutubeController {

    private final YoutubeService youtubeService;


    //프론트엔드에 Google OAuth 인증 URL을 생성하여 반환하는 엔드포인트
    @GetMapping("/auth-url")
    public ResponseEntity<?> generateGoogleAuthUrl(HttpServletRequest request) {
        try {
            // "Bearer " 접두사 제거 후 토큰 추출
            String token = request.getHeader("Authorization").substring(7);
            String authUrl = youtubeService.generateGoogleAuthUrl(token);
            // URL 문자열을 직접 응답 바디에 담아 반환
            return ResponseEntity.ok(authUrl);
        } catch (Exception e) {
            // 예외 발생 시 에러 메시지를 담은 응답 반환
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("인증 URL 생성에 실패했습니다: " + e.getMessage()));
        }
    }

    //구글 로그인 성공 후 리디렉션될 콜백 엔드포인트
    @GetMapping("/oauth2/callback")
    public void handleYoutubeCallback(@RequestParam String code, @RequestParam String state, HttpServletResponse response) throws IOException {
        try {
            youtubeService.handleCallback(code, state);
            // 콜백 처리가 끝나면, 실제 업로드를 시작할 프론트엔드 페이지로 리디렉션
            response.sendRedirect("https://chw-frontend.vercel.app//upload-finish"); // 사용하는 프론트엔드 주소로 변경
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("https://chw-frontend.vercel.app//error?msg=" +
                    URLEncoder.encode("유튜브 연동 중 오류가 발생했습니다.", StandardCharsets.UTF_8));
        }
    }

    //최종적으로 비디오를 업로드하는 엔드포인트
    @PostMapping("/{id}")
    public ResponseEntity<?> uploadToYoutube(@PathVariable Long id, Authentication authentication) {
        try {
            String youtubeUrl = youtubeService.uploadUserBoardToYoutube(id, authentication);
            return ResponseEntity.ok(new SuccessResponse(youtubeUrl));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("유튜브 업로드 실패: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("업로드 중 알 수 없는 오류가 발생했습니다."));
        }
    }
}