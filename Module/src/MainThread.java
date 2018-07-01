import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainThread
{ //Hilo principal
    public static void main(String[] args)
    {
        int posicion=0;
        MainThread mainThread = new MainThread();
        posicion=mainThread.leerHilillos("Module\\Hilillos\\0.txt",posicion); //Leer cada archivo
        posicion=mainThread.leerHilillos("Module\\Hilillos\\1.txt",posicion);
        posicion=mainThread.leerHilillos("Module\\Hilillos\\2.txt",posicion);
        posicion=mainThread.leerHilillos("Module\\Hilillos\\3.txt",posicion);
        posicion=mainThread.leerHilillos("Module\\Hilillos\\4.txt",posicion);
        posicion=mainThread.leerHilillos("Module\\Hilillos\\5.txt",posicion);
        //mainThread.empezar();
        mainThread.prueba();
    }

    private int[] memoriaPrincipalDatos;
    private int[] memoriaPrincipalInstrucciones;
    public static ArrayList<Contexto> contextoList; //Lista con logica de round-robin
    private boolean busDatos=true;
    private boolean busInstrucciones=true; //Los buses inician desocupados
    private ArrayList<BloqueCacheDatos> cacheDatosNucleo0;
    private ArrayList<BloqueCacheDatos> cacheDatosNucleo1;
    private ArrayList<BloqueCacheInstrucciones> cacheInstruccionesNucleo0;
    private ArrayList<BloqueCacheInstrucciones> cacheInstruccionesNucleo1; //Caches para ambos nucleos y sus partes
    private Nucleo N0, N1; //Los dos nucleos
    public static Semaphore semaforo, semauxforo; //Semaforos necesarios para la barrera
    public static Lock[] candadosN0, candadosN1; //Candados para cada bloque de cache en ambos nucleos
    public static int enBarrera, tic, modo; //Hilos esperando en la barrera - Tics del reloj - Ejecucion lenta o rapida
    public static int reloj = 0; //Por donde va el reloj
    public static int quantum = 1000; //Cantidad de quantum que tiene cada hilillo
    private static boolean rapido = true; //Si el modo rapido esta activado

    //Constructor que inicializa los elementos de la simulacion
    public MainThread() {
        memoriaPrincipalDatos = new int[96];
        memoriaPrincipalInstrucciones = new int[640]; //Reserva el espacio para la memoria principal

        for (int i = 0; i < memoriaPrincipalInstrucciones.length; i++)
            memoriaPrincipalInstrucciones[i] = 1; //Inicializa ambas memorias con unos
        for (int i = 0; i < memoriaPrincipalDatos.length; i++)
            memoriaPrincipalDatos[i] = 1; //Inicializa ambas memorias con unos
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

        //Se inicializar cada nucleo con su respectiva informacion
        //Nucleo(miCache, otroCache, miCacheIns, memoriaPrincipalInstrucciones, memoriaPrincipalDatos, busDatos, busInstrucciones, numero)
        N0 = new Nucleo(cacheDatosNucleo0,cacheDatosNucleo1,cacheInstruccionesNucleo0,memoriaPrincipalInstrucciones,memoriaPrincipalDatos,busDatos,busInstrucciones,0);
        N1 = new Nucleo(cacheDatosNucleo1,cacheDatosNucleo0,cacheInstruccionesNucleo1,memoriaPrincipalInstrucciones,memoriaPrincipalDatos,busDatos,busInstrucciones,1);
        semaforo = new Semaphore(0); //Semaforo para pasar la barrera, empieza sin permisos
        semauxforo = new Semaphore(1); //Semaforo para tocar la variable enBarrera
        enBarrera = 0; //Ningun hilo empieza en barrera
        tic = 0; //Empzamos en el tic 0
        candadosN0 = new Lock[4];
        candadosN1 = new Lock[4]; //Inicializa ambos arreglos de candados
        for(int i = 0; i < 4; i++)
            candadosN0[i] = new ReentrantLock(); //Se hacen reentrant para que un hilo no se bloquee a si mismo
        for(int i = 0; i < 4; i++)
            candadosN1[i] = new ReentrantLock();
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
    private void empezar(){
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
        N0 = new Nucleo(cacheDatosNucleo0,cacheDatosNucleo1,cacheInstruccionesNucleo0,memoriaPrincipalInstrucciones,memoriaPrincipalDatos,busDatos,busInstrucciones,0);
        N1 = new Nucleo(cacheDatosNucleo1,cacheDatosNucleo0,cacheInstruccionesNucleo1,memoriaPrincipalInstrucciones,memoriaPrincipalDatos,busDatos,busInstrucciones,1);
        N0.setContexto(contextoList.get(0)); //El nucleo 0 empezara por el primero de la lista
        N1.setContexto(contextoList.get(1)); //El nucleo 1 empezara por el segundo de la lista
        contextoList.remove(0); //Quita de la lista al primero
        contextoList.remove(0); //Quita de la lista al que antes estaba de segundo
        N0.start();
        N1.start(); //Empiezan a correr ambos nucleos
    }

    private void prueba(){
        N0 = new Nucleo(cacheDatosNucleo0,cacheDatosNucleo1,cacheInstruccionesNucleo0,memoriaPrincipalInstrucciones,memoriaPrincipalDatos,busDatos,busInstrucciones,0);
        N1 = new Nucleo(cacheDatosNucleo1,cacheDatosNucleo0,cacheInstruccionesNucleo1,memoriaPrincipalInstrucciones,memoriaPrincipalDatos,busDatos,busInstrucciones,1);
        N0.setContexto(contextoList.get(0));
        N1.setContexto(contextoList.get(1));
        contextoList.remove(0);
        contextoList.remove(0);
        N0.start();
        N1.start();
    }

    //Obtiene una instruccion en memoria para lo que se le manda de donde empieza
    public int[] getInstructionFromMem(int memPosition){
        int[] instruction = new int[4];
        for (int i = 0; i < 4; i++)
            instruction[i] = memoriaPrincipalInstrucciones[memPosition++];
        return instruction;
    }

    //Obtiene una instruccion en cache para lo que se le manda de donde empieza
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

    //Metodo para imprimir el estado actual del sistema
    public void imprimirEstado(){
        System.out.println("--- MEMORIA PRINCIPAL ---");
        System.out.println(" -- Memoria de datos -- ");
        int posMemDatos = 0; //Posicion que se esta recorriendo
        for(int i = 0; i < 24; i++) //Tiene 24 bloques en total
        {
            String bloque = "Bloque " + i + ":  | "; //String de la linea de cada bloque
            for(int j = 0; j < 4; j++, posMemDatos++)
            {
                bloque = bloque + memoriaPrincipalDatos[posMemDatos] + " | "; //Incluye cada posicion del bloque
            }
            System.out.println(bloque); //Imprime el renglon de este bloque
        }
        System.out.println(" -- Memoria de intrucciones -- ");
        int posMemInst = 0; //Posicion que se esta recorriendo
        for(int i = 0; i < 40; i++) //Tiene 40 bloques en total
        {
            String bloque = "Bloque " + i + ":"; //String de varias lineas
            for(int j = 0; j < 4; j++) //Cada bloque tiene 4 instrucciones
            {
                bloque = bloque + "\n\tInstruccion " + j + ": | "; //Cada instruccion es una linea
                for(int k = 0; k < 4; k++, posMemInst++) //Cada instrucciones tiene 4 numeros
                {
                    bloque = bloque + memoriaPrincipalInstrucciones[posMemInst] + " | "; //Incluye el numero actual
                }
            }
            System.out.println(bloque); //Imprime el string de varios renglones del bloque actual
        }

        //Aqui se usan dos metodos auxiliares para evitar repetir codigo
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

    //Imprimir el estado actual de una cache de datos especifica que recibe por parametro
    private void imprimirCacheDatos(ArrayList<BloqueCacheDatos> nucleo) {
        BloqueCacheDatos bloqueDatosAux; //Bloque auxiliar para ir recorriendo la cache
        int[] palabrasDatos; //Vector para almacenar las palabras
        for(int i = 0; i < nucleo.size(); i++) //Recorre toda la cache
        {
            bloqueDatosAux = nucleo.get(i); //Recorrer bloque por bloque
            palabrasDatos = bloqueDatosAux.getPalabras(); //Obtiene las palabras del bloque actual
            String bloque = "Bloque " + i + ": | "
                            + palabrasDatos[0] + " | "
                            + palabrasDatos[1] + " | "
                            + palabrasDatos[2] + " | "
                            + palabrasDatos[3] + " | "; //Monta la linea con la informacion
            System.out.println(bloque + bloqueDatosAux.getEtiqueta() + " | " + bloqueDatosAux.getEstado() + " |");
        }
    }

    //Imprimir el estado actual de una cache de instrucciones especifica que recibe por parametro
    private void imprimirCacheInstrucciones(ArrayList<BloqueCacheInstrucciones> nucleo) {
        BloqueCacheInstrucciones bloqueInsAux; //Bloque auxiliar para ir recorriendo la cache
        int[][] palabrasInst; //Vector para almacenar las instrucciones y sus palabras
        for(int i = 0; i < nucleo.size(); i++) //Recorre toda la cache
        {
            bloqueInsAux = nucleo.get(i); //Recorrer bloque por bloque
            palabrasInst = bloqueInsAux.getInstrucciones(); //Obtiene las instrucciones del bloque actual
            String bloque = "Bloque " + i + ":"; //Un renglon para indicar cual bloque es
            System.out.println(bloque);
            System.out.println("Etiqueta: " + bloqueInsAux.getEtiqueta()); //Imprime el hilillo al que pertenece
            for(int j = 0; j < 4; j++) //Recorrer las 4 instrucciones
            {
                bloque = "\tInstruccion " + j + ": | "
                         + palabrasInst[j][0] + " | "
                         + palabrasInst[j][1] + " | "
                         + palabrasInst[j][2] + " | "
                         + palabrasInst[j][3] + " | "; //Monta la linea de cada instrucciones con sus 4 numeros
                System.out.println(bloque);
            }
        }
    }
}
