package io.github.johntortoise.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;


@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext context;


    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.context = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return context;
    }


    public static <T> T getBean(Class<T> clazz){
        return context.getBean(clazz);
    }

}