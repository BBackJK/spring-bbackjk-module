package test.bbackjk.http.core.reflector;

import java.util.Optional;

public interface ParameterArgumentHandler {
    void handle(ArgumentPresetMetadata<?> preset, Optional<Object> arg);
}
