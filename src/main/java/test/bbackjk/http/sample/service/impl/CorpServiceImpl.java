package test.bbackjk.http.sample.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import test.bbackjk.http.sample.dto.CorpAffiliateRequest;
import test.bbackjk.http.sample.dto.CorpAffiliateResponse;
import test.bbackjk.http.sample.dto.CorpBasicGenericResponse;
import test.bbackjk.http.sample.http.CorpBasicInfoClient;
import test.bbackjk.http.sample.service.CorpService;

@Service
@RequiredArgsConstructor
@Slf4j
public class CorpServiceImpl implements CorpService {

    private final CorpBasicInfoClient client;
    private final String serviceKey = "lStmXFIDJOmtwq5Z2pWDmw8r931C0r15RLXsZGgLv6Dj521NFC4rRVP+0ezLXmHDb8ET/EIDjqrt4VDtbE7guw==";
    @Override
    public void getAffiliate() {
        CorpBasicGenericResponse<CorpAffiliateResponse> result = client.getAffiliate(CorpAffiliateRequest.of(serviceKey, "json"));

        if ( result != null ) {
            CorpBasicGenericResponse.CorpResponse genericResponse = result.getResponse();
            if ( genericResponse.getBody() != null ) {
                log.info("genericResponse.getBody() :: {}", genericResponse.getBody());
            }
        }
    }
}
