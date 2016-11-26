package cl.duoc.android.ubicacion;

import android.location.Location;

public class Utilidades {

    /**
     * Retorna la distancia en metros entre la ubicación 1 y 2
     *
     * @param ubicacion1
     * @param ubicacion2
     * @return
     */
    public static float calcularDistancia(Location ubicacion1, Location ubicacion2){
        return ubicacion1.distanceTo(ubicacion2);
    }

}
