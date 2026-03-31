package edu.pe.cibertec.infracciones.test;

import edu.pe.cibertec.infracciones.model.EstadoMulta;
import edu.pe.cibertec.infracciones.model.Infractor;
import edu.pe.cibertec.infracciones.model.Multa;
import edu.pe.cibertec.infracciones.repository.InfractorRepository;
import edu.pe.cibertec.infracciones.service.impl.InfractorServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("InfractorSeriviceImpl - Unit Test With Mockito")
public class InfractorServiceImplTest {

    @Mock
    private InfractorRepository repository;

    @InjectMocks
    private InfractorServiceImpl service;

    @Test
    @DisplayName("Validar Bloqueo a Infractor con menos de 3 multas Vencidas")
    void dadoUnInfractorQueTiene2MultasVencidasY3Pagadas_entoncesNoSeBloqueaNiLanzaException(){
        Long infractorId = 1L;

        List<Multa> multasHarcodeada = List.of(
                multaVencida(),
                multaVencida(),
                multaPagada(),
                multaPagada(),
                multaPagada()
        );

        Infractor infractor = new Infractor();
        infractor.setId(infractorId);
        infractor.setMultas(multasHarcodeada);

        when(repository.findById(1L)).thenReturn(Optional.of(infractor));
        service.verificarBloqueo(1L);
        assertFalse(infractor.isBloqueado());

        verify(repository, never()).save(any());
    }

    //region Helpers para Multa
    private Multa multaVencida() {
        Multa m = new Multa();
        m.setEstado(EstadoMulta.VENCIDA);
        return m;
    }

    private Multa multaPagada() {
        Multa m = new Multa();
        m.setEstado(EstadoMulta.PAGADA);
        return m;
    }

    //endregion Helpers para Multa
}