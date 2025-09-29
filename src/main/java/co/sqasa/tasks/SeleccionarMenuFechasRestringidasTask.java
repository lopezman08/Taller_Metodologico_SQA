package co.sqasa.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;

import static co.sqasa.userinterfaces.PaginaDatepickerUI.BTN_RESTRICT_DATE_RANGE;
import static net.serenitybdd.screenplay.Tasks.instrumented;

public class SeleccionarMenuFechasRestringidasTask implements Task {

    public static SeleccionarMenuFechasRestringidasTask irALaOpcionRestrictDateRange(){
        return instrumented(SeleccionarMenuFechasRestringidasTask.class);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                Click.on(BTN_RESTRICT_DATE_RANGE)
        );
    }
}
