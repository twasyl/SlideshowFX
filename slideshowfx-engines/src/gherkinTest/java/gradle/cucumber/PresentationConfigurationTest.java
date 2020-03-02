package gradle.cucumber;

import com.twasyl.slideshowfx.content.extension.Resource;
import com.twasyl.slideshowfx.engine.Variable;
import com.twasyl.slideshowfx.engine.context.PresentationConfigurationTestContext;
import com.twasyl.slideshowfx.engine.presentation.configuration.Slide;
import gradle.cucumber.types.SlidesAndSlideElementsMapping;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;

public class PresentationConfigurationTest extends AbstractConfigurationTest {

    private PresentationConfigurationTestContext presentationConfigurationTestContext;

    @Before
    public void before() {
        changeJavaTmpDirForTests();
        this.presentationConfigurationTestContext = new PresentationConfigurationTestContext();
    }

    @After
    public void after() {
        this.presentationConfigurationTestContext.clean();
        resetJavaTmpDirForTests();
    }

    @Given("^a presentation$")
    public void loadPresentation() {
        this.presentationConfigurationTestContext.loadPresentation();
    }

    @When("^a valid presentation configuration is loaded$")
    public void loadValidConfiguration() {
        this.presentationConfigurationTestContext.loadValidConfiguration();
    }

    @Then("the following field(s) is/are defined in the presentation configuration file")
    public void theFieldIsEqualTo(final Map<String, String> mapping) {
        mapping.entrySet()
                .stream()
                .filter(entry -> !"Name".equals(entry.getKey()))
                .forEach(entry -> this.presentationConfigurationTestContext.assertFieldEquals(entry.getKey(), entry.getValue()));
    }

    @Then("these slides exist")
    public void slideExists(final List<Slide> slides) {
        this.presentationConfigurationTestContext.assertNumberOfSlides(slides.size());
        slides.forEach(slide -> assertAll("Assertions for slide " + slide.getId() + " have failed",
                () -> this.presentationConfigurationTestContext.withSlideId(slide.getId()),
                () -> this.presentationConfigurationTestContext.assertSlideNumber(slide.getSlideNumber()),
                () -> this.presentationConfigurationTestContext.assertSlideTemplateId(slide.getTemplate().getId()),
                () -> this.presentationConfigurationTestContext.assertSpeakerNotes(slide.getSpeakerNotes())));
    }

    @Then("the slide(s) has/have the following element(s)")
    public void slideHasSlideElements(final SlidesAndSlideElementsMapping mapping) {
        mapping.keySet().forEach(slideId -> {
            this.presentationConfigurationTestContext.withSlideId(slideId)
                    .assertNumberOfSlideElements(mapping.get(slideId).size());

            mapping.get(slideId).forEach(element -> assertAll(
                    "Assertions of slide elements for slide " + slideId + " have failed",
                    () -> this.presentationConfigurationTestContext.withSlideElement(element.getId()),
                    () -> this.presentationConfigurationTestContext.assertSlideElementTemplateId(element.getTemplate().getId()),
                    () -> this.presentationConfigurationTestContext.assertOriginalContentCode(element.getOriginalContentCode()),
                    () -> this.presentationConfigurationTestContext.assertOriginalContent(element.getOriginalContent()),
                    () -> this.presentationConfigurationTestContext.assertHtmlContent(element.getHtmlContent())));
        });
    }

    @Then("this/these custom resource(s) is/are defined")
    public void theseCustomResourcesAreDefined(final List<Resource> expectedCustomResources) {
        expectedCustomResources.forEach(this.presentationConfigurationTestContext::assertHasCustomResource);
    }

    @Then("this/these variable(s) is/are defined")
    public void theseVariablesAreDefined(final List<Variable> expectedVariables) {
        this.presentationConfigurationTestContext.assertNumberOfVariables(expectedVariables.size());
        expectedVariables.forEach(this.presentationConfigurationTestContext::assertHasVariable);
    }

    @Then("^the slide \"([^\"]+)\" is (before|after) the slide \"([^\"]+)\"$")
    public void siblingSlides(final String sibingSlideId, final String position, final String currentSlideId) {
        this.presentationConfigurationTestContext.withSlideId(currentSlideId);

        if ("after".equals(position)) {
            this.presentationConfigurationTestContext.assertNextSlideIs(sibingSlideId);
        } else {
            this.presentationConfigurationTestContext.assertPreviousSlideIs(sibingSlideId);
        }
    }

    @And("^the presentation (has no more|has no|has) slides$")
    public void clearSlides(final String expression) {
        if ("has no more".equals(expression) || "has no".equals(expression)) {
            this.presentationConfigurationTestContext
                    .withSlideId("slide-01")
                    .clearSlides()
                    .assertHasNoSlides();
        } else {
            this.presentationConfigurationTestContext.assertHasSlides();
        }
    }

    @Then("^there is no (previous|next) slide$")
    public void noMoreSiblingSlide(final String position) {
        if ("previous".equals(position)) {
            this.presentationConfigurationTestContext.assertPreviousSlideIs("none");
        } else {
            this.presentationConfigurationTestContext.assertNextSlideIs("none");
        }
    }

    @Then("^the (first|last) slide is \"([^\"]+)\"$")
    public void firstAndLastSlide(final String position, final String expectedSlideId) {
        if ("first".equals(position)) {
            this.presentationConfigurationTestContext.assertFirstSlide(expectedSlideId);
        } else {
            this.presentationConfigurationTestContext.assertLastSlide(expectedSlideId);
        }
    }
}
