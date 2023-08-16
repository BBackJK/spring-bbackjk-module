package test.bbackjk.http.core.reflector;

import lombok.NoArgsConstructor;

import java.util.Optional;

@NoArgsConstructor
class RestCallbackArgumentHandler implements ParameterArgumentHandler {

    @Override
    public void handle(ArgumentPresetMetadata<?> preset, Optional<Object> arg) {
        // ignore
    }
}
