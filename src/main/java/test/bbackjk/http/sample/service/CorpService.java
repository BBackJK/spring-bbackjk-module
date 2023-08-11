package test.bbackjk.http.sample.service;

import test.bbackjk.http.sample.dto.CorpAffiliateResponse;
import test.bbackjk.http.sample.dto.CorpAffiliateXmlResponse;

public interface CorpService {

    CorpAffiliateResponse getAffiliateJson();

    CorpAffiliateXmlResponse getAffiliateXml();
}
