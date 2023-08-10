package test.bbackjk.http.sample.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CorpAffiliateResponse {

    private ListResponse items;
    private int numOfRows;
    private int pageNo;
    private int totalCount;

    @Getter
    @NoArgsConstructor
    public class ListResponse {
        private List<Response> item;
    }

    @Getter
    @NoArgsConstructor
    public class Response {
        private String basDt;
        private String crno;
        private String afilCmpyNm;
        private String afilCmpyCrno;
        private String lstgYn;
    }
}
