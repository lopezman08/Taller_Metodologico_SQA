package co.sqasa.questions;

import co.sqasa.userInterfaces.PaginaDatepickerUI;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

public class ObtenerValorCampoDateQuestion implements Question<String> {

    public static ObtenerValorCampoDateQuestion ultimo(){
        return new ObtenerValorCampoDateQuestion();
    }
    @Override
    public String answeredBy(Actor actor) {
        return PaginaDatepickerUI.BTN_DATE.resolveFor(actor).getValue();
    }
}
