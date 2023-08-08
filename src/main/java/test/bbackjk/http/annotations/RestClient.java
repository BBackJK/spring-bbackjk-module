package test.bbackjk.http.annotations;

import org.springframework.core.annotation.AliasFor;
import test.bbackjk.http.bean.agent.OkHttpAgent;
import test.bbackjk.http.interfaces.HttpAgent;
import test.bbackjk.http.interfaces.ResponseMapper;
import test.bbackjk.http.bean.mapper.DefaultResponseMapper;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestClient {

    @AliasFor("context")
    String value() default "";
    @AliasFor("value")
    String context() default "";
    String url();

    Class<? extends HttpAgent> agent() default OkHttpAgent.class;
    Class<? extends ResponseMapper> mapper() default DefaultResponseMapper.class;
}
