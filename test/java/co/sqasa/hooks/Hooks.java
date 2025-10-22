package co.sqasa.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import net.serenitybdd.annotations.Managed;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;
import org.openqa.selenium.WebDriver;

public class Hooks {

    @Managed(driver = "chrome")
    WebDriver driver;

    @Before(order = 0)
    public void setTheStage(){
        OnStage.setTheStage(new OnlineCast());
        OnStage.theActorCalled("oscar").can(BrowseTheWeb.with(driver));
    }

    @After
    public void tearDown(){
        OnStage.drawTheCurtain();
    }

}
