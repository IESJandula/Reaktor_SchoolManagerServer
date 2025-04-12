package es.iesjandula.reaktor.school_manager_server.generator.models.enums;

/**
 * Enumerado con los días de la semana
 */
public enum Dia
{
    LUNES(0), MARTES(1), MIERCOLES(2), JUEVES(3), VIERNES(4);

    private int indice;

    Dia(int indice)
    {
        this.indice = indice;
    }

    /**
     * @return el índice del día
     */
    public int getIndice()
    {
        return this.indice;
    }
}
