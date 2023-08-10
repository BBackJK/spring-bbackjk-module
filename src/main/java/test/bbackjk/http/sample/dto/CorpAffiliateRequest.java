package test.bbackjk.http.sample.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CorpAffiliateRequest {
    private final String serviceKey;
    private final String pageNo;
    private final String numOfRows;
    private final String resultType;

    public static CorpAffiliateRequest of(String serviceKey, String resultType) {
        return new CorpAffiliateRequest(serviceKey, "1", "10", resultType);
    }
}
