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
        contexto.setPC(posicionMemInstr); //Su primera instruccion esta donde se empieza a guardar
        contexto.setID(Integer.parseInt(""+ruta.charAt(ruta.length() - 5)));
        try {
            BufferedReader br = new BufferedReader(new FileReader(ruta));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine(); //Lee un reglon = una instruccion

            while (line != null) { //Mientras haya instrucciones
                String[] data = line.split(" "); //Cada numero va separado por espacios
                for(int i = 0; i < data.length ; i++){ //Cada numero va en una posicion de memoria
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
        contextoList.add(contexto); //Agrega el conetxto a la cola
        return posicionMemInstr; //Retorna donde termina que es donde va a empezar el proximo hilillo
    }

    //Metodo con las primeras interacciones para el usuario
    private void empezar() throws InterruptedException {
        System.out.println("Digite el numero segun el modo que desea:\n1. Rapido\n2. Lento");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); //Para leer de consola
        String entrada = null;
        try {
            entrada = br.readLine(); //Lee lo que digite el usuario
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            modo = Integer.parseInt(entrada); //Intenta convertirlo en entero
        }catch(NumberFormatException nfe){
            System.err.println("Formato invalido");
            empezar(); //Si el formato esta mal, vuelve a solicitar el numero
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
            default: //El formato es valido, pero no es un numero solicitado
                System.err.println("Debe digitar 1 o 2");
                empezar();
                break;
        }

        System.out.println("Digite el valor para el quantum que desea:");
        BufferedReader br2 = new BufferedReader(new InputStreamReader(System.in)); //Lee de consola nuevamente
        String entradaQuantum = null;
        try {
            entradaQuantum = br2.readLine(); //Lee el valor
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            quantum = Integer.parseInt(entradaQuantum); //Convierte a entero
        }catch(NumberFormatException nfe){
            System.err.println("Formato invalido");
            empezar();
        }

        //imprimirEstado();
        N0.setContexto(contextoList.get(0)); //El nucleo 0 empezara por el primero de la lista
        N1.setContexto(contextoList.get(1)); //El nucleo 1 empezara por el primero de la lista
        contextoList.remove(0); //Quita de la lista al primero
        contextoList.remove(0); //Quita de la lista al que antes estaba de segundo
        N0.start(); //Empieza a correr el nucleo 0
        N1.start(); //Empieza a correr el nucleo 1
        N0.join(); //Volver a unir el hilo del nucleo 0 al principal
        N1.join(); //Volver a unir el hilo del nucleo 1 al principal
        imprimirEstado();
    }

    //Muestra el estado actual del sistema
    public void imprimirEstado(){
        System.out.println("--- MEMORIA PRINCIPAL ---");
        System.out.println(" -- Memoria de datos -- ");
        int posMemDatos = 0; //Posicion en memoria de datos
        for(int i = 0; i < 24; i++) //Tiene 24 bloques
        {
            String bloque = "Bloque " + i + ":  | ";
            for(int j = 0; j < 4; j++, posMemDatos++) //Cada bloque tiene 4 palabras
            {
                bloque = bloque + memoriaPrincipalDatos[posMemDatos] + " | ";
            }
            System.out.println(bloque); //Imprime un renglon correspondiente a este bloque
        }
        System.out.println(" -- Memoria de intrucciones -- ");
        int posMemInst = 0; //Posicion en memoria de instrucciones
        for(int i = 0; i < 40; i++) //Tiene 40 bloque en total
        {
            String bloque = "Bloque " + i + ":";
            for(int j = 0; j < 4; j++) //Cada bloque tiene 4 instrucciones
            {
                bloque = bloque + "\n\tInstruccion " + j + ": | ";
                for(int k = 0; k < 4; k++, posMemInst++) //Cada instruccion tiene 4 numeros
                {
                    bloque = bloque + memoriaPrincipalInstrucciones[posMemInst] + " | "; //TODO: Cambiar posMemInst por memoriaPrincipalInstrucciones[posMemInst]
                }
            }
            System.out.println(bloque); //Imprime string de multiples lineas de este bloque
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
        BloqueCacheDatos bloqueDatosAux; //Bloque de datos auxiliar
        int[] palabrasDatos; //Arreglo para las palabras del bloque
        for(int i = 0; i < nucleo.size(); i++) //Imprime cada bloque
        {
            bloqueDatosAux = nucleo.get(i);
            palabrasDatos = bloqueDatosAux.getPalabras();
            String bloque = "Bloque " + i + ": | "
                    + palabrasDatos[0] + " | "
                    + palabrasDatos[1] + " | "
                    + palabrasDatos[2] + " | "
                    + palabrasDatos[3] + " | "; //Arma el string del renglon de este bloque
            System.out.println(bloque + bloqueDatosAux.getEtiqueta() + " | " + bloqueDatosAux.getEstado() + " |");
        }
    }

    private void imprimirCacheInstrucciones(ArrayList<BloqueCacheInstrucciones> nucleo) {
        BloqueCacheInstrucciones bloqueInsAux; //Bloque de instrucciones auxiliar
        int[][] palabrasInst; //Matriz para las instrucciones de este bloque y sus numeros
        for(int i = 0; i < nucleo.size(); i++) //Explora cada bloque
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
                        + palabrasInst[j][3] + " | "; //Monta el renglon de esta instruccion
                System.out.println(bloque);
            }
        }
    }

    private void imprimirContextos(){
        //Imprimir contextos de los hilillos
        System.out.println("--- CONTEXTOS ---");
        Contexto contauxto; //Contexto auxiliar
        String strContexto;
        ArrayList<Contexto> listaTemporal = new ArrayList<Contexto>();

        if(contextoList.size() > 0){ //Si todavia hay hilillos esperando ejecutarse
            System.out.println(" -- Hilillos sin terminar -- ");
            listaTemporal = contextoList;
        }else{
            System.out.println(" -- Hilillos terminados -- ");
            listaTemporal = contextosCompletados;
        }

        for(int c = 0; c < listaTemporal.size(); c++){ //Por cada hilillo en la lista
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
