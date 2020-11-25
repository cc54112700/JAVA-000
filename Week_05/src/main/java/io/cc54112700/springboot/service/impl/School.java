package io.cc54112700.springboot.service.impl;

import io.cc54112700.springboot.entity.Klass;
import io.cc54112700.springboot.entity.Student;
import io.cc54112700.springboot.service.ISchool;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

@Data
public class School implements ISchool {
    
    // Resource 
    @Autowired(required = true) //primary
    Klass class1;
    
    @Resource(name = "student1024")
    Student student100;
    
    @Override
    public void ding(){
    
        System.out.println("Class1 have " + this.class1.getStudents().size() + " students and one is " + this.student100);
        
    }
}
