package test.bbackjk.http.sample.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import test.bbackjk.http.sample.dto.CorpBasicAffiliate;
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
        String result = client.getAffiliate(CorpBasicAffiliate.of(serviceKey, "json"));
    }
}
