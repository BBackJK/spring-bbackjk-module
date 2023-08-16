package test.bbackjk.http.core.interfaces;

import test.bbackjk.http.core.wrapper.RequestMetadata;
import test.bbackjk.http.core.exceptions.RestClientCallException;
import test.bbackjk.http.core.wrapper.ResponseMetadata;

public interface HttpAgent {

    ResponseMetadata doGet(RequestMetadata requestMetadata) throws RestClientCallException;
    ResponseMetadata doPost(RequestMetadata requestMetadata) throws RestClientCallException;
    ResponseMetadata doPatch(RequestMetadata requestMetadata) throws RestClientCallException;
    ResponseMetadata doPut(RequestMetadata requestMetadata) throws RestClientCallException;
    ResponseMetadata doDelete(RequestMetadata requestMetadata) throws RestClientCallException;

}
