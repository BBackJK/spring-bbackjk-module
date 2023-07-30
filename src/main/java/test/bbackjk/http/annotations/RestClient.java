package test.bbackjk.http.annotations;

import org.springframework.core.annotation.AliasFor;
import test.bbackjk.http.configuration.DefaultHttpAgentConfiguration;
import test.bbackjk.http.interfaces.HttpAgentConfiguration;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestClient {


    @AliasFor("url")
    String value() default "";
    @AliasFor("value")
    String url() default "";

    Class<? extends HttpAgentConfiguration> config() default DefaultHttpAgentConfiguration.class;

}
