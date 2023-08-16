package test.bbackjk.http.core.configuration;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Optional;

@Getter
@Configuration
public class RestClientConnectProperties {

    private static final String PROPERTY_PREFIX = "bbackjk.http.connect";
    private static final String KEY_CONNECT_TIMEOUT = "timeout";
    private static final String KEY_CONNECT_POOL_SIZE = "pool-size";
    private static final String KEY_CONNECT_KEEP_ALIVE = "keep-alive";
    private final int timout;
    private final int poolSize;
    private final int keepAlive;

    public RestClientConnectProperties(Environment env) {
        this.timout = Integer.parseInt(this.getValue(env, KEY_CONNECT_TIMEOUT).orElseGet(() -> "10"));
        this.poolSize = Integer.parseInt(this.getValue(env, KEY_CONNECT_POOL_SIZE).orElseGet(() -> "5"));
        this.keepAlive = Integer.parseInt(this.getValue(env, KEY_CONNECT_KEEP_ALIVE).orElseGet(() -> "5"));
    }

    private Optional<String> getValue(Environment env, String key) {
        return Optional.ofNullable(env.getProperty(String.format("%s.%s", PROPERTY_PREFIX, key)));
    }
}
