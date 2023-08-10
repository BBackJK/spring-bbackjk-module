package test.bbackjk.http.sample.http;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import test.bbackjk.http.core.annotations.RestClient;
import test.bbackjk.http.core.bean.mapper.DefaultResponseMapper;
import test.bbackjk.http.core.interfaces.ResponseMapper;
import test.bbackjk.http.sample.dto.CorpAffiliateRequest;
import test.bbackjk.http.sample.dto.CorpAffiliateResponse;
import test.bbackjk.http.sample.dto.CorpBasicGenericResponse;

@RestClient(value = "CorpBasic", url = "https://apis.data.go.kr/1160100/service/GetCorpBasicInfoService_V2")
public interface CorpBasicInfoClient {

    @GetMapping("/getAffiliate_V2")
    CorpBasicGenericResponse<CorpAffiliateResponse> getAffiliate(@RequestParam CorpAffiliateRequest param);

    String getConsSubsComp();
//    String getCorpOutline();

    public static void main(String[] args) {
        String value = "{\"response\":{\"body\":{\"items\":{\"item\":[{\"basDt\":\"20200509\",\"crno\":\"1101110000086\",\"afilCmpyNm\":\"롯데건설(주)\",\"afilCmpyCrno\":\"1101110014764\",\"lstgYn\":\"\"},{\"basDt\":\"20200509\",\"crno\":\"1101110000086\",\"afilCmpyNm\":\"(주)롯데푸드\",\"afilCmpyCrno\":\"1101110033722\",\"lstgYn\":\"\"},{\"basDt\":\"20200509\",\"crno\":\"1101110000086\",\"afilCmpyNm\":\"롯데지주(주)\",\"afilCmpyCrno\":\"1101110076300\",\"lstgYn\":\"\"},{\"basDt\":\"20200509\",\"crno\":\"1101110000086\",\"afilCmpyNm\":\"호텔롯데(주)\",\"afilCmpyCrno\":\"1101110145410\",\"lstgYn\":\"\"},{\"basDt\":\"20200509\",\"crno\":\"1101110000086\",\"afilCmpyNm\":\"롯데상사(주)\",\"afilCmpyCrno\":\"1101110159099\",\"lstgYn\":\"\"},{\"basDt\":\"20200509\",\"crno\":\"1101110000086\",\"afilCmpyNm\":\"롯데케미칼(주)\",\"afilCmpyCrno\":\"1101110193196\",\"lstgYn\":\"\"},{\"basDt\":\"20200509\",\"crno\":\"1101110000086\",\"afilCmpyNm\":\"롯데하이마트(주)\",\"afilCmpyCrno\":\"1101110532633\",\"lstgYn\":\"\"},{\"basDt\":\"20200509\",\"crno\":\"1101110000086\",\"afilCmpyNm\":\"롯데캐피탈(주)\",\"afilCmpyCrno\":\"1101111217416\",\"lstgYn\":\"\"},{\"basDt\":\"20200509\",\"crno\":\"1101110000086\",\"afilCmpyNm\":\"롯데렌탈(주)\",\"afilCmpyCrno\":\"1101113326588\",\"lstgYn\":\"\"},{\"basDt\":\"20200509\",\"crno\":\"1101110000086\",\"afilCmpyNm\":\"서울복합물류프로젝트금융투자(주)\",\"afilCmpyCrno\":\"1101114660232\",\"lstgYn\":\"\"}]},\"numOfRows\":10,\"pageNo\":1,\"totalCount\":13563448},\"header\":{\"resultCode\":\"00\",\"resultMsg\":\"NORMAL SERVICE.\"}}}";
        ResponseMapper mapper = new DefaultResponseMapper();
        CorpBasicGenericResponse<CorpAffiliateResponse> result = mapper.convert(value, CorpBasicGenericResponse.class);
        System.out.println(result);

        if ( result != null ) {
            CorpBasicGenericResponse.CorpResponse genericResponse = result.getResponse();
            if ( genericResponse.getBody() != null ) {
                System.out.println("genericResponse.getBody() :: " + genericResponse.getBody());
            }
        }
    }
}
