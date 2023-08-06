package test.bbackjk.http.sample.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor(staticName = "of")
@ToString
public class SampleRequestDto {
    String key1;
    String key2;
    String key3;
}
