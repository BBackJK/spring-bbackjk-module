package test.bbackjk.http.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MethodInvoker;
import test.bbackjk.http.annotations.RestClient;
import test.bbackjk.http.configuration.RestClientConnectProperties;
import test.bbackjk.http.exceptions.RestClientCommonException;
import test.bbackjk.http.interfaces.HttpAgentConfiguration;
import test.bbackjk.http.util.Logs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
public class RestClientProxy<T> implements InvocationHandler {

    private final Class<T> restClientInterface;
    private final Map<Method, MethodInvoker> cachedClient;

    public RestClientProxy(
            Class<T> restClientInterface
            , Map<Method, MethodInvoker> cachedClient
            , RestClientConnectProperties restClientConnectProperties
    ) {
        RestClient restClient = restClientInterface.getAnnotation(RestClient.class);

        this.restClientInterface = restClientInterface;
        this.cachedClient = cachedClient;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        return null;
    }

    // TODO: http client 로 사용 될 라이브러리 들은 무조건 singleton 으로 가야 된다. Registrar 에서 client 들을 끌고와서 사용할 방법들을 고민해보자.
    private HttpAgentConfiguration getHttpAgentConfiguration(RestClient restClient) {
        Class<? extends HttpAgentConfiguration> httpAgentConfigurationClazz = restClient.config();
        try {
            Constructor<? extends HttpAgentConfiguration> constructor = httpAgentConfigurationClazz.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            Logs.err(e.getMessage());
            throw new RestClientCommonException("HttpAgentCallConfiguration class 는 기본 생성자는 필수 입니다.");
        }
    }
}
