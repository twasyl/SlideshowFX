package gradle.cucumber;

import cucumber.api.TypeRegistry;
import cucumber.api.TypeRegistryConfigurer;
import gradle.cucumber.types.CustomDataTableTypes;

import java.util.Locale;

import static java.util.Locale.ENGLISH;

/**
 * Class registering custom data type to be used in Gherkin files.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class TypeRegistryConfiguration implements TypeRegistryConfigurer {
    @Override
    public Locale locale() {
        return ENGLISH;
    }

    @Override
    public void configureTypeRegistry(TypeRegistry typeRegistry) {
        typeRegistry.defineDataTableType(CustomDataTableTypes.forSlides());
        typeRegistry.defineDataTableType(CustomDataTableTypes.forSlideElements());
        typeRegistry.defineDataTableType(CustomDataTableTypes.forSlidesAndSlideElementsMapping());
        typeRegistry.defineDataTableType(CustomDataTableTypes.forCustomResources());
        typeRegistry.defineDataTableType(CustomDataTableTypes.forVariables());
    }
}
