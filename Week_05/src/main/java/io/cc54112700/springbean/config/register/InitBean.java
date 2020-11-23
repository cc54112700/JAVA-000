package io.cc54112700.springbean.config.register;

import io.cc54112700.springbean.entity.Student;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

//
public class InitBean {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(Student.class);
        context.refresh();

        Student student = context.getBean(Student.class);

        student.init();

        context.close();
    }
}
