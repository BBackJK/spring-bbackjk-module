package test.bbackjk.http.sample.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CorpBasicAffiliate {
    private final String serviceKey;
    private final String pageNo;
    private final String numOfRows;
    private final String resultType;

    public static CorpBasicAffiliate of(String serviceKey, String resultType) {
        return new CorpBasicAffiliate(serviceKey, "1", "10", resultType);
    }
}
