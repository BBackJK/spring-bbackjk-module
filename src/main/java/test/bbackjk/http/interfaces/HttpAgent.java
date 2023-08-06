package test.bbackjk.http.interfaces;

import test.bbackjk.http.exceptions.RestClientCallException;
import test.bbackjk.http.wrapper.RequestMetadata;
import test.bbackjk.http.wrapper.RestCommonResponse;

public interface HttpAgent {

    RestCommonResponse doGet(RequestMetadata requestMetadata) throws RestClientCallException;
    RestCommonResponse doPost(RequestMetadata requestMetadata) throws RestClientCallException;
    RestCommonResponse doPatch(RequestMetadata requestMetadata) throws RestClientCallException;
    RestCommonResponse doPut(RequestMetadata requestMetadata) throws RestClientCallException;
    RestCommonResponse doDelete(RequestMetadata requestMetadata) throws RestClientCallException;

}
