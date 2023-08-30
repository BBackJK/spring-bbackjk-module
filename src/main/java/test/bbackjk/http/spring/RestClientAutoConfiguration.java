package test.bbackjk.http.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(AutoConfiguredRestClientScannerRegistrar.class)
@ConditionalOnProperty(value = "bback.http.enable", havingValue = "true")
public class RestClientAutoConfiguration { }
