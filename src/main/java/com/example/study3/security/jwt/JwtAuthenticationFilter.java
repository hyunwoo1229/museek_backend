package com.example.study3.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException{
        String token = resolveToken(request);


        if (token != null) { // 헤더에 토큰이 존재하는 경우
            if (jwtTokenProvider.validateToken(token)) {
                // 토큰이 유효하면 인증 정보 설정
                String loginId = jwtTokenProvider.getLoginId(token);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(loginId, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                // 토큰이 유효하지 않은 경우 (만료 등)
                // 401 에러를 응답하고 필터 체인을 여기서 중단
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid/Expired Token");
                return;
            }
        }

        // 토큰이 아예 없는 경우는 그대로 필터 체인 계속 진행 (공개 API 접근 허용)
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if(bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
