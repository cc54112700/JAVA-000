package io.cc54112700.springboot.properties;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "i.student")
public class StudentProperties {

    private int id = 1024;
    private String name = "John";
}
