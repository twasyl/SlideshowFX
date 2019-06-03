package gradle.cucumber;

import com.twasyl.slideshowfx.engine.Variable;
import com.twasyl.slideshowfx.engine.context.TemplateConfigurationTestContext;
import com.twasyl.slideshowfx.engine.template.configuration.SlideTemplate;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import gradle.cucumber.types.SlideTemplatesAndSlideElementTemplatesMapping;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;

public class TemplateConfigurationTest extends AbstractConfigurationTest {
    TemplateConfigurationTestContext templateConfigurationTestContext;

    @Before
    public void before() {
        changeJavaTmpDirForTests();
        this.templateConfigurationTestContext = new TemplateConfigurationTestContext();
    }

    @After
    public void after() {
        this.templateConfigurationTestContext.clean();
        resetJavaTmpDirForTests();
    }

    @Given("^a template$")
    public void loadTemplate() {
        this.templateConfigurationTestContext.loadTemplate();
    }

    @When("^a valid template configuration is loaded$")
    public void loadValidConfiguration() {
        this.templateConfigurationTestContext.loadValidConfiguration();
    }

    @Then("the following field(s) is/are defined in the template configuration file")
    public void theFieldIsEqualTo(final Map<String, String> mapping) {
        mapping.entrySet()
                .stream()
                .filter(entry -> !"Name".equals(entry.getKey()))
                .forEach(entry -> this.templateConfigurationTestContext.assertFieldEquals(entry.getKey(), entry.getValue()));
    }

    @Then("this/these default variable(s) is/are defined")
    public void theseDefaultVariablesAreDefined(final List<Variable> expectedDefaultVariables) {
        this.templateConfigurationTestContext.assertNumberOfDefaultVariables(expectedDefaultVariables.size());
        expectedDefaultVariables.forEach(this.templateConfigurationTestContext::assertHasDefaultVariable);
    }

    @Then("this/these slide template(s) exist(s)")
    public void slideTemplatesExist(final List<SlideTemplate> slideTemplates) {
        this.templateConfigurationTestContext.assertNumberOfSlideTemplates(slideTemplates.size());

        slideTemplates.forEach(slideTemplate -> {
            assertAll("Assertions of slide template " + slideTemplate.getId() + " have failed",
                    () -> this.templateConfigurationTestContext.withSlideTemplateId(slideTemplate.getId()),
                    () -> this.templateConfigurationTestContext.assertSlideTemplateName(slideTemplate.getName()),
                    () -> this.templateConfigurationTestContext.assertSlideTemplateFile(slideTemplate.getFile()));
        });
    }

    @Then("the slide template(s) has/have the following element(s)")
    public void slideTemplateHasSlideElementTemplates(final SlideTemplatesAndSlideElementTemplatesMapping mapping) {
        mapping.keySet().forEach(slideTemplateId -> {
            this.templateConfigurationTestContext.withSlideTemplateId(slideTemplateId)
                    .assertNumberOfSlideElementTemplates(mapping.get(slideTemplateId).size());

            mapping.get(slideTemplateId).forEach(element -> assertAll(
                    "Assertions of slide element templates for slide template " + slideTemplateId + " have failed",
                    () -> this.templateConfigurationTestContext.withSlideElementTemplateId(element.getId()),
                    () -> this.templateConfigurationTestContext.assertSlideElementHtmlId(element.getHtmlId()),
                    () -> this.templateConfigurationTestContext.assertSlideElementDefaultContent(element.getDefaultContent())));
        });
    }
}