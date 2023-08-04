package test.bbackjk.http.interfaces;

import test.bbackjk.http.exceptions.RestClientCallException;
import test.bbackjk.http.wrapper.RequestMetadata;
import test.bbackjk.http.wrapper.RestResponse;

public interface HttpAgent {

    RestResponse doGet(RequestMetadata requestMetadata) throws RestClientCallException;
    RestResponse doPost(RequestMetadata requestMetadata) throws RestClientCallException;
    RestResponse doPatch(RequestMetadata requestMetadata) throws RestClientCallException;
    RestResponse doPut(RequestMetadata requestMetadata) throws RestClientCallException;
    RestResponse doDelete(RequestMetadata requestMetadata) throws RestClientCallException;

}
