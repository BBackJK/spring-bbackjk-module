package test.bbackjk.http.sample.http;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import test.bbackjk.http.core.annotations.RestClient;
import test.bbackjk.http.sample.dto.CorpAffiliateRequest;
import test.bbackjk.http.sample.dto.CorpAffiliateResponse;
import test.bbackjk.http.sample.dto.CorpAffiliateXmlResponse;

@RestClient(value = "CorpBasic", url = "https://apis.data.go.kr/1160100/service/GetCorpBasicInfoService_V2")
public interface CorpBasicInfoClient {

    @GetMapping("/getAffiliate_V2")
    CorpAffiliateResponse getAffiliate(@RequestParam CorpAffiliateRequest param);

    @GetMapping(value = "/getAffiliate_V2", produces = MediaType.APPLICATION_XML_VALUE)
    CorpAffiliateXmlResponse getAffiliateXml(@RequestParam CorpAffiliateRequest param);

    String getConsSubsComp();
//    String getCorpOutline();
}
