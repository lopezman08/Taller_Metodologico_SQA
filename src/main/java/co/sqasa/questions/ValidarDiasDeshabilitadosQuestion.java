package co.sqasa.questions;

import co.sqasa.userInterfaces.PaginaDatepickerUI;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

public class ValidarDiasDeshabilitadosQuestion implements Question<Boolean> {

    public static ValidarDiasDeshabilitadosQuestion enCalendario(){
        return new ValidarDiasDeshabilitadosQuestion();
    }

    @Override
    public Boolean answeredBy(Actor actor) {
        return !PaginaDatepickerUI.DIAS_DESHABILITADOS.resolveAllFor(actor).isEmpty();
    }
}
