package br.ufc.ds.trabalho3.api;

import br.ufc.ds.trabalho2.app.InvestidorServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InvestmentConfiguration {

    @Bean
    public InvestidorServiceImpl investidorService() {
        return new InvestidorServiceImpl();
    }
}
