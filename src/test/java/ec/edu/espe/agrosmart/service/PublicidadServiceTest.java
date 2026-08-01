package ec.edu.espe.agrosmart.service;

import ec.edu.espe.agrosmart.ai.AgroSmartAIService;
import ec.edu.espe.agrosmart.persistence.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;

class PublicidadServiceTest {

    @Test
    void generarPublicidad_cuandoElProveedorResponde_debeEmitirElTextoGenerado() {
        // Arrange
        ProductoRepository repository = Mockito.mock(ProductoRepository.class);
        AgroSmartAIService aiService = Mockito.mock(AgroSmartAIService.class);

        Mockito.when(aiService.generarPublicidad(
                        "Café arábigo",
                        "exportadores europeos"
                ))
                .thenReturn("Café arábigo premium para conquistar Europa.");

        ProductoService service = new ProductoService(repository, aiService);

        // Act
        var resultado = service.generarPublicidad(
                "Café arábigo",
                "exportadores europeos"
        );

        // Assert
        StepVerifier.create(resultado)
                .expectNext("Café arábigo premium para conquistar Europa.")
                .verifyComplete();
    }

    @Test
    void generarPublicidad_cuandoElProveedorFalla_debeEmitirMensajeDeRespaldo() {
        // Arrange
        ProductoRepository repository = Mockito.mock(ProductoRepository.class);
        AgroSmartAIService aiService = Mockito.mock(AgroSmartAIService.class);

        Mockito.when(aiService.generarPublicidad(
                        anyString(),
                        anyString()
                ))
                .thenThrow(new RuntimeException("429 Too Many Requests"));

        ProductoService service = new ProductoService(repository, aiService);

        // Act
        var resultado = service.generarPublicidad(
                "Café",
                "exportadores"
        );

        // Assert
        StepVerifier.create(resultado)
                .expectNextMatches(texto ->
                        texto.contains("Publicidad no disponible")
                                && texto.contains("RuntimeException")
                )
                .verifyComplete();
    }
}