package io.cc54112700.springbean.config.beanfactory;

import io.cc54112700.springbean.entity.Student;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.annotation.PostConstruct;

public class InitBean implements BeanFactoryAware {

    private BeanFactory beanFactory;

    @PostConstruct
    public void register() {

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(Student.class);

        builder.addPropertyValue("id", 551)
                .addPropertyValue("name", "John Wick");

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) this.beanFactory;
        registry.registerBeanDefinition("student", builder.getBeanDefinition());
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {

        this.beanFactory = beanFactory;
    }

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(InitBean.class);
        Student student = context.getBean(Student.class);
        System.out.println(student);
        student.init();

        context.close();
    }
}
