package test.bbackjk.http.sample.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CorpAffiliateResponse {

    private CorpResponse response;

    @Getter
    @ToString
    public class CorpResponse {
        private Body body;
        private CorpHeader header;

        @Getter
        public class CorpHeader {
            private String resultCode;
            private String resultMsg;
        }
    }

    @Getter
    @NoArgsConstructor
    @ToString
    public static class Body {
        private ListResponse items;
        private int numOfRows;
        private int pageNo;
        private int totalCount;
    }

    @Getter
    @NoArgsConstructor
    @ToString
    public static class ListResponse {
        private List<Response> item;
    }

    @Getter
    @NoArgsConstructor
    @ToString
    public static class Response {
        private String basDt;
        private String crno;
        private String afilCmpyNm;
        private String afilCmpyCrno;
        private String lstgYn;
    }
}
