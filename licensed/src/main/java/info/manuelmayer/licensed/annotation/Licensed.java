package info.manuelmayer.licensed.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Inherited
public @interface Licensed {

    boolean currentDevice() default false;
    
    boolean currentUser() default false;
    
    boolean devices() default false;
    
    String feature() default "";
    
    boolean host() default false;
    
    String key();

    boolean period() default false;
    
    boolean users() default false;
    
    boolean version() default false;
    
}
