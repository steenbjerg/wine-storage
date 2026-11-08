package dk.stonemountain.wine.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.jboss.logging.Logger;

import dk.stonemountain.wine.domain.Price;
import dk.stonemountain.wine.domain.Wine;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class WineInitializer {
    private static final Logger LOG = Logger.getLogger(WineInitializer.class);
    private static final String VENDOR_DESCRIPTION = """
    Denne unikke Altos de Montote kommer fra et område med gamle Grenache-vinstokke, en af de få tilbageværende i La Rioja og en af de ældste. Denne vin er lavet på 100% Grenache. Druerne er høstet i hånden og omhyggeligt udvalgt for at opnå en vin med en enestående personlighed, behagelig på næsen og med en kraftfuld smag. Fermenteringen af mosten er foretaget separat, efter flere teknikker som "Delestage".
    Vinen har en utrolig flot og fremtrædende kirsebærrød farve med mørke toner. Dens aroma minder os om flere røde frugter som ribs og jordbær samt et strejf af sort peber, lakrids og anis. I munden har den en middelhavskarakter, meget livlig, der minder om syltetøj, krydderier, tørre figner og dadler. Snyd ikke dig selv for denne topklasse af en vin!
    Server vinen ved 15-17 grader og prøv den til lam, oksekød eller til vildt. Du kan også servere den helt for sig selv, så du kan få alle de skønne noter og drømme dig til Spanien.

    JAMES SUCKLING 92 points
    "This has a super rich array of plums and summer berries on the nose with a sleek, flavorful palate that carries such attractive and fleshy texture."
    """;

    @Inject
    WineService service;

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        LOG.info("Wine storage is starting – ensuring seed data");

        // Only seed if empty to avoid duplicating on each restart
        if (Wine.count() == 0) {
            persistSampleData();
            LOG.info("Seeded initial wine data");
        } else {
            LOG.infof("Skipping seed; %d wines already present", Wine.count());
        }
    }

    void onStop(@Observes ShutdownEvent ev) {
        LOG.info("Wine storage is stopping");
    }

    private void persistSampleData() {
        Wine viejas = new Wine();
        viejas.type = Wine.WineType.RED;
        viejas.name = "Altos de Montote Rioja Garnacha de Vinas Viejas";
        viejas.year = 2013;
        viejas.country = Wine.Country.SPAIN;
        viejas.region = "Rioja";
        viejas.grape = "Grenache";
        viejas.alcoholPercentage = 14.0;
        viejas.vendor = "https://www.megavin.dk";
        viejas.vendorUrl = "https://www.megavin.dk/altos-de-montote-garnacha-de-vinas-viejas-rioja-1";
        viejas.vendorDescription = VENDOR_DESCRIPTION;
        viejas.purchaseDate = ZonedDateTime.now();
        viejas.purchasePrice = new Price();
        viejas.purchasePrice.cost = 99.95;
        viejas.purchasePrice.pieces = 6;
        viejas.salePrice = new Price();
        viejas.salePrice.cost = 349.95;
        viejas.salePrice.pieces = 1;
        viejas.inStock = 6;
        viejas.note = "Not tried yet";
        viejas.rating = 0;
        viejas.persist();

        Optional<Path> tempFile = Optional.empty();
        try (var is = WineInitializer.class.getResourceAsStream("/images/altos-de-montote-garnacha-de-vinas-viejas-rioja-1.png")) {
            tempFile = Optional.of(Files.createTempFile("wine-", ".png"));
            Files.copy(is, tempFile.get(), StandardCopyOption.REPLACE_EXISTING);
            service.putImage(viejas, tempFile.get().toFile());
        } catch (IllegalArgumentException | UnsupportedOperationException | IOException e) {
            LOG.error("Failed to store image", e);
        } finally {
            tempFile.ifPresent(f -> {
                try {
                    Files.delete(f);
                } catch(IOException e) {
                    LOG.error("Failed to delete file", e);
                }
            });
        }
    }
}
