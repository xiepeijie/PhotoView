package me.payge.evenbus;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface EventObserver {
    int runThread() default RunThread.CURRENT;

    interface RunThread {
        int CURRENT = 1;
        int MAIN = 2;
        int ASYNC = 3;
    }
}
