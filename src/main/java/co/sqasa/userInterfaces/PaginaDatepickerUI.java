package co.sqasa.userInterfaces;

import net.serenitybdd.screenplay.targets.Target;
import org.openqa.selenium.By;

public class PaginaDatepickerUI {

    public static Target BTN_RESTRICT_DATE_RANGE = Target.the("Ir al menu de fechas restringidas")
            .locatedBy("//a[@href=\"/resources/demos/datepicker/min-max.html\"]");

    public static Target FRAME_CALENDARIO = Target.the("Acceder al frame")
            .locatedBy("//iframe[@class='demo-frame']");

    public static Target BTN_DATE = Target.the("Campo Date")
            .located(By.id("datepicker"));

    public static final Target DIAS_DESHABILITADOS = Target.the("días deshabilitados")
            .locatedBy(".ui-datepicker-calendar td.ui-datepicker-unselectable.ui-state-disabled");


}
