package test.bbackjk.http.core.configuration;

import lombok.Getter;
import lombok.ToString;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Optional;

@Getter
@Configuration
@ToString
public class RestClientConnectProperties {

    private static final String PROPERTY_CONNECTION_PREFIX = "bback.http.connect";
    private static final String PROPERTY_SOCKET_PREFIX = "bback.http.socket";
    private static final String KEY_CONNECT_TIMEOUT = "timeout";
    private static final String KEY_CONNECT_POOL_SIZE = "pool-size";
    private static final String KEY_CONNECT_KEEP_ALIVE = "keep-alive";
    private static final String KEY_CONNECT_POOL_PER_ROUTE = "pool-per-route";
    private final int socketTimeout;
    private final int connectPoolSize;
    private final int connectKeepAlive;
    private final int connectPoolPerRoute;

    public RestClientConnectProperties(Environment env) {
        this.socketTimeout = Integer.parseInt(this.getSocketValue(env, KEY_CONNECT_TIMEOUT).orElseGet(() -> "180"));                // second
        this.connectPoolSize = Integer.parseInt(this.getConnectValue(env, KEY_CONNECT_POOL_SIZE).orElseGet(() -> "20"));            // amount
        this.connectKeepAlive = Integer.parseInt(this.getConnectValue(env, KEY_CONNECT_KEEP_ALIVE).orElseGet(() -> "5"));           // second
        this.connectPoolPerRoute = Integer.parseInt(this.getConnectValue(env, KEY_CONNECT_POOL_PER_ROUTE).orElseGet(() -> "5"));    // amount
    }

    private Optional<String> getConnectValue(Environment env, String key) {
        return Optional.ofNullable(env.getProperty(String.format("%s.%s", PROPERTY_CONNECTION_PREFIX, key)));
    }

    private Optional<String> getSocketValue(Environment env, String key) {
        return Optional.ofNullable(env.getProperty(String.format("%s.%s", PROPERTY_SOCKET_PREFIX, key)));
    }
}
