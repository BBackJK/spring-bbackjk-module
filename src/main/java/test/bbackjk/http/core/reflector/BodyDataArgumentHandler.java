package test.bbackjk.http.core.reflector;

import lombok.NoArgsConstructor;

import java.util.Optional;

@NoArgsConstructor
class BodyDataArgumentHandler implements ParameterArgumentHandler {

    @Override
    public void handle(ArgumentPresetMetadata<?> preset, Optional<Object> arg) {
        preset.set("", null);   // 초기화
        arg.ifPresent(o -> preset.set("", o));
    }
}
