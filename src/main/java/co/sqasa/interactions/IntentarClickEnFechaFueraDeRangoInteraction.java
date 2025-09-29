package co.sqasa.interactions;

import co.sqasa.userinterfaces.PaginaDatepickerUI;
import co.sqasa.utils.DeterminarFechasFueraDeRangoUtil;
import net.serenitybdd.core.pages.WebElementFacade;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class IntentarClickEnFechaFueraDeRangoInteraction implements Interaction {

    public enum ResultadoClick { HABILITADO_CLICKEADO, DESHABILITADO_BLOQUEADO }

    private final String tokenFecha;

    public static IntentarClickEnFechaFueraDeRangoInteraction desde(String tokenFecha) {
        return instrumented(IntentarClickEnFechaFueraDeRangoInteraction.class, tokenFecha);
    }

    public IntentarClickEnFechaFueraDeRangoInteraction(String tokenFecha) {
        this.tokenFecha = tokenFecha;
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        LocalDate fechaObjetivo = DeterminarFechasFueraDeRangoUtil.traerFechaLimite(tokenFecha);
        actor.remember("fechaObjetivo", fechaObjetivo);

        String valorInicial = PaginaDatepickerUI.CAMPO_FECHA.resolveFor(actor).getValue();
        actor.remember("initialDateStr", valorInicial == null ? "" : valorInicial);

        navegarHastaMesYAnio(actor, fechaObjetivo.getMonthValue(), fechaObjetivo.getYear());

        String dia = String.valueOf(fechaObjetivo.getDayOfMonth());

        List<WebElementFacade> diasDeshab =
                PaginaDatepickerUI.DIA_DESHABILITADA.of(dia).resolveAllFor(actor);

        if (!diasDeshab.isEmpty()) {
            WebElementFacade span = diasDeshab.get(0);
            try {
                span.click();
            } catch (Exception e) {
                WebDriver drv = BrowseTheWeb.as(actor).getDriver();
                ((JavascriptExecutor) drv).executeScript("arguments[0].click();", span);
            }
            actor.remember("clickOutcome", ResultadoClick.DESHABILITADO_BLOQUEADO);
            return;
        }

        List<WebElementFacade> diasHab =
                PaginaDatepickerUI.DIA_HABILITADA.of(dia).resolveAllFor(actor);

        if (!diasHab.isEmpty()) {
            diasHab.get(0).click();
            actor.remember("clickOutcome", ResultadoClick.HABILITADO_CLICKEADO);
            return;
        }

        throw new IllegalStateException(
                "No se encontró el día " + dia + " en el mes visible tras navegar. Revisa selectores o la navegación."
        );
    }

    private void navegarHastaMesYAnio(Actor actor, int mesObjetivo, int anioObjetivo) {
        int intentos = 0;
        while (intentos++ < 3) {
            String textoMes = PaginaDatepickerUI.ETIQUETA_MES.resolveFor(actor).getText().trim();
            int mesActual   = numeroDeMes(textoMes);
            int anioActual  = Integer.parseInt(PaginaDatepickerUI.ETIQUETA_ANIO.resolveFor(actor).getText().trim());

            if (mesActual == mesObjetivo && anioActual == anioObjetivo) return;

            boolean irAAnterior = (anioActual > anioObjetivo) ||
                    (anioActual == anioObjetivo && mesActual > mesObjetivo);

            if (irAAnterior) {
                PaginaDatepickerUI.BTN_PREV.resolveFor(actor).click();
            } else {
                PaginaDatepickerUI.BTN_NEXT.resolveFor(actor).click();
            }
        }
        throw new IllegalStateException("No fue posible llegar a " + mesObjetivo + "/" + anioObjetivo + " con prev/next.");
    }

    private int numeroDeMes(String nombreMes) {
        String normalizado = nombreMes.toLowerCase(Locale.ROOT);

        for (int m = 1; m <= 12; m++) {
            Month month = Month.of(m);
            String en = month.getDisplayName(TextStyle.FULL,  Locale.ENGLISH).toLowerCase(Locale.ROOT);
            String es = month.getDisplayName(TextStyle.FULL,  new Locale("es")).toLowerCase(Locale.ROOT);
            if (normalizado.equals(en) || normalizado.equals(es)) return m;
        }
        for (int m = 1; m <= 12; m++) {
            Month month = Month.of(m);
            String en = month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toLowerCase(Locale.ROOT);
            String es = month.getDisplayName(TextStyle.SHORT, new Locale("es")).toLowerCase(Locale.ROOT);
            if (normalizado.equals(en) || normalizado.equals(es)) return m;
        }
        return LocalDate.now().getMonthValue();
    }
}
