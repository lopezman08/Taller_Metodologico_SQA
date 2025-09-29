package co.sqasa.interactions;

import co.sqasa.userInterfaces.PaginaDatepickerUI;
import co.sqasa.utils.DeterminarFechasFueraDeRangoUtil;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class IntentarClickEnFechaFueraDeRangoInteraction implements Interaction {

    /** Resultado del intento de clic en el calendario. */
    public enum ResultadoClick { HABILITADO_CLICKEADO, DESHABILITADO_BLOQUEADO }

    /** Token con la expresión de fecha, p.ej. "D-21" o "M+1,D+10". */
    private final String tokenFecha;

    public static IntentarClickEnFechaFueraDeRangoInteraction desde(String tokenFecha) {
        return instrumented(IntentarClickEnFechaFueraDeRangoInteraction.class, tokenFecha);
    }

    public IntentarClickEnFechaFueraDeRangoInteraction(String tokenFecha) {
        this.tokenFecha = tokenFecha;
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();

        // 1) Calcular fecha objetivo y guardar contexto mínimo
        LocalDate fechaObjetivo = DeterminarFechasFueraDeRangoUtil.TraerFechaLimite(tokenFecha);
        actor.remember("fechaObjetivo", fechaObjetivo);

        String valorInicial = PaginaDatepickerUI.BTN_DATE.resolveFor(actor).getValue();
        actor.remember("initialDateStr", valorInicial == null ? "" : valorInicial);

        // 2) Navegar hasta que en el encabezado del datepicker aparezcan el mes/año de la fecha objetivo
        navegarHastaMesYAnio(driver, fechaObjetivo.getMonthValue(), fechaObjetivo.getYear());

        // 3) Intentar el clic sobre el día (habilitado o deshabilitado)
        int diaObjetivo = fechaObjetivo.getDayOfMonth();

        // Días deshabilitados: <td class="ui-state-disabled"><span class="ui-state-default">DIA</span></td>
        By diaDeshabilitado = By.xpath(
                "//table[contains(@class,'ui-datepicker-calendar')]//td[contains(@class,'ui-state-disabled')]" +
                        "[.//span[@class='ui-state-default' and normalize-space()='" + diaObjetivo + "']]"
        );

        // Días habilitados: <td><a>DIA</a></td>
        By diaHabilitado = By.xpath(
                "//table[contains(@class,'ui-datepicker-calendar')]//td[not(contains(@class,'ui-state-disabled'))]" +
                        "//a[normalize-space()='" + diaObjetivo + "']"
        );

        List<WebElement> deshabilitados = driver.findElements(diaDeshabilitado);
        if (!deshabilitados.isEmpty()) {
            WebElement celda = deshabilitados.get(0);
            try {
                celda.findElement(By.cssSelector("span.ui-state-default")).click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", celda);
            }
            actor.remember("clickOutcome", ResultadoClick.DESHABILITADO_BLOQUEADO);
            return;
        }

        List<WebElement> habilitados = driver.findElements(diaHabilitado);
        if (!habilitados.isEmpty()) {
            habilitados.get(0).click(); // Esto sí escribe en el input
            actor.remember("clickOutcome", ResultadoClick.HABILITADO_CLICKEADO);
            return;
        }

        throw new IllegalStateException(
                "No se encontró el día " + diaObjetivo +
                        " en el mes visible tras navegar. Revisa selectores o la navegación."
        );
    }

    /** Navega con los botones «prev/next» hasta que coincidan [mes, año] del encabezado. */
    private void navegarHastaMesYAnio(WebDriver driver, int mesObjetivo, int anioObjetivo) {
        By lblMes  = By.cssSelector(".ui-datepicker-month");
        By lblAnio = By.cssSelector(".ui-datepicker-year");
        By btnPrev = By.cssSelector(".ui-datepicker-prev");
        By btnNext = By.cssSelector(".ui-datepicker-next");

        int intentos = 0;
        while (intentos++ < 24) { // tope de 2 años para evitar bucles infinitos
            String textoMes = driver.findElement(lblMes).getText().trim();    // «octubre» / «October»
            int mesActual   = numeroDeMes(textoMes);
            int anioActual  = Integer.parseInt(driver.findElement(lblAnio).getText().trim());

            if (mesActual == mesObjetivo && anioActual == anioObjetivo) return;

            boolean irAAnterior = (anioActual > anioObjetivo) ||
                    (anioActual == anioObjetivo && mesActual > mesObjetivo);
            driver.findElement(irAAnterior ? btnPrev : btnNext).click();
        }
        throw new IllegalStateException("No fue posible llegar a " + mesObjetivo + "/" + anioObjetivo + " con prev/next.");
    }

    /** Convierte el nombre del mes (ES/EN, largo o corto) a número 1..12 sin usar switch. */
    private int numeroDeMes(String nombreMes) {
        String normalizado = nombreMes.toLowerCase(Locale.ROOT);

        // Nombres completos
        for (int m = 1; m <= 12; m++) {
            Month month = Month.of(m);
            String en = month.getDisplayName(TextStyle.FULL,  Locale.ENGLISH).toLowerCase(Locale.ROOT);
            String es = month.getDisplayName(TextStyle.FULL,  new Locale("es")).toLowerCase(Locale.ROOT);
            if (normalizado.equals(en) || normalizado.equals(es)) return m;
        }
        // Abreviaturas
        for (int m = 1; m <= 12; m++) {
            Month month = Month.of(m);
            String en = month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toLowerCase(Locale.ROOT);
            String es = month.getDisplayName(TextStyle.SHORT, new Locale("es")).toLowerCase(Locale.ROOT);
            if (normalizado.equals(en) || normalizado.equals(es)) return m;
        }
        // Fallback: devolver mes actual si no se reconoce (evita NPE pero deja pista en logs si quisieras)
        return LocalDate.now().getMonthValue();
    }
}
