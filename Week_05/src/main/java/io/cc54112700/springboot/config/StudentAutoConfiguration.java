package io.cc54112700.springboot.config;

import io.cc54112700.springboot.entity.Klass;
import io.cc54112700.springboot.entity.Student;
import io.cc54112700.springboot.properties.StudentProperties;
import io.cc54112700.springboot.service.impl.School;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@EnableConfigurationProperties(StudentProperties.class)
@ConditionalOnProperty(prefix = "i.student", name = "enabled", havingValue = "true", matchIfMissing = true)
public class StudentAutoConfiguration {

    @Autowired
    private StudentProperties studentProperties;

    @Bean(name = "student1024")
    @ConditionalOnMissingBean
    public Student createStudent() {

        return new Student(studentProperties.getId(), studentProperties.getName());
    }

    @Bean(name = "class1")
    @ConditionalOnBean(name = {"student1024"})
    @ConditionalOnMissingBean(name = {"class1"})
    public Klass initKlass(Student student1024) {

        Klass class1 = new Klass();
        class1.setStudents(Arrays.asList(student1024));

        return class1;
    }

    @Bean
    @ConditionalOnBean(name = {"class1"})
    @ConditionalOnMissingBean(name = {"school1"})
    public School initSchool(Klass class1) {

        School school = new School();
        school.setClass1(class1);

        return school;
    }

}
