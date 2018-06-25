public class BloqueCacheDatos {
    private int etiqueta;
    private int[] palabras;
    private int estado; /* 0 = Compartido,
                           1 = Modificado,
                           2 = Invalido */

    public BloqueCacheDatos() {
        palabras = new int[4];
        for(int i = 0; i < 4; i++)
            palabras[i] = 0;
        etiqueta = -1;
        estado = 0;
    }

    /*Constructor para inicializar bloque del cacé de datos a partir de values*/
    /*public BloqueCacheDatos( int[] values, int etiq, int est ) {
        palabras = new int[4];
        for( int i = 0; i < 4; i++ )
            palabras[i] = values[i];
        etiqueta = etiq;
        estado = est;
    }*/

    public int getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(int etiqueta) {
        this.etiqueta = etiqueta;
    }

    public int[] getPalabras() {
        return palabras;
    }

    public void setPalabras(int[] palabras) {
        this.palabras = palabras;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }
}
