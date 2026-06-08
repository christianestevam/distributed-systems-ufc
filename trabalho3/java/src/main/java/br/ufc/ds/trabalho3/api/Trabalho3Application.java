package br.ufc.ds.trabalho3.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "br.ufc.ds.trabalho2.app",
        "br.ufc.ds.trabalho3.api"
})
public class Trabalho3Application {

    public static void main(String[] args) {
        SpringApplication.run(Trabalho3Application.class, args);
    }
}