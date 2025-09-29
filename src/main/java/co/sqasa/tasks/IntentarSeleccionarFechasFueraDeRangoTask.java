package co.sqasa.tasks;

import co.sqasa.interactions.EntrarAlFrameDelCalendarioInteraction;
import co.sqasa.interactions.IntentarClickEnFechaFueraDeRangoInteraction;
import co.sqasa.userInterfaces.PaginaDatepickerUI;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class IntentarSeleccionarFechasFueraDeRangoTask implements Task {

    private final String token;

    public IntentarSeleccionarFechasFueraDeRangoTask(String token) {
        this.token = token;
    }


    public static IntentarSeleccionarFechasFueraDeRangoTask fechaFueraDeRango(String token){
        return instrumented(IntentarSeleccionarFechasFueraDeRangoTask.class, token);
    }
    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                EntrarAlFrameDelCalendarioInteraction.now(),
                Click.on(PaginaDatepickerUI.BTN_DATE),
                IntentarClickEnFechaFueraDeRangoInteraction.desde(token)
        );
    }

}
