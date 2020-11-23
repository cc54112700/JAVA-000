package io.cc54112700.springbean.config.serviceloader;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Iterator;
import java.util.ServiceLoader;

public class InitBean {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(IServiceLoaderFactoryBean.class);
        context.refresh();

        AutowireCapableBeanFactory autowireCapableBeanFactory = context.getAutowireCapableBeanFactory();

        IFactoryImpl bean = autowireCapableBeanFactory.createBean(IFactoryImpl.class);

        System.out.println(bean.iFactory());
        bean.iFactory().init();

        context.close();
    }
}