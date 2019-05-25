package gradle.cucumber.types;

import com.twasyl.slideshowfx.content.extension.Resource;
import com.twasyl.slideshowfx.content.extension.ResourceType;
import com.twasyl.slideshowfx.engine.Variable;
import com.twasyl.slideshowfx.engine.presentation.configuration.Slide;
import com.twasyl.slideshowfx.engine.presentation.configuration.SlideElement;
import com.twasyl.slideshowfx.engine.template.configuration.SlideElementTemplate;
import com.twasyl.slideshowfx.engine.template.configuration.SlideTemplate;
import com.twasyl.slideshowfx.utils.beans.Pair;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableEntryTransformer;
import io.cucumber.datatable.TableTransformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Class containing all Cucumber custom {@link DataTableType}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class CustomDataTableTypes {

    private static final String SLIDE_ID = "Slide ID";
    private static final String SLIDE_NUMBER = "Slide number";
    private static final String TEMPLATE_ID = "Template ID";
    private static final String ELEMENT_ID = "Element ID";
    private static final String ORIGINAL_CONTENT_CODE = "Original content code";
    private static final String ORIGINAL_CONTENT = "Original content";
    private static final String HTML_CONTENT = "HTML content";
    private static final String TYPE = "Type";
    private static final String CONTENT = "Content";
    private static final String NAME = "Name";
    private static final String VALUE = "Value";

    private CustomDataTableTypes() {
    }

    public static DataTableType forSlides() {
        return new DataTableType(Slide.class, slidesTableEntryTransformer());
    }

    public static DataTableType forSlideElements() {
        return new DataTableType(SlideElement.class, slideElementsTableEntryTransformer());
    }

    public static DataTableType forSlidesAndSlideElementsMapping() {
        return new DataTableType(SlidesAndSlideElementsMapping.class, slidesAndSlideElementsTableTransformer());
    }

    public static DataTableType forCustomResources() {
        return new DataTableType(Resource.class, customResourcesTableEntryTransformer());
    }

    public static DataTableType forVariables() {
        return new DataTableType(Variable.class, variablesTableEntryTransformer());
    }

    private static TableEntryTransformer<Slide> slidesTableEntryTransformer() {
        return entry -> {
            final Slide slide = new Slide();

            if (entry.containsKey(SLIDE_ID)) {
                slide.setId(entry.get(SLIDE_ID));
            }

            if (entry.containsKey(SLIDE_NUMBER)) {
                slide.setSlideNumber(entry.get(SLIDE_NUMBER));
            }

            if (entry.containsKey(TEMPLATE_ID)) {
                final SlideTemplate template = new SlideTemplate();
                template.setId(Integer.parseInt(entry.get(TEMPLATE_ID)));
                slide.setTemplate(template);
            }
            return slide;
        };
    }

    private static TableEntryTransformer<SlideElement> slideElementsTableEntryTransformer() {
        return CustomDataTableTypes::entryToSlideElement;
    }

    private static TableTransformer slidesAndSlideElementsTableTransformer() {
        return table -> {
            final SlidesAndSlideElementsMapping mapping = new SlidesAndSlideElementsMapping();

            if (table.height() > 1) {
                final List<String> headers = table.row(0);
                final int slideIdHeaderIndex = headers.indexOf(SLIDE_ID);

                for (int rowIndex = 1; rowIndex < table.height(); rowIndex++) {
                    final List<String> row = table.row(rowIndex);
                    final String slideId = row.get(slideIdHeaderIndex);

                    mapping.addSlideElement(slideId, entryToSlideElement(toEntry(headers, row)));
                }

            }
            return mapping;
        };
    }

    private static Map<String, String> toEntry(final List<String> headers, final List<String> values) {
        final Map<String, String> entry = new HashMap<>();

        for (int index = 0; index < headers.size(); index++) {
            entry.put(headers.get(index), values.get(index));
        }

        return entry;
    }

    private static SlideElement entryToSlideElement(final Map<String, String> entry) {
        final SlideElement slideElement = new SlideElement();

        if (entry.containsKey(TEMPLATE_ID)) {
            final SlideElementTemplate template = new SlideElementTemplate();
            template.setId(Integer.parseInt(entry.get(TEMPLATE_ID)));
            slideElement.setTemplate(template);
        }

        if (entry.containsKey(ELEMENT_ID)) {
            slideElement.setId(entry.get(ELEMENT_ID));
        }

        if (entry.containsKey(ORIGINAL_CONTENT_CODE)) {
            slideElement.setOriginalContentCode(entry.get(ORIGINAL_CONTENT_CODE));
        }

        if (entry.containsKey(ORIGINAL_CONTENT)) {
            slideElement.setOriginalContent(entry.get(ORIGINAL_CONTENT));
        }

        if (entry.containsKey(HTML_CONTENT)) {
            slideElement.setHtmlContent(entry.get(HTML_CONTENT));
        }

        return slideElement;
    }

    private static TableEntryTransformer<Resource> customResourcesTableEntryTransformer() {
        return entry -> {
            if (!entry.containsKey(TYPE)) {
                fail("Type for custom resource is not set");
            }

            if (!entry.containsKey(CONTENT)) {
                fail("Content for custom resource is not set");
            }

            return new Resource(ResourceType.valueOf(entry.get(TYPE)), entry.get(CONTENT));
        };
    }

    private static TableEntryTransformer<Variable> variablesTableEntryTransformer() {
        return entry -> {
            final Variable variable = new Variable();

            if (entry.containsKey(NAME)) {
                variable.setName(entry.get(NAME));
            }

            if (entry.containsKey(VALUE)) {
                variable.setValue(entry.get(VALUE));
            }

            return variable;
        };
    }
}
