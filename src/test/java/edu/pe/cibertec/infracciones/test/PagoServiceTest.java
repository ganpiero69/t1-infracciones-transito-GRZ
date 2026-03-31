package edu.pe.cibertec.infracciones.test;

import edu.pe.cibertec.infracciones.dto.PagoResponseDTO;
import edu.pe.cibertec.infracciones.exception.MultaNotFoundException;
import edu.pe.cibertec.infracciones.model.EstadoMulta;
import edu.pe.cibertec.infracciones.model.Multa;
import edu.pe.cibertec.infracciones.model.Pago;
import edu.pe.cibertec.infracciones.repository.MultaRepository;
import edu.pe.cibertec.infracciones.repository.PagoRepository;
import edu.pe.cibertec.infracciones.service.impl.PagoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PagoServiceTest {

    private PagoRepository pagoRepository;
    private MultaRepository multaRepository;
    private PagoServiceImpl service;
    private Clock clock;

    @BeforeEach
    void setUp() {
        pagoRepository = mock(PagoRepository.class);
        multaRepository = mock(MultaRepository.class);

        LocalDate fechaFija = LocalDate.of(2026, 3, 30);
        clock = Clock.fixed(fechaFija.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

        service = new PagoServiceImpl(pagoRepository, multaRepository, clock);
    }

    @Test
    void procesarPago_conRecargo_correctamente() {
        // Dado
        LocalDate fechaEmision = LocalDate.now(clock).minusDays(12);  // hace 12 días
        LocalDate fechaVencimiento = LocalDate.now(clock).minusDays(2); // hace 2 días

        Multa multa = new Multa();
        multa.setId(1L);
        multa.setMonto(500.0);
        multa.setFechaEmision(fechaEmision);
        multa.setFechaVencimiento(fechaVencimiento);
        multa.setEstado(EstadoMulta.PENDIENTE);

        when(multaRepository.findById(1L)).thenReturn(Optional.of(multa));

        // Cuando
        PagoResponseDTO pagoDTO = service.procesarPago(1L);

        // Entonces
        // Capturamos el objeto Pago que se guardó
        ArgumentCaptor<Pago> captor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository, times(1)).save(captor.capture());
        Pago pagoGuardado = captor.getValue();

        // Validamos los valores calculados
        assertEquals(0.0, pagoGuardado.getDescuentoAplicado(), 0.001);
        assertEquals(75.0, pagoGuardado.getRecargo(), 0.001);
        assertEquals(575.0, pagoGuardado.getMontoPagado(), 0.001);

        // Validar que la multa cambió de estado
        assertEquals(EstadoMulta.PAGADA, multa.getEstado());
        verify(multaRepository, times(1)).save(multa);
    }
}