import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainThread
{ //Clase del hilo principal
    public static void main(String[] args) throws InterruptedException {
        int posicion=0;
        MainThread mainThread = new MainThread();
        posicion=mainThread.leerHilillos("Module\\Hilillos\\0.txt",posicion); //Leer cada archivo
        posicion=mainThread.leerHilillos("Module\\Hilillos\\1.txt",posicion);
        posicion=mainThread.leerHilillos("Module\\Hilillos\\2.txt",posicion);
        posicion=mainThread.leerHilillos("Module\\Hilillos\\3.txt",posicion);
        posicion=mainThread.leerHilillos("Module\\Hilillos\\4.txt",posicion);
        posicion=mainThread.leerHilillos("Module\\Hilillos\\5.txt",posicion);
        mainThread.empezar();
    }

    public static int hilillos_completados = 0; //Cantidad de hilillos que han terminado su ejecucion
    private int[] memoriaPrincipalDatos; //Memoria principal de datos
    private int[] memoriaPrincipalInstrucciones; //Memoria principal de instrucciones
    public static ArrayList<Contexto> contextoList; //Cola de contextos con logica round-robin
    public static ArrayList<Contexto> contextosCompletados; //Lista de contextos de los hilillos completados
    public static boolean busDatos=true; //Bus de datos
    public static boolean busInstrucciones=true; //Bus de instrucciones
    private ArrayList<BloqueCacheDatos> cacheDatosNucleo0; //Cache de datos para el nucleo 0
    private ArrayList<BloqueCacheDatos> cacheDatosNucleo1; //Cache de datos para el nucleo 1
    private ArrayList<BloqueCacheInstrucciones> cacheInstruccionesNucleo0; //Cache de instrucciones para el nucleo 0
    private ArrayList<BloqueCacheInstrucciones> cacheInstruccionesNucleo1; //Cache de instrucciones para el nucleo 1
    private Nucleo N0, N1; //Nucleos que van a correr
    public static Semaphore semaforo, semauxforo; //Semaforos para la barrera
    public static Semaphore[] candadosN0, candadosN1; //Candados para cada bloque en cada cache
    public static int enBarrera, tic, modo; //Hilis en barrera, tics, modo rapido o lento
    public static int reloj = 0; //Reloj
    public static int quantum = 1000; //Tamanyo del quantum
    public static boolean rapido = true; //Si es modo rapido o no

    //Constructor que inicializa los elementos de la simulacion
    public MainThread() {
        memoriaPrincipalDatos = new int[96];
        memoriaPrincipalInstrucciones = new int[640];

        for (int i = 0; i < memoriaPrincipalInstrucciones.length; i++)
            memoriaPrincipalInstrucciones[i] = 1; //Inicializa memorias con 1
        for (int i = 0; i < memoriaPrincipalDatos.length; i++)
            memoriaPrincipalDatos[i] = 1; //Inicializa memorias con 1
        contextoList = new ArrayList<Contexto>();
        contextosCompletados = new ArrayList<Contexto>();
        cacheDatosNucleo0 = new ArrayList<BloqueCacheDatos>();
        cacheDatosNucleo1 = new ArrayList<BloqueCacheDatos>();
        cacheInstruccionesNucleo0 = new ArrayList<BloqueCacheInstrucciones>();
        cacheInstruccionesNucleo1 = new ArrayList<BloqueCacheInstrucciones>();

        //Para inicializar caches
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

        //Nucleo(miCache, otroCache, miCacheIns, memoriaPrincipalInstrucciones, memoriaPrincipalDatos, busDatos, busInstrucciones, numero)
        N0 = new Nucleo(cacheDatosNucleo0,cacheDatosNucleo1,cacheInstruccionesNucleo0,memoriaPrincipalInstrucciones,memoriaPrincipalDatos,busDatos,busInstrucciones,0);
        N1 = new Nucleo(cacheDatosNucleo1,cacheDatosNucleo0,cacheInstruccionesNucleo1,memoriaPrincipalInstrucciones,memoriaPrincipalDatos,busDatos,busInstrucciones,1);
        semaforo = new Semaphore(0); //El semaforo de la barrera no empieza con permisos
        semauxforo = new Semaphore(1);
        enBarrera = 0; //No empiezan hilos en barrera
        tic = 0;
        candadosN0 = new Semaphore[4];
        candadosN1 = new Semaphore[4];
        for(int i = 0; i < 4; i++)
            candadosN0[i] = new Semaphore(1);
        for(int i = 0; i < 4; i++)
            candadosN1[i] = new Semaphore(1);
    }

    //Metodo para leer cada hilillo
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

    private void empezar() throws InterruptedException {
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
                rapido=false;
                break;
            default:
                System.err.println("Debe digitar 1 o 2");
                empezar();
                break;
        }

        System.out.println("Digite el valor para el quantum que desea:");
        BufferedReader br2 = new BufferedReader(new InputStreamReader(System.in));
        String entradaQuantum = null;
        try {
            entradaQuantum = br2.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            quantum = Integer.parseInt(entradaQuantum);
        }catch(NumberFormatException nfe){
            System.err.println("Formato invalido");
            empezar();
        }

        //imprimirEstado();
        N0.setContexto(contextoList.get(0));
        N1.setContexto(contextoList.get(1));
        contextoList.remove(0);
        contextoList.remove(0);
        N0.start();
        N1.start();
        N0.join();
        N1.join();
        imprimirEstado();
    }

    private void prueba() throws InterruptedException {
        N0.setContexto(contextoList.get(0));
        N1.setContexto(contextoList.get(1));
        contextoList.remove(0);
        contextoList.remove(0);
        N0.start();
        N1.start();
        N0.join();
        N1.join();
        System.out.print("Contextos completados: " + contextosCompletados.size() + "\n");
    }

    public void imprimirEstado(){
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
        imprimirContextos();
    }

    private void imprimirCacheDatos(ArrayList<BloqueCacheDatos> nucleo) {
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

    private void imprimirCacheInstrucciones(ArrayList<BloqueCacheInstrucciones> nucleo) {
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

    private void imprimirContextos(){
        //Imprimir contextos de los hilillos
        System.out.println("--- CONTEXTOS ---");
        Contexto contauxto;
        String strContexto;
        ArrayList<Contexto> listaTemporal = new ArrayList<Contexto>();

        if(contextoList.size() > 0){
            System.out.println(" -- Hilillos sin terminar -- ");
            listaTemporal = contextoList;
        }else{
            System.out.println(" -- Hilillos terminados -- ");
            listaTemporal = contextosCompletados;
        }

        for(int c = 0; c < listaTemporal.size(); c++){
            contauxto = listaTemporal.get(c);
            strContexto = "ID: " + contauxto.getID() + ". PC: " + contauxto.getPC() + ". Regs: | ";
            for(int r = 0; r < 32; r++)
            {
                strContexto = strContexto + contauxto.getRegistro(r) + " | ";
            }
            System.out.println(strContexto);
        }
    }

}
