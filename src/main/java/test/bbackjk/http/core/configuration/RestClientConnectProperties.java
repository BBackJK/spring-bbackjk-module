package test.bbackjk.http.core.configuration;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import test.bbackjk.http.core.util.RestClientUtils;

@Getter
@Configuration
public class RestClientConnectProperties {

    private static final String PROPERTY_PREFIX = "bbackjk.http.connect";
    private static final String KEY_CONNECT_TIMEOUT = "timeout";
    private static final String DEFAULT_CONNECT_TIMEOUT = "10";
    private static final String KEY_CONNECT_POOL_SIZE = "pool-size";
    private static final String DEFAULT_POOL_SIZE = "5";

    private static final String KEY_CONNECT_KEEP_ALIVE = "keep-alive";
    private static final String DEFAULT_KEEP_ALIVE = "5";
    private final int timout;
    private final int poolSize;
    private final int keepAlive;

    public RestClientConnectProperties(Environment env) {
        this.timout = Integer.parseInt(
                RestClientUtils.orElse(this.getValue(env, KEY_CONNECT_TIMEOUT), DEFAULT_CONNECT_TIMEOUT)
        );
        this.poolSize = Integer.parseInt(
                RestClientUtils.orElse(this.getValue(env, KEY_CONNECT_POOL_SIZE), DEFAULT_POOL_SIZE)
        );
        this.keepAlive = Integer.parseInt(
                RestClientUtils.orElse(this.getValue(env, KEY_CONNECT_KEEP_ALIVE), DEFAULT_KEEP_ALIVE)
        );
    }

    private String getValue(Environment env, String key) {
        return env.getProperty(String.format("%s.%s", PROPERTY_PREFIX, key));
    }
}
