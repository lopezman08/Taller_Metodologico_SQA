package co.sqasa.runners;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "test/resources/features/SeleccionarFecha.feature",
        glue = {"co.sqasa.stepDefinitions","co.sqasa.hooks"},
        snippets = CucumberOptions.SnippetType.CAMELCASE
)
public class TestRunner {
}
