package test.bbackjk.http.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import test.bbackjk.http.interfaces.HttpAgent;

@Configuration
public class DefaultHttpAgentConfiguration {

    @Bean
    public HttpAgent getHttpAgent(HttpAgent okHttpAgent) {
        return okHttpAgent;
    }

    @Bean
    public HttpAgent getHttpAgent() {
        return null;
    }
}
