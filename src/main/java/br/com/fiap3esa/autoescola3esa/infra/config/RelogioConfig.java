package br.com.fiap3esa.autoescola3esa.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Expõe o relógio da aplicação como bean. Nos testes unitários, o Clock é substituído por um
 * relógio fixo, o que deixa as regras de antecedência (30 min / 24 h) determinísticas.
 */
@Configuration
public class RelogioConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
