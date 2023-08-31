package test.bbackjk.http.sample.http;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import test.bbackjk.http.core.annotations.RestClient;
import test.bbackjk.http.core.bean.agent.UnirestHttpAgent;
import test.bbackjk.http.sample.dto.KakaoTokenResponseModel;
import test.bbackjk.http.sample.dto.KakaoUserInfoResponseModel;

import java.util.Map;

@RestClient(agent = UnirestHttpAgent.class)
public interface KakaoClient {

    @PostMapping(
            value = "https://kauth.kakao.com/oauth/token"
            , consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    KakaoTokenResponseModel getToken(Map<String, String> requestValues);


    @GetMapping(value = "https://kapi.kakao.com/v2/user/me")
    KakaoUserInfoResponseModel getUserInfo(@RequestHeader("Authorization") String bearerToken);
}
