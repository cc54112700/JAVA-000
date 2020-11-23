package io.cc54112700.springbean.config.serviceloader;

import org.springframework.beans.factory.serviceloader.ServiceLoaderFactoryBean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class IServiceLoaderFactoryBean extends ServiceLoaderFactoryBean {

    @PostConstruct
    public void setServiceType() {
        super.setServiceType(IFactory.class);
    }
}
