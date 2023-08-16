package test.bbackjk.http.core.reflector;

import java.util.Optional;

class PathValueArgumentHandler implements ParameterArgumentHandler {

    private final RequestParamMetadata metadata;

    public PathValueArgumentHandler(RequestParamMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public void handle(ArgumentPresetMetadata<?> preset, Optional<Object> arg) {
        arg.ifPresent(o -> preset.set(this.metadata.getParamName(), String.valueOf(o)));
    }
}
