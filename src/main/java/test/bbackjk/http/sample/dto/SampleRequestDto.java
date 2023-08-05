package test.bbackjk.http.sample.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class SampleRequestDto {
    String key1;
    String key2;
    String key3;
}
