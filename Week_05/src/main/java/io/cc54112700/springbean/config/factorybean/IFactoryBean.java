package io.cc54112700.springbean.config.factorybean;

import io.cc54112700.springbean.entity.Student;
import org.springframework.beans.factory.FactoryBean;

public class IFactoryBean implements FactoryBean<Student> {

    @Override
    public Student getObject() throws Exception {
        return new Student(7, "John Wick");
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
