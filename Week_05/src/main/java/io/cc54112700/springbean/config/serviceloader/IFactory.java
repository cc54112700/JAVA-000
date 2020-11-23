package io.cc54112700.springbean.config.serviceloader;

import io.cc54112700.springbean.entity.Student;

public interface IFactory {

    default Student iFactory() {

        return new Student(1024, "Night Mare");
    }
}
