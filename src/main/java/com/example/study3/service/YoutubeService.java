package com.example.study3.service;

import com.example.study3.domain.BoardEntity;
import com.example.study3.domain.Member;
import com.example.study3.domain.MusicEntity;
import com.example.study3.repository.BoardRepository;
import com.example.study3.repository.MemberRepository;
import com.example.study3.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class YoutubeService {

    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final YoutubeUploadService youtubeUploadService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${google.client.secret}")
    private String CLIENT_SECRET;

    private static final String CLIENT_ID = "1067642253282-b0oi07bo4l4bjhdqtkrndmdr31ekj5cc.apps.googleusercontent.com";
    private static final String REDIRECT_URI = "https://chw-frontend.vercel.app/api/youtube/oauth2/callback";

    public String generateGoogleAuthUrl(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "토큰이 없습니다");
        }

        String loginId = jwtTokenProvider.getLoginId(token);
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저 없음"));

        String stateJwt = jwtTokenProvider.createAccessToken(member.getLoginId());

        return UriComponentsBuilder.fromHttpUrl("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", REDIRECT_URI)
                .queryParam("response_type", "code")
                .queryParam("scope", "https://www.googleapis.com/auth/youtube.upload profile email")
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("state", stateJwt)
                .build()
                .toUriString();
    }

    public void handleCallback(String code, String state) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", CLIENT_ID);
        params.add("client_secret", CLIENT_SECRET);
        params.add("redirect_uri", REDIRECT_URI);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> tokenResponse = new RestTemplate().postForEntity(
                "https://oauth2.googleapis.com/token",
                request,
                Map.class
        );

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        Claims claims = jwtTokenProvider.parseClaims(state);
        String loginId = claims.getSubject();

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        member.setYoutubeAccessToken(accessToken);
        memberRepository.save(member);
    }

    public String uploadUserBoardToYoutube(Long id, Authentication authentication) throws Exception {
        Member member = memberRepository.findByLoginId(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 필요"));

        BoardEntity board = boardRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글 없음"));

        MusicEntity music = board.getMusic();
        if (music == null || music.getAudioUrl() == null || music.getImageUrl() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "음악 정보가 없습니다");
        }

        String accessToken = member.getYoutubeAccessToken();

        String youtubeUrl = youtubeUploadService.uploadToYoutube(
                accessToken,
                music.getImageUrl(),
                music.getAudioUrl(),
                board.getTitle(),
                board.getContent() != null ? board.getContent() : ""
        );

        return youtubeUrl;
    }
}
