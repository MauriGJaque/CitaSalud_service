# language: es
Característica: Reserva de cita en línea 24/7

  Como paciente quiero reservar una cita en línea en cualquier momento del día
  para no tener que llamar durante mi horario de almuerzo ni acumular intentos fallidos.

  Antecedentes:
    Dado que existe un médico con franjas horarias disponibles

  Escenario: Reserva exitosa fuera de horario telefónico
    Dado que el paciente accede al sistema fuera del horario de atención telefónica
    Cuando elige médico, fecha y hora disponibles y confirma
    Entonces la cita queda registrada
    Y el paciente recibe confirmación por WhatsApp

  Escenario: Franja ya ocupada
    Dado que el paciente intenta seleccionar una franja ya ocupada
    Cuando intenta confirmarla
    Entonces el sistema la muestra como no disponible
    Y lo invita a elegir otra franja

  Escenario: Liberación automática de franja retenida
    Dado que el paciente selecciona una franja horaria pero no confirma la reserva
    Cuando transcurren 5 minutos sin confirmación
    Entonces la franja vuelve al estado DISPONIBLE
