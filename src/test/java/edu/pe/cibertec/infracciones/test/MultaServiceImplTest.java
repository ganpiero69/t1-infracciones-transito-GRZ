package edu.pe.cibertec.infracciones.test;

import edu.pe.cibertec.infracciones.model.EstadoMulta;
import edu.pe.cibertec.infracciones.model.Multa;
import edu.pe.cibertec.infracciones.repository.MultaRepository;
import edu.pe.cibertec.infracciones.service.impl.MultaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MultaServiceImpl - actualizarEstados")
public class MultaServiceImplTest {

    @Mock
    private MultaRepository repository;

    @InjectMocks
    private MultaServiceImpl service;

    private Multa multaPendiente;

    @BeforeEach
    void setUp() {
        multaPendiente = new Multa();
        multaPendiente.setEstado(EstadoMulta.PENDIENTE);
        multaPendiente.setFechaVencimiento(LocalDate.of(2026, 1, 1));
    }

    @Test
    @DisplayName("Actualizar estado de multa pendiente vencida")
    void dadoMultaPendienteConFechaVencida_cuandoActualizarEstados_entoncesSeVuelveVencidaYSeGuarda() {

        when(repository.findByEstado(EstadoMulta.PENDIENTE)).thenReturn(List.of(multaPendiente));

        service.actualizarEstados();

        assertEquals(EstadoMulta.VENCIDA, multaPendiente.getEstado());

        verify(repository, times(1)).save(multaPendiente);
    }
}