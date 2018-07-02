public class BloqueCacheInstrucciones {
    private int etiqueta; //Hilillo al que pertenece
    private int[][] instrucciones; //Instrucciones que incluye

    //Constructor de la clase que inicializa el bloque en 0 y el hilillo al que pertenece en -1 (ninguno todavia)
    public BloqueCacheInstrucciones() {
        this.etiqueta = -1;
        this.instrucciones = new int[4][4];
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                instrucciones[i][j]=0;
            }
        }
    }

    //Getter de la etiqueta
    public int getEtiqueta() {
        return etiqueta;
    }

    //Setter de la etiqueta
    public void setEtiqueta(int etiqueta) {
        this.etiqueta = etiqueta;
    }

    //Getter de las intrucciones
    public int[][] getInstrucciones() {
        return instrucciones;
    }

    //Getter de una instruccion en especifica
    public int[] getPalabra(int pos){
        return instrucciones[pos];
    }

    //Setter de una instruccion especifica
    public void setPalabra(int pos, int palabra[]){
        instrucciones[pos]=palabra;
    }

    //Setter de las instrucciones
    public void setInstrucciones(int[][] instrucciones) {
        this.instrucciones = instrucciones;
    }
}
