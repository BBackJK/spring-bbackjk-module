package test.bbackjk.http.core.bean.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.stereotype.Component;
import test.bbackjk.http.core.exceptions.RestClientDataMappingException;
import test.bbackjk.http.core.interfaces.ResponseMapper;

import java.io.StringReader;
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
            if (!clazz.isInterface()) {
                this.canConvert(clazz);
            }
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
            if (!clazz.isInterface()) {
                this.canConvert(value.getClass());
                this.canConvert(clazz);
            }
            return this.om.convertValue(value, new TypeReference<>() {});
        } catch (IllegalArgumentException e) {
            throw new RestClientDataMappingException(e);
        }
    }

    @Override
    public <T> T toXml(String value, Class<T> clazz) throws RestClientDataMappingException {
        try {
            if (!clazz.isInterface()) {
                this.canConvert(clazz);
            }
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (T) unmarshaller.unmarshal(new StringReader(value));
        } catch (JAXBException e) {
            throw new RestClientDataMappingException(e);
        }
    }
}
