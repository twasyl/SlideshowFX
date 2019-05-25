package gradle.cucumber;

import com.twasyl.slideshowfx.engine.context.TemplateConfigurationTestContext;
import cucumber.api.CucumberOptions;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@CucumberOptions()
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

    @Then("^the \"([^\"]*)\" is equal to \"([^\"]*)\" in the template configuration file$")
    public void theFieldIsEqualTo(final String fieldName, final String expectedValue) {
        templateConfigurationTestContext.assertFieldEquals(fieldName, expectedValue);
    }

    @Then("the template defines {int} default variable(s)")
    public void checkNumberOfVariables(int numberOfVariables) {
        this.templateConfigurationTestContext.assertNumberOfDefaultVariables(numberOfVariables);
    }

    @Then("^a default variable named \"([^\"]+)\" with the value \"([^\"]+)\" exists$")
    public void checkDefaultVariable(final String variableName, final String expectedValue) {
        this.templateConfigurationTestContext.assertDefaultVariable(variableName, expectedValue);
    }

    @Then("the template defines {int} slide(s)")
    public void checkNumberOfSlides(int numberOfSlides) {
        this.templateConfigurationTestContext.assertNumberOfSlideTemplates(numberOfSlides);
    }

    @Then("there is a slide template with id {int}")
    public void slideWithId(int slideTemplateId) {
        this.templateConfigurationTestContext
                .withSlideTemplateId(slideTemplateId)
                .assertSlideTemplateExists();
    }

    @And("^it's name is \"([^\"]+)\"$")
    public void slideHasName(String expectedName) {
        this.templateConfigurationTestContext.assertSlideTemplateName(expectedName);
    }

    @And("^it's file is \"([^\"]+)\"$")
    public void slideHasFile(final String expectedFile) {
        this.templateConfigurationTestContext.assertSlideTemplateFile(expectedFile);
    }

    @And("which defines {int} slide element(s)")
    public void slideHasSlideElements(final int expectedNumberOfSlideElements) {
        this.templateConfigurationTestContext.assertNumberOfSlideElements(expectedNumberOfSlideElements);
    }

    @Then("the slide template {int} has a template element with the id {int}")
    public void slideElementId(final int slideTemplateId, final int slideElementId) {
        this.templateConfigurationTestContext.withSlideTemplateId(slideTemplateId)
                .withSlideElementTemplateId(slideElementId)
                .assertSlideElementExists();
    }

    @And("^it's HTML id is \"([^\"]+)\"$")
    public void slideElementHtmlId(final String expectedHtmlId) {
        this.templateConfigurationTestContext.assertSlideElementHtmlId(expectedHtmlId);
    }

    @And("it's default content is \"([^\"]+)\"")
    public void slideElementContent(final String expectedDefaultContent) {
        this.templateConfigurationTestContext.assertSlideElementDefaultContent(expectedDefaultContent);
    }
}