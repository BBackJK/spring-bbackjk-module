package test.bbackjk.http.core.interfaces;

import test.bbackjk.http.core.wrapper.RequestMetadata;
import test.bbackjk.http.core.exceptions.RestClientCallException;
import test.bbackjk.http.core.wrapper.RestCommonResponse;

public interface HttpAgent {

    RestCommonResponse doGet(RequestMetadata requestMetadata) throws RestClientCallException;
    RestCommonResponse doPost(RequestMetadata requestMetadata) throws RestClientCallException;
    RestCommonResponse doPatch(RequestMetadata requestMetadata) throws RestClientCallException;
    RestCommonResponse doPut(RequestMetadata requestMetadata) throws RestClientCallException;
    RestCommonResponse doDelete(RequestMetadata requestMetadata) throws RestClientCallException;

}
