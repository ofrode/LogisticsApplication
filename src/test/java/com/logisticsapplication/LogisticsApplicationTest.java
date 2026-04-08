package com.logisticsapplication;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class LogisticsApplicationTest {

    @Test
    void mainDelegatesToSpringApplicationRun() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            LogisticsApplication.main(new String[]{"--spring.main.banner-mode=off"});

            springApplication.verify(
                    () -> SpringApplication.run(
                            LogisticsApplication.class,
                            new String[]{"--spring.main.banner-mode=off"}
                    )
            );
        }
    }
}
