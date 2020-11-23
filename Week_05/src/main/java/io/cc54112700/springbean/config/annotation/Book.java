package io.cc54112700.springbean.config.annotation;

import org.springframework.stereotype.Component;


@Component
public class Book {

    private int id;
    private String name;

    public void init() {
        System.out.println("hello.this.is.a.book........");
    }
}
