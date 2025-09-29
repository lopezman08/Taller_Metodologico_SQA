package co.sqasa.utils;

import java.time.LocalDate;

public class DeterminarFechasFueraDeRangoUtil {

    private DeterminarFechasFueraDeRangoUtil() {
    }

    public static LocalDate traerFechaLimite(String expr) {
        LocalDate fechaBase= LocalDate.now();
        if (expr == null) return fechaBase;

        var operaciones = expr.split(",");

        for (var o:
             operaciones) {

            var cantidad = Integer.parseInt(o.substring(1).replace("+",""));

            if(o.contains("D"))
                fechaBase = fechaBase.plusDays(cantidad);
            if(o.contains("M"))
                fechaBase = fechaBase.plusMonths(cantidad);
        }

        return fechaBase;
    }


}
