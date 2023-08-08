package test.bbackjk.http.interfaces;

import test.bbackjk.http.exceptions.RestClientDataMappingException;

import java.util.List;

public interface ResponseMapper {

    <T> T convert(String value, Class<T> clazz) throws RestClientDataMappingException;
    <T> List<T> converts(String value, Class<T> clazz) throws RestClientDataMappingException;
    <T, E> E convert(T value, Class<E> clazz) throws RestClientDataMappingException;
    <T> T toXml(String value, Class<T> clazz) throws RestClientDataMappingException;

    default <T> void canConvert(Class<T> clazz) throws RestClientDataMappingException {
        try {
            clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RestClientDataMappingException(String.format("%s 클래스의 기본 생성자는 필수입니다.", clazz.getSimpleName()));
        }
    }
}
