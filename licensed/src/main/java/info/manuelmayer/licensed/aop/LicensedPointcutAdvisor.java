package info.manuelmayer.licensed.aop;

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;

import info.manuelmayer.licensed.annotation.Licensed;

public class LicensedPointcutAdvisor extends AbstractPointcutAdvisor {

    private static final long serialVersionUID = 1L;
    
    @Autowired
    private LicensedInterceptor advice;

    @Override
    public Pointcut getPointcut() {
        return new StaticMethodMatcherPointcut() {
            @Override
            public boolean matches(Method method, Class<?> targetClass) {
                return AnnotationUtils.findAnnotation(method, Licensed.class) != null 
                        || AnnotationUtils.findAnnotation(targetClass, Licensed.class) != null;
            }
        };
    }

    @Override
    public Advice getAdvice() {
        return advice;
    }

}
