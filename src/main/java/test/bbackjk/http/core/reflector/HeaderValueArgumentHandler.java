package test.bbackjk.http.core.reflector;

import java.util.Optional;

class HeaderValueArgumentHandler implements ParameterArgumentHandler {

    private final RequestParamMetadata metadata;

    public HeaderValueArgumentHandler(RequestParamMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public void handle(ArgumentPresetMetadata<?> preset, Optional<Object> arg) {
        arg.ifPresent(o -> preset.set(this.metadata.getParamName(), String.valueOf(o)));
    }
}
