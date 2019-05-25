package gradle.cucumber;

import com.twasyl.slideshowfx.content.extension.Resource;
import com.twasyl.slideshowfx.engine.Variable;
import com.twasyl.slideshowfx.engine.context.PresentationConfigurationTestContext;
import com.twasyl.slideshowfx.engine.presentation.configuration.Slide;
import com.twasyl.slideshowfx.utils.beans.Pair;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import gradle.cucumber.types.SlidesAndSlideElementsMapping;

import java.util.List;

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

    @Given("^a presentation")
    public void loadPresentation() {
        this.presentationConfigurationTestContext.loadPresentation();
    }

    @When("^a valid presentation configuration is loaded$")
    public void loadValidConfiguration() {
        this.presentationConfigurationTestContext.loadValidConfiguration();
    }

    @Then("^the \"([^\"]*)\" is equal to \"([^\"]*)\" in the presentation configuration file$")
    public void theFieldIsEqualTo(final String fieldName, final String expectedValue) {
        this.presentationConfigurationTestContext.assertFieldEquals(fieldName, expectedValue);
    }

    @Then("the presentation configuration file contains {int} slide(s)")
    public void checkNumberOfSlides(int numberOfSlides) {
        this.presentationConfigurationTestContext.assertNumberOfSlides(numberOfSlides);
    }

    @Then("these slides exist")
    public void slideExists(final List<Slide> slides) {
        slides.forEach(slide -> assertAll("Assertions for slide " + slide.getId() + " have failed",
                () -> this.presentationConfigurationTestContext.withSlideId(slide.getId()),
                () -> this.presentationConfigurationTestContext.assertSlideNumber(slide.getSlideNumber()),
                () -> this.presentationConfigurationTestContext.assertSlideTemplateId(slide.getTemplate().getId())));
    }

    @Then("the slide(s) has/have the following elements")
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
        expectedVariables.forEach(this.presentationConfigurationTestContext::assertHasVariable);
    }
}
