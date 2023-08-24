package test.bbackjk.http.sample.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import test.bbackjk.http.core.util.ObjectUtils;
import test.bbackjk.http.sample.dto.KakaoTokenResponseModel;
import test.bbackjk.http.sample.dto.KakaoUserInfoResponseModel;
import test.bbackjk.http.sample.http.KakaoClient;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class SampleKakaoController {

    private final KakaoClient kakaoClient;
    private static final String BASE_APP_URL = "http://localhost:8080";
    private static final String APP_KEY = "###################################"; // TODO: REST API KEY 를 입력하세요.

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/kakao/oauth")
    public String kakaoOauth() {
        String oauthUrl = "https://kauth.kakao.com/oauth/authorize";
        oauthUrl += "?response_type=code";
        oauthUrl += "&client_id=" + APP_KEY;
        oauthUrl += "&redirect_uri=" + BASE_APP_URL + "/kakao/oauth/callback";
        oauthUrl += "&prompt=login";    // 기존 세션 로그인 여부와 상관없이 로그인
        return "redirect:" + oauthUrl;
    }

    @GetMapping("/kakao/oauth/callback")
    public String kakaoCallback(@RequestParam(value = "code", required = false) String kakaoOauthCode, @RequestParam(value = "error", required = false) String kakaoOauthError) {
        String redirectHome = "redirect:/";
        if (ObjectUtils.isEmpty(kakaoOauthCode)) {
            log.error(" 로그인에 실패하였습니다. ");
            return redirectHome;
        }
        if (ObjectUtils.isNotEmpty(kakaoOauthError)) {
            log.error(" 로그인을 취소하셨습니다. ");
            return redirectHome;
        }

        Map<String, String> requestValues = new HashMap<>();
        requestValues.put("grant_type", "authorization_code");
        requestValues.put("client_id", APP_KEY);
        requestValues.put("redirect_uri", BASE_APP_URL + "/kakao/oauth/callback");
        requestValues.put("code", kakaoOauthCode);
        KakaoTokenResponseModel tokenResponse = kakaoClient.getToken(requestValues);
        String bearerToken = tokenResponse.getAccessToken();

        KakaoUserInfoResponseModel userInfoResponse = kakaoClient.getUserInfo("Bearer " + bearerToken);
        log.info("userInfoResponse :: {}", userInfoResponse);

        return redirectHome;
    }
}
