public class BloqueCacheInstrucciones {
    private int etiqueta; //Hilillo al que pertenece
    private int[][] instrucciones; //Instrucciones que incluye

    //Constructor de la clase que inicializa el bloque en 0
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

    public int[] getPalabra(int pos){
        return instrucciones[pos];
    }

    public void setPalabra(int pos, int palabra[]){
        instrucciones[pos]=palabra;
    }

    public void setInstrucciones(int[][] instrucciones) {
        this.instrucciones = instrucciones;
    }
}
