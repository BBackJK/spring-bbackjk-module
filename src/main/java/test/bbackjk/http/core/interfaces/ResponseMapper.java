package test.bbackjk.http.core.interfaces;

import test.bbackjk.http.core.exceptions.RestClientDataMappingException;

public interface ResponseMapper {

    <T> T convert(String value, Class<T> clazz) throws RestClientDataMappingException;

    <T,E> T convert(String value, Class<T> genericClass, Class<E> rawClass) throws RestClientDataMappingException;

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
