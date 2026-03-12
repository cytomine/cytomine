package org.cytomine.repository;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import org.cytomine.repository.http.serialization.ObjectMapperFactory;

@SpringBootApplication
@Import(ObjectMapperFactory.class)
public class RepositoryApp {
    public static void main(String[] args) {
        SpringApplication.run(RepositoryApp.class, args);
    }

}
