package co.sqasa.interactions;

import co.sqasa.userInterfaces.PaginaDatepickerUI;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class EntrarAlFrameDelCalendarioInteraction implements Interaction {

    public static EntrarAlFrameDelCalendarioInteraction now(){
        return instrumented(EntrarAlFrameDelCalendarioInteraction.class);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        WebElement frame = PaginaDatepickerUI.FRAME_CALENDARIO.resolveFor(actor);
        driver.switchTo().frame(frame);
    }
}
