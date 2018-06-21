public class BloqueCacheInstrucciones {
    private int etiqueta;
    private int[][] instrucciones;

    public BloqueCacheInstrucciones() {
        this.etiqueta = -1;
        this.instrucciones = new int[4][4];
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                instrucciones[i][j]=0;
            }
        }
    }

    public int getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(int etiqueta) {
        this.etiqueta = etiqueta;
    }

    public int[][] getInstrucciones() {
        return instrucciones;
    }

    public void setInstrucciones(int[][] instrucciones) {
        this.instrucciones = instrucciones;
    }
}
