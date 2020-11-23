package io.cc54112700.springbean.config.annotation;

import io.cc54112700.springbean.entity.Student;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class InitBean {

    public static void main(String[] args) {

        // 注意 ComponentScan
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ScanConfig.class);

        // 注意@Component
        Book b = context.getBean(Book.class);

        b.init();

        context.close();
    }
}
