package io.cc54112700.springbean.config.factorybean;

import io.cc54112700.springbean.entity.Student;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class InitBean {

    public static void main(String[] args) throws Exception {


        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        context.register(IFactoryBean.class);
        context.refresh();

        IFactoryBean iFactoryBean = context.getBean(IFactoryBean.class);
        Student student = iFactoryBean.getObject();
        System.out.println(student);
        student.init();

        context.close();
    }
}
