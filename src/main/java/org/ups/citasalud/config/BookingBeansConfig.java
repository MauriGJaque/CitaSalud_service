package org.ups.citasalud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ups.citasalud.booking.application.usecase.ConsultarDisponibilidadUseCase;
import org.ups.citasalud.booking.application.usecase.LiberarFranjaRetenidaUseCase;
import org.ups.citasalud.booking.application.usecase.ReservarCitaUseCase;
import org.ups.citasalud.booking.application.usecase.SeleccionarFranjaUseCase;
import org.ups.citasalud.booking.domain.port.CitaRepositoryPort;
import org.ups.citasalud.booking.domain.port.Clock;
import org.ups.citasalud.booking.domain.port.FranjaHorariaRepositoryPort;
import org.ups.citasalud.booking.domain.port.MedicoRepositoryPort;
import org.ups.citasalud.booking.domain.port.NotificacionPort;
import org.ups.citasalud.booking.domain.port.PacienteRepositoryPort;

/**
 * Conecta los casos de uso (capa `application`, sin anotaciones de
 * framework) con los adapters (capa `adapter`) que implementan sus puertos
 * (Principio I: Clean Architecture).
 */
@Configuration
public class BookingBeansConfig {

    @Bean
    public LiberarFranjaRetenidaUseCase liberarFranjaRetenidaUseCase(
            FranjaHorariaRepositoryPort franjaHorariaRepositoryPort, Clock clock) {
        return new LiberarFranjaRetenidaUseCase(franjaHorariaRepositoryPort, clock);
    }

    @Bean
    public ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase(
            FranjaHorariaRepositoryPort franjaHorariaRepositoryPort,
            LiberarFranjaRetenidaUseCase liberarFranjaRetenidaUseCase,
            Clock clock) {
        return new ConsultarDisponibilidadUseCase(franjaHorariaRepositoryPort, liberarFranjaRetenidaUseCase, clock);
    }

    @Bean
    public SeleccionarFranjaUseCase seleccionarFranjaUseCase(
            FranjaHorariaRepositoryPort franjaHorariaRepositoryPort, Clock clock) {
        return new SeleccionarFranjaUseCase(franjaHorariaRepositoryPort, clock);
    }

    @Bean
    public ReservarCitaUseCase reservarCitaUseCase(
            CitaRepositoryPort citaRepositoryPort,
            FranjaHorariaRepositoryPort franjaHorariaRepositoryPort,
            MedicoRepositoryPort medicoRepositoryPort,
            PacienteRepositoryPort pacienteRepositoryPort,
            NotificacionPort notificacionPort,
            Clock clock) {
        return new ReservarCitaUseCase(citaRepositoryPort, franjaHorariaRepositoryPort, medicoRepositoryPort,
                pacienteRepositoryPort, notificacionPort, clock);
    }
}
