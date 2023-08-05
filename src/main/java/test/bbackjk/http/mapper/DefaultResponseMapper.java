package test.bbackjk.http.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import test.bbackjk.http.exceptions.RestClientDataMappingException;
import test.bbackjk.http.interfaces.ResponseMapper;

import java.util.List;

@Component
public class DefaultResponseMapper implements ResponseMapper {

    private final ObjectMapper om;

    public DefaultResponseMapper() {
        this.om = new ObjectMapper();
    }

    @Override
    public <T> T convert(String value, Class<T> clazz) throws RestClientDataMappingException {
        try {
            this.canConvert(clazz);
            return this.om.readValue(value, clazz);
        } catch (IllegalArgumentException | JsonProcessingException e) {
            throw new RestClientDataMappingException(e);
        }
    }

    @Override
    public <T> List<T> converts(String value, Class<T> clazz) throws RestClientDataMappingException {
        try {
            this.canConvert(clazz);
            return this.om.readValue(value, this.om.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IllegalArgumentException | JsonProcessingException e) {
            throw new RestClientDataMappingException(e);
        }
    }

    @Override
    public <T, E> E convert(T value, Class<E> clazz) throws RestClientDataMappingException {
        try {
            this.canConvert(clazz);
            return this.om.convertValue(value, new TypeReference<>() {});
        } catch (IllegalArgumentException e) {
            throw new RestClientDataMappingException(e);
        }
    }
}
