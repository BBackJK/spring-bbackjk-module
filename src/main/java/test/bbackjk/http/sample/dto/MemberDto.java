package test.bbackjk.http.sample.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor(staticName = "of")
@Data
@NoArgsConstructor
public class MemberDto {

//    @JacksonXmlProperty(localName = "id")
    private int id;
//    @JacksonXmlProperty(localName = "name")
    private String name;
}
