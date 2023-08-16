package test.bbackjk.http.core.reflector;

import java.util.List;

class ParameterArgumentHandlerFactory {

    private ParameterArgumentHandlerFactory() {
        throw new RuntimeException();
    }

    public static ParameterArgumentHandler getHandler(RequestParamMetadata metadata, boolean isOnlyRequestParam, boolean isEmptyAllAnnotation, List<String> pathValueNames) {
        if (metadata.isAnnotationRequestHeader()) {
            return new HeaderValueArgumentHandler(metadata);
        } else if (metadata.isAnnotationPathVariable()) {
            return new PathValueArgumentHandler(metadata);
        } else if (metadata.canRequestParam(isOnlyRequestParam, isEmptyAllAnnotation, pathValueNames)) {
            return new QueryValueArgumentHandler(metadata);
        } else if (metadata.isRestCallback()) {
            return new RestCallbackArgumentHandler();
        } else {
            return new BodyDataArgumentHandler();
        }
    }
}
