package io.cc54112700.springbean.config.xml;

import io.cc54112700.springbean.entity.Student;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class InitBean {

    public static void main(String[] args) {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");

        Student student = (Student) context.getBean("student001");

        System.out.println(student);

        context.close();
    }
}
