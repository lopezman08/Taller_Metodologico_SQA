package co.sqasa.stepDefinitions;

import co.sqasa.interactions.IntentarClickEnFechaFueraDeRangoInteraction;
import co.sqasa.questions.ObtenerValorCampoDateQuestion;
import co.sqasa.tasks.AbrirPaginaDatepickerTask;
import co.sqasa.tasks.IntentarSeleccionarFechasFueraDeRangoTask;
import co.sqasa.tasks.SeleccionarMenuFechasRestringidasTask;
import io.cucumber.java.es.*;
import net.serenitybdd.screenplay.actors.OnStage;
import static org.hamcrest.Matchers.*;
import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;

public class DatepickerStepDefinition {

    @Dado("que el usuario abre la seccion Datepicker en la pagina de jQuery")
    public void queElUsuarioAbreLaSeccionDatepickerEnLaPaginaDeJQuery() {
        OnStage.theActorCalled("oscar").attemptsTo(
                AbrirPaginaDatepickerTask.page());
    }
    @Dado("navega a la opcion Restrict date range")
    public void navegaALaOpcionRestrictDateRange() {
        OnStage.theActorCalled("oscar").attemptsTo(
                SeleccionarMenuFechasRestringidasTask.irALaOpcionRestrictDateRange());
    }
    @Cuando("intenta seleccionar la fecha {string}")
    public void intentaSeleccionarLaFecha(String token) {
        OnStage.theActorCalled("oscar").attemptsTo(
                IntentarSeleccionarFechasFueraDeRangoTask.fechaFueraDeRango(token));
    }
    @Entonces("el día correspondiente aparece deshabilitado en el calendario")
    public void elDíaCorrespondienteApareceDeshabilitadoEnElCalendario() {
        OnStage.theActorCalled("oscar").should(seeThat(
                actor -> actor.recall("clickOutcome"),
                is(IntentarClickEnFechaFueraDeRangoInteraction.ResultadoClick.DESHABILITADO_BLOQUEADO)
        ));
    }
    @Entonces("el campo de fecha permanece sin cambios")
    public void elCampoDeFechaPermaneceSinCambios() {
        String initial = OnStage.theActorCalled("oscar").recall("initialDateStr");
        OnStage.theActorCalled("oscar").should(
                seeThat(ObtenerValorCampoDateQuestion.ultimo(), equalTo(initial)));
    }

}
