import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainThread
{
    public static void main(String[] args)
    {
        MainThread mainThread = new MainThread();
        int posicion=0;
        posicion=mainThread.leerHilillos("C:\\Users\\fpand\\IdeaProjects\\AoKuang\\Module\\Hilillos\\0.txt",posicion);
        posicion=mainThread.leerHilillos("C:\\Users\\fpand\\IdeaProjects\\AoKuang\\Module\\Hilillos\\1.txt",posicion);
        posicion=mainThread.leerHilillos("C:\\Users\\fpand\\IdeaProjects\\AoKuang\\Module\\Hilillos\\2.txt",posicion);
        posicion=mainThread.leerHilillos("C:\\Users\\fpand\\IdeaProjects\\AoKuang\\Module\\Hilillos\\3.txt",posicion);
        posicion=mainThread.leerHilillos("C:\\Users\\fpand\\IdeaProjects\\AoKuang\\Module\\Hilillos\\4.txt",posicion);
        posicion=mainThread.leerHilillos("C:\\Users\\fpand\\IdeaProjects\\AoKuang\\Module\\Hilillos\\5.txt",posicion);
        mainThread.empezar();
    }

    private int[] memoriaPrincipalDatos;
    private int[] memoriaPrincipalInstrucciones;
    private List<Contexto> contextoList;
    private boolean busDatos=true;
    private boolean busInstrucciones=true;
    private List<BloqueCacheDatos> cacheDatosNucleo0;
    private List<BloqueCacheDatos> cacheDatosNucleo1;
    private List<BloqueCacheInstrucciones> cacheInstruccionesNucleo0;
    private List<BloqueCacheInstrucciones> cacheInstruccionesNucleo1;
    private Nucleo N0;
    private BloqueCacheDatos invalid = new BloqueCacheDatos(); //Bloque de cache default para retornar en caso de fallo

    public MainThread() {
        memoriaPrincipalDatos = new int[96];
        memoriaPrincipalInstrucciones = new int[640];

        for (int i = 0; i < memoriaPrincipalInstrucciones.length; i++)
            memoriaPrincipalInstrucciones[i] = 1;
        for (int i = 0; i < memoriaPrincipalDatos.length; i++)
            memoriaPrincipalDatos[i] = 1;
        contextoList = new ArrayList<Contexto>();
        cacheDatosNucleo0 = new ArrayList<BloqueCacheDatos>();
        cacheDatosNucleo1 = new ArrayList<BloqueCacheDatos>();
        cacheInstruccionesNucleo0 = new ArrayList<BloqueCacheInstrucciones>();
        cacheInstruccionesNucleo1 = new ArrayList<BloqueCacheInstrucciones>();
        //initCacheDatos(); //para inicializar cache datos con valores manualmente
        for (int i = 0; i < 4; i++) {
            BloqueCacheInstrucciones bloqueIns1 = new BloqueCacheInstrucciones();
            BloqueCacheInstrucciones bloqueIns2 = new BloqueCacheInstrucciones();
            BloqueCacheDatos bloqueData1 = new BloqueCacheDatos();
            BloqueCacheDatos bloqueData2 = new BloqueCacheDatos();
            cacheInstruccionesNucleo0.add(bloqueIns1);
            cacheInstruccionesNucleo1.add(bloqueIns2);
            cacheDatosNucleo0.add(bloqueData1);
            cacheDatosNucleo1.add(bloqueData2);
        }
        N0 = new Nucleo(0, this);
    }

    private int leerHilillos (String ruta, int posicionMemInstr){
        Contexto contexto = new Contexto();
        contexto.setPC(posicionMemInstr);
        contexto.setID(Integer.parseInt(""+ruta.charAt(ruta.length() - 5)));
        try {
            BufferedReader br = new BufferedReader(new FileReader(ruta));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                String[] data = line.split(" ");
                for(int i = 0; i < data.length ; i++){
                    memoriaPrincipalInstrucciones[posicionMemInstr]=Integer.parseInt(data[i]);
                    posicionMemInstr++;
                }
                line = br.readLine();
            }
            String everything = sb.toString();
            System.out.print(everything);
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        contextoList.add(contexto);
        return posicionMemInstr;
    }

    private void empezar(){
        N0.procesar(contextoList.get(1));
    }

    public int[] getInstructionFromMem(int memPosition){
        int[] instruction = new int[4];
        for (int i = 0; i < 4; i++)
            instruction[i] = memoriaPrincipalInstrucciones[memPosition++];
        return instruction;
    }

    public int[] getInstructionFromCache(int memPosition){
        int[] instruction = new int[4];
        for (int i = 0; i < 4; i++)
            instruction[i] = memoriaPrincipalInstrucciones[memPosition++];
        return instruction;
    }

    public boolean verifyCacheInstructionsCore0( int posicion, int numBloque ) {
        BloqueCacheInstrucciones target = cacheInstruccionesNucleo0.get(posicion);
        System.out.print(posicion + "   " + numBloque + "   " + target.getEtiqueta() );
        if ( target.getEtiqueta() == numBloque ) return true;
        return false;
    }

    public BloqueCacheDatos verifyCacheDatos( int posicion, int numBloque, int idNucleo ){
        BloqueCacheDatos target = idNucleo == 0 ? cacheDatosNucleo0.get(posicion) : cacheDatosNucleo1.get(posicion);
        System.out.print(posicion + "   " + numBloque + "   " + target.getEtiqueta() );
        if ( target.getEtiqueta() == numBloque ) return target;
        return invalid;
    }

    public void loadToCacheInstFromMem(int memPosition){
        BloqueCacheInstrucciones newBloque = new BloqueCacheInstrucciones();
        int numBloque = memPosition / 16;
        int posBloque = memPosition % 16;
        newBloque.setEtiqueta(numBloque);
        int[][] instructionSet = new int[4][4];
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++) {
                instructionSet[i][j] = memoriaPrincipalInstrucciones[memPosition];
            }
        cacheInstruccionesNucleo0.add( posBloque , newBloque);
    }

    /* Crea bloques manualmente y los agrega a cache instrucciones */
    /*public void initCacheDatos(){
        BloqueCacheDatos bl1 = new BloqueCacheDatos(new int[]{4,12,-8,4}, 0, 1);
        BloqueCacheDatos bl2 = new BloqueCacheDatos(new int[]{6,31,0,-2}, 5, 0);
        BloqueCacheDatos bl3 = new BloqueCacheDatos(new int[]{14,-2,6,9}, 22, 0);
        BloqueCacheDatos bl4 = new BloqueCacheDatos(new int[]{3,13,-4,9}, 19, 2);
        cacheDatosNucleo0.add(bl1);
        cacheDatosNucleo0.add(bl2);
        cacheDatosNucleo0.add(bl3);
        cacheDatosNucleo0.add(bl4);
    }*/
}
