package edu.pe.cibertec.infracciones.test;

import edu.pe.cibertec.infracciones.dto.PagoResponseDTO;
import edu.pe.cibertec.infracciones.model.EstadoMulta;
import edu.pe.cibertec.infracciones.model.Multa;
import edu.pe.cibertec.infracciones.repository.MultaRepository;
import edu.pe.cibertec.infracciones.repository.PagoRepository;
import edu.pe.cibertec.infracciones.service.impl.PagoServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("PagoServiceImplTest - Unit Test With Mockito y Clock")
public class PagoServiceImplTest {

    @Mock
    private MultaRepository multaRepository;

    @Mock
    private PagoRepository pagoRepository;

    @InjectMocks
    private PagoServiceImpl service;

    @Test
    @DisplayName("Procesar pago dentro de los primeros 5 días aplica descuento 20% y cambia estado a PAGADA")
    void dadoMultaPendienteHoy_aplicaDescuento20YcambiaEstadoAPagada() {
        LocalDate fechaFija = LocalDate.of(2026, 3, 30);
        Clock fixedClock = Clock.fixed(fechaFija.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

        Multa multa = new Multa();
        multa.setId(1L);
        multa.setMonto(500.0);
        multa.setFechaEmision(fechaFija); // mismo día que hoy → aplica 20% descuento
        multa.setFechaVencimiento(fechaFija.plusDays(10));
        multa.setEstado(EstadoMulta.PENDIENTE);

        when(multaRepository.findById(1L)).thenReturn(Optional.of(multa));

        service = new PagoServiceImpl(pagoRepository, multaRepository, fixedClock);

        PagoResponseDTO pagoDTO = service.procesarPago(1L);

        // Verificamos estado de la multa
        assertEquals(EstadoMulta.PAGADA, multa.getEstado());

        // Verificamos monto pagado con descuento aplicado
        double montoEsperado = 500.0 - (500.0 * 0.20); // 400.0
        assertEquals(montoEsperado, pagoDTO.getMontoPagado());

        // Verificamos descuento aplicado correctamente
        assertEquals(500.0 * 0.20, pagoDTO.getDescuentoAplicado());

        // Verificamos que el pago fue guardado
        verify(pagoRepository).save(argThat(p ->
                p.getMontoPagado() == montoEsperado &&
                        p.getDescuentoAplicado() == 500.0 * 0.20
        ));

        // Verificamos que la multa se guardó
        verify(multaRepository).save(multa);
    }
}