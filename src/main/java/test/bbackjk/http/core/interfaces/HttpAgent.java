package test.bbackjk.http.core.interfaces;

import test.bbackjk.http.core.wrapper.RequestMetadata;
import test.bbackjk.http.core.exceptions.RestClientCallException;
import test.bbackjk.http.core.wrapper.HttpAgentResponse;

public interface HttpAgent {

    HttpAgentResponse doGet(RequestMetadata requestMetadata) throws RestClientCallException;
    HttpAgentResponse doPost(RequestMetadata requestMetadata) throws RestClientCallException;
    HttpAgentResponse doPatch(RequestMetadata requestMetadata) throws RestClientCallException;
    HttpAgentResponse doPut(RequestMetadata requestMetadata) throws RestClientCallException;
    HttpAgentResponse doDelete(RequestMetadata requestMetadata) throws RestClientCallException;

}
