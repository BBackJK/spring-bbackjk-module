package test.bbackjk.http.spring;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.MimeType;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

// TODO : 설정 적용
@Configuration
public class SpringWebJacksonConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.removeIf(converter -> supportsXml(converter) || hasXmlMapper(converter));
    }

    private boolean supportsXml(HttpMessageConverter<?> converter) {
        return converter.getSupportedMediaTypes().stream()
                .map(MimeType::getSubtype)
                .anyMatch(subType -> subType.equalsIgnoreCase("xml"));
    }

    private boolean hasXmlMapper(HttpMessageConverter<?> converter) {
        return converter instanceof MappingJackson2HttpMessageConverter
                && ((MappingJackson2HttpMessageConverter)converter).getObjectMapper().getClass().equals(XmlMapper.class);
    }

}
