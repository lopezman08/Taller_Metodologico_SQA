package co.sqasa.questions;

import co.sqasa.userinterfaces.PaginaDatepickerUI;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

public class ObtenerValorCampoDateQuestion implements Question<String> {

    public static ObtenerValorCampoDateQuestion ultimo(){
        return new ObtenerValorCampoDateQuestion();
    }
    @Override
    public String answeredBy(Actor actor) {
        return PaginaDatepickerUI.CAMPO_FECHA.resolveFor(actor).getValue();
    }
}
