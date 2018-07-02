public class BloqueCacheDatos {
    private int etiqueta; //Hilillo al que pertenece
    private int[] palabras; //Datos que contiene
    private int estado; /* 0 = Compartido,
                           1 = Modificado,
                           2 = Invalido */

    //Constructor que inicializa los datos y el estado en 0, y el hilillo al que pertenece en -1 (ninguno todavia)
    public BloqueCacheDatos() {
        palabras = new int[4];
        for(int i = 0; i < 4; i++)
            palabras[i] = 0;
        etiqueta = -1;
        estado = 0;
    }

    //Getter de la etiqueta
    public int getEtiqueta() {
        return etiqueta;
    }

    //Setter de la etiqueta
    public void setEtiqueta(int etiqueta) {
        this.etiqueta = etiqueta;
    }

    //Getter de los datos
    public int[] getPalabras() {
        return palabras;
    }

    //Setter de los datos
    public void setPalabras(int[] palabras) {
        this.palabras = palabras;
    }

    //Getter del estado
    public int getEstado() {
        return estado;
    }

    //Setter del estado
    public void setEstado(int estado) {
        this.estado = estado;
    }
}
