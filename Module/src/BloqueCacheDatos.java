public class BloqueCacheDatos {
    private int etiqueta;
    private int[] palabras;
    private int estado;

    public BloqueCacheDatos() {
        palabras = new int[4];
        for(int i = 0; i < 4; i++)
            palabras[i] = 0;
        etiqueta = -1;
        estado = 0;
    }

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
