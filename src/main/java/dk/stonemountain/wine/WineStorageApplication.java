package dk.stonemountain.wine;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.ws.rs.core.Application;

@OpenAPIDefinition(
    tags = {
        @Tag(name="wine-storage", description="Wine Storage operations.")
    },
    info = @Info(
        title="Example API",
        version = "1.0.0",
        contact = @Contact(
            name = "Fin Steenbjerg",
            url = "https://stonemountain.dk/contact",
            email = "fin.steenbjerg@gmail.com"))
)
public class WineStorageApplication extends Application {
}
