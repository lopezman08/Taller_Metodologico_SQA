package co.sqasa.userinterfaces;

import net.serenitybdd.screenplay.targets.Target;
import org.openqa.selenium.By;

public final class PaginaDatepickerUI {

    private PaginaDatepickerUI() {

    }

    public static final Target BTN_RESTRICT_DATE_RANGE = Target.the("Ir al menu de fechas restringidas")
            .locatedBy("//a[@href=\"/resources/demos/datepicker/min-max.html\"]");

    public static final Target FRAME_CALENDARIO = Target.the("Acceder al frame")
            .locatedBy("//iframe[@class='demo-frame']");

    public static final Target CAMPO_FECHA = Target.the("Campo Date")
            .located(By.id("datepicker"));

    public static final Target ETIQUETA_MES  = Target.the("Etiqueta del mes visible")
            .locatedBy("//div[@id='ui-datepicker-div']//span[@class='ui-datepicker-month']");
    public static final Target ETIQUETA_ANIO = Target.the("Etiqueta del año visible")
            .locatedBy("//div[@id='ui-datepicker-div']//span[@class='ui-datepicker-year']");
    public static final Target BTN_PREV = Target.the("Botón mes anterior")
            .locatedBy("//a[contains(@class,'ui-datepicker-prev')]");
    public static final Target BTN_NEXT = Target.the("Botón mes siguiente")
            .locatedBy("//a[contains(@class,'ui-datepicker-next')]");

    public static final Target DIA_DESHABILITADA = Target.the("Día deshabilitado {0}")
            .locatedBy("//table[contains(@class,'ui-datepicker-calendar')]"
                    + "//td[contains(@class,'ui-state-disabled')]"
                    + "//span[@class='ui-state-default' and normalize-space()='{0}']");

    public static final Target DIA_HABILITADA = Target.the("Día habilitado {0}")
            .locatedBy("//table[contains(@class,'ui-datepicker-calendar')]"
                    + "//td[not(contains(@class,'ui-state-disabled'))]"
                    + "//a[normalize-space()='{0}']");

}
