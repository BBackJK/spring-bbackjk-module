package test.bbackjk.http.sample.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CorpBasicGenericResponse<T> {

    private CorpResponse response;

    @Getter
    public class CorpResponse {
        private T body;
        private CorpHeader header;

        @Getter
        public class CorpHeader {
            private String resultCode;
            private String resultMsg;
        }
    }
}
