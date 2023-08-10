package test.bbackjk.http.sample.http;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import test.bbackjk.http.core.annotations.RestClient;
import test.bbackjk.http.sample.dto.CorpBasicAffiliate;

@RestClient(value = "CorpBasic", url = "https://apis.data.go.kr/1160100/service/GetCorpBasicInfoService_V2")
public interface CorpBasicInfoClient {

    @GetMapping("/getAffiliate_V2")
    String getAffiliate(@RequestParam CorpBasicAffiliate param);
//    String getConsSubsComp();
//    String getCorpOutline();
}
