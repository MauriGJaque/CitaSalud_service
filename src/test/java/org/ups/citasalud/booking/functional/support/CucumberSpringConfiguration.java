package org.ups.citasalud.booking.functional.support;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.ups.citasalud.booking.domain.port.Clock;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
public class CucumberSpringConfiguration {

    @TestConfiguration
    static class ClockDePruebaConfig {

        @Bean
        @Primary
        public Clock clockDePrueba() {
            return new MutableTestClock();
        }
    }
}
