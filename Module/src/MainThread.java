import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainThread
{
    public static void main(String[] args)
    {
        int posicion=0;
        MainThread mainThread = new MainThread();
        posicion=mainThread.leerHilillos("Module\\Hilillos\\0.txt",posicion);
        posicion=mainThread.leerHilillos("Module\\Hilillos\\1.txt",posicion);
        posicion=mainThread.leerHilillos("Module\\Hilillos\\2.txt",posicion);
        posicion=mainThread.leerHilillos("Module\\Hilillos\\3.txt",posicion);
        posicion=mainThread.leerHilillos("Module\\Hilillos\\4.txt",posicion);
        posicion=mainThread.leerHilillos("Module\\Hilillos\\5.txt",posicion);
        mainThread.empezar();
    }

    private int[] memoriaPrincipalDatos;
    private int[] memoriaPrincipalInstrucciones;
    private ArrayList<Contexto> contextoList;
    private boolean busDatos=true;
    private boolean busInstrucciones=true;
    private ArrayList<BloqueCacheDatos> cacheDatosNucleo0;
    private ArrayList<BloqueCacheDatos> cacheDatosNucleo1;
    private ArrayList<BloqueCacheInstrucciones> cacheInstruccionesNucleo0;
    private ArrayList<BloqueCacheInstrucciones> cacheInstruccionesNucleo1;
    private Nucleo N0, N1;
    public static Semaphore semaforo, semauxforo;
    public static Lock[] candadosN0, candadosN1;
    public static Lock candado;
    public static int enBarrera, tic, modo;
    public static int reloj = 0;
    public static final int quantum = 1000;
    //public static int next_context = 2;
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

        //Nucleo(miCache,otroCache,miCacheIns,memoriaPrincipalInstrucciones,memoriaPrincipalDatos,busDatos,busInstrucciones,numero){
        N0 = new Nucleo(cacheDatosNucleo0,cacheDatosNucleo1,cacheInstruccionesNucleo0,memoriaPrincipalInstrucciones,memoriaPrincipalDatos,busDatos,busInstrucciones,0);
        N1 = new Nucleo(cacheDatosNucleo1,cacheDatosNucleo0,cacheInstruccionesNucleo1,memoriaPrincipalInstrucciones,memoriaPrincipalDatos,busDatos,busInstrucciones,1);
        semaforo = new Semaphore(1);
        semauxforo = new Semaphore(1);
        enBarrera = 0;
        tic = 0;
        candadosN0 = new Lock[4];
        candadosN1 = new Lock[4];
        for(int i = 0; i < 4; i++)
            candadosN0[i] = new ReentrantLock();
        for(int i = 0; i < 4; i++)
            candadosN1[i] = new ReentrantLock();
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
        System.out.println("Digite el numero segun el modo que desea:\n1. Rapido\n2. Lento");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String entrada = null;
        try {
            entrada = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            modo = Integer.parseInt(entrada);
        }catch(NumberFormatException nfe){
            System.err.println("Formato invalido");
            empezar();
        }
        switch (modo)
        {
            case 1:
                System.out.println("Modo rapido seleccionado");
                break;
            case 2:
                System.out.println("Modo lento seleccionado");
                break;
            default:
                System.err.println("Debe digitar 1 o 2");
                empezar();
                break;
        }
        imprimirEstado();
        N0 = new Nucleo(cacheDatosNucleo0,cacheDatosNucleo1,cacheInstruccionesNucleo0,memoriaPrincipalInstrucciones,memoriaPrincipalDatos,busDatos,busInstrucciones,0, contextoList.get(0));
        N1 = new Nucleo(cacheDatosNucleo1,cacheDatosNucleo0,cacheInstruccionesNucleo1,memoriaPrincipalInstrucciones,memoriaPrincipalDatos,busDatos,busInstrucciones,1, contextoList.get(1));
        contextoList.remove(0);
        contextoList.remove(0);
        N0.start();
        N1.start();
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

    public void imprimirEstado()
    {
        System.out.println("--- MEMORIA PRINCIPAL ---");
        System.out.println(" -- Memoria de datos -- ");
        int posMemDatos = 0;
        for(int i = 0; i < 24; i++)
        {
            String bloque = "Bloque " + i + ":  | ";
            for(int j = 0; j < 4; j++, posMemDatos++)
            {
                bloque = bloque + memoriaPrincipalDatos[posMemDatos] + " | "; //TODO: Cambiar posMemDatos por memoriaPrincipalDatos[posMemDatos]
            }
            System.out.println(bloque);
        }
        System.out.println(" -- Memoria de intrucciones -- ");
        int posMemInst = 0;
        for(int i = 0; i < 40; i++)
        {
            String bloque = "Bloque " + i + ":";
            for(int j = 0; j < 4; j++)
            {
                bloque = bloque + "\n\tInstruccion " + j + ": | ";
                for(int k = 0; k < 4; k++, posMemInst++)
                {
                    bloque = bloque + memoriaPrincipalInstrucciones[posMemInst] + " | "; //TODO: Cambiar posMemInst por memoriaPrincipalInstrucciones[posMemInst]
                }
            }
            System.out.println(bloque);
        }

        System.out.println("--- CACHES ---");
        System.out.println(" -- Cache de datos del nucleo 0 -- ");
        imprimirCacheDatos(cacheDatosNucleo0);
        System.out.println(" -- Cache de instrucciones del nucleo 0 -- ");
        imprimirCacheInstrucciones(cacheInstruccionesNucleo0);
        System.out.println(" -- Cache de datos del nucleo 1 -- ");
        imprimirCacheDatos(cacheDatosNucleo1);
        System.out.println(" -- Cache de instrucciones del nucleo 1 -- ");
        imprimirCacheInstrucciones(cacheInstruccionesNucleo1);
    }

    private void imprimirCacheDatos(ArrayList<BloqueCacheDatos> nucleo)
    {
        BloqueCacheDatos bloqueDatosAux;
        int[] palabrasDatos;
        for(int i = 0; i < nucleo.size(); i++)
        {
            bloqueDatosAux = nucleo.get(i);
            palabrasDatos = bloqueDatosAux.getPalabras();
            String bloque = "Bloque " + i + ": | "
                            + palabrasDatos[0] + " | "
                            + palabrasDatos[1] + " | "
                            + palabrasDatos[2] + " | "
                            + palabrasDatos[3] + " | ";
            System.out.println(bloque + bloqueDatosAux.getEtiqueta() + " | " + bloqueDatosAux.getEstado() + " |");
        }
    }

    private void imprimirCacheInstrucciones(ArrayList<BloqueCacheInstrucciones> nucleo)
    {
        BloqueCacheInstrucciones bloqueInsAux;
        int[][] palabrasInst;
        for(int i = 0; i < nucleo.size(); i++)
        {
            bloqueInsAux = nucleo.get(i);
            palabrasInst = bloqueInsAux.getInstrucciones();
            String bloque = "Bloque " + i + ":";
            System.out.println(bloque);
            System.out.println("Etiqueta: " + bloqueInsAux.getEtiqueta());
            for(int j = 0; j < 4; j++)
            {
                bloque = "\tInstruccion " + j + ": | "
                         + palabrasInst[j][0] + " | "
                         + palabrasInst[j][1] + " | "
                         + palabrasInst[j][2] + " | "
                         + palabrasInst[j][3] + " | ";
                System.out.println(bloque);
            }
        }
    }
}
