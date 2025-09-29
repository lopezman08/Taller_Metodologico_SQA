#language: es

  Característica: Seleccionar una fecha en el calendario emergente
    Yo como usuario quiero seleccionar un fecha fuera del rango establecido

    Antecedentes:
      Dado que el usuario abre la seccion Datepicker en la pagina de jQuery
      Y navega a la opcion Restrict date range

    Esquema del escenario: Intentar seleccionar una fecha fuera del rango
      Cuando intenta seleccionar la fecha "<fechaInvalida>"
      Entonces el día correspondiente aparece deshabilitado en el calendario
      Y el campo de fecha permanece sin cambios

      Ejemplos:
        | fechaInvalida |
        | D-21          |
        | M+1,D+11      |

