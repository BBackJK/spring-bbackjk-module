package test.bbackjk.http.sample.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@XmlRootElement(name = "response")
public class CorpAffiliateXmlResponse {

    @XmlElement(name = "body")
    private Body body;
    @XmlElement(name = "header")
    private CorpHeader header;

    @Getter
    public static class CorpHeader {
        @XmlElement(name = "resultCode")
        private String resultCode;
        @XmlElement(name = "resultMsg")
        private String resultMsg;
    }

    @Getter
    @NoArgsConstructor
    @ToString
    public static class Body {
        @XmlElement(name = "items")
        private ListResponse items;
        @XmlElement(name = "numOfRows")
        private int numOfRows;
        @XmlElement(name = "pageNo")
        private int pageNo;
        @XmlElement(name = "totalCount")
        private int totalCount;
    }

    @Getter
    @NoArgsConstructor
    @ToString
    public static class ListResponse {
        @XmlElement(name = "item")
        private List<Response> item;
    }

    @Getter
    @NoArgsConstructor
    @ToString
    public static class Response {
        @XmlElement(name = "basDt")
        private String basDt;
        @XmlElement(name = "crno")
        private String crno;
        @XmlElement(name = "afilCmpyNm")
        private String afilCmpyNm;
        @XmlElement(name = "afilCmpyCrno")
        private String afilCmpyCrno;
        @XmlElement(name = "lstgYn")
        private String lstgYn;
    }
}
