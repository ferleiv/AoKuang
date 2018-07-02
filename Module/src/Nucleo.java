import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Nucleo extends Thread
{   //Clase que representa un nucleo de ejecucion, usan un hilo
    private int numNucleo;
    private ArrayList<BloqueCacheDatos> miCache; //Cache de datos propia
    private ArrayList<BloqueCacheDatos> otroCache; //La cache del otro nucleo por si hace falta cambiar o bloquear algo
    private ArrayList<BloqueCacheInstrucciones> miCacheIns; //Cache de instrucciones propia para saber por donde va
    private int[] memoriaPrincipalInstrucciones; //Memoria para traer las instrucciones en los fallos
    private int[] memoriaPrincipalDatos; //Memoria compartida de los datos con el estado mas reciente
    //private boolean busDatos; //Bus de datos del sistema el cual solo esta ocupado o desocupado
    //private boolean busInstrucciones; //Bus de instrucciones del sistema que funciona similar
    private Semaphore semaphoreMiCache = new Semaphore(1);
    private Semaphore semaphoreOtroCache = new Semaphore(1);
    private Contexto context; //Contexto del hilillo que se va a procesar
    private int huboFallo = 0; //Numero que se marca en 40 cuando hay un fallo
    private int quantum; //Quantum restante del hilillo actual
    private boolean terminado = false; //Booleano que indica si se llego a la instruccion final del hilillo

    //Constructor de la clase
    public Nucleo(ArrayList<BloqueCacheDatos> miCache, ArrayList<BloqueCacheDatos> otroCache, ArrayList<BloqueCacheInstrucciones> miCacheIns, int[] memoriaPrincipalInstrucciones,
                  int[] memoriaPrincipalDatos, int numero){
        this.miCache = miCache;
        this.otroCache = otroCache;
        this.miCacheIns = miCacheIns;
        this.memoriaPrincipalInstrucciones = memoriaPrincipalInstrucciones;
        this.memoriaPrincipalDatos = memoriaPrincipalDatos;
        this.numNucleo = numero;
        this.context = new Contexto();
    }

    //Metodo que corre cuando a la instancia s le hace start()
    public void run() {
        procesar();
    }

    //Metodo de la barrera para sincronizar cada tic del reloj
    private void Barrera(){
        try {
            MainThread.semauxforo.acquire(); //Semaforo que evita un race condition con la variable enBarrera
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        MainThread.enBarrera++; //Hay uno mas esperando en la barrera
        if(MainThread.enBarrera == 2) //Verifica si ya son dos o es el primer hilo en entrar
        {
            MainThread.semauxforo.release();
            MainThread.enBarrera = 0;
            MainThread.semauxforo.release(); //Ya puede liberar la seccion critica con enBarrera
            MainThread.semaforo.release(1); //Concede un permiso para que el otro hilo tambien pueda pasar
            MainThread.reloj++; //Paso un tic
            quantum--; //Un tic menos de quantum restante
            Pasar(); //Puede pasar la barrera
        }
        else
        {
            MainThread.semauxforo.release(); //Libera seccion critica con enBarrera
            synchronized (MainThread.semaforo)
            {
                try
                {
                    MainThread.semaforo.acquire(); //Espera al otro hilo
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        check_thread_state();
    }
    //Metodo que unicamente se llama en Barrera cuando ambos hilos estan listos para pasar
    private void Pasar(){
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MainThread.reloj++;
        quantum--;
        check_thread_state();
    }

    //Metodo que se llama para vigilar si el hilillo no ha terminado y todavia tiene quantum para ser procesado
    private void procesar() {
        this.quantum = MainThread.quantum; //Toma el quantum restante
        while ( !terminado && quantum > 0) {//debe agregarse tambien el fin por quantum
            if (huboFallo < 1) { //No hay fallo
                resolverInstruccion(siguienteInstruccion());
            } else huboFallo--;
            if (MainThread.hilillos_completados < 5) {
                Barrera();
            } else Pasar();
        }
        Barrera();
    }

    //Metodo de cambio de contexto si el hilillo actual termino o se quedo sin quantum
    private void check_thread_state(){
        if (terminado) { //Hilillo llego a su instruccion final
            MainThread.contextosCompletados.add(context);
            if (MainThread.contextoList.size() > 0 ){ //Si todavia hay hililloss por ejecutar
                setContexto(MainThread.contextoList.get(0)); //Obtiene el primer contexto en la cola round-robin
                MainThread.contextoList.remove(context); //Quita el elemento de la cabeza
                huboFallo = 0;
                terminado = false;
                procesar();
            } else {
                MainThread.semauxforo.release();
                MainThread.semaforo.release(1);
            }
        }
        if (quantum < 1) { //Se le acabo el quantum
            MainThread.contextoList.add(context); //Lo agrega a la cola para continuarlo luego
            setContexto(MainThread.contextoList.get(0)); //Obtiene el primer contexto en la cola round-robin
            MainThread.contextoList.remove(context); //Quita el elemento de la cabeza
            huboFallo = 0;
            procesar();
        }
    }

    //Getter para el contexto del hilillo
    public Contexto getContexto(){
        return context;
    }

    //Setter para el contexto del hilillo
    public void setContexto(Contexto contexto) {
        context = contexto;
    }

    private void resolverInstruccion(int[] ir){
        /*if (/*numNucleo == 0 &&ir[0] != -1 && context.getID() == 4) {
            System.out.println("Nucleo: " + numNucleo + " Hilillo " + context.getID() + " instruccion: " + ir[0] + " | " + ir[1] + " | " + ir[2] + " | " + ir[3]);
        }*/
        switch (ir[0]){
            case 8: //8 = DADDI
                daddi(ir);
                break;
            case 32: //32 = DADD
                dadd(ir);
                break;
            case 34: //34 = DSUB
                dsub(ir);
                break;
            case 12: //12 = DMUL
                dmul(ir);
                break;
            case 14: //14 = DDIV
                ddiv(ir);
                break;
            case 4: //4 = BEQZ
                beqz(ir);
                break;
            case 5: //5 = BNEZ
                bnez(ir);
                break;
            case 3: //3 = JAL
                jal(ir);
                break;
            case 2: //2 = JR
                jr(ir);
                break;
            case 35: //35 = LW
                LW(ir[1],ir[2],ir[3]);
                break;
            case 43: //43 = SW
                SW(ir[1],ir[2],ir[3]);
                break;
            case 63: //63 = Terminar
                terminado = true;
                MainThread.hilillos_completados++;
                System.out.println("\n\n ----- Nucleo " + numNucleo + " termino hilillo " + context.getID() + "------\n");
                break;
            default:
                //Hubo fallo en cache de instrucciones
                break;
        }
    }

    //Metodo para resolver un DADDI
    private void daddi(int[] ir){
        int valor = context.getRegistro(ir[1])+ir[3];
        context.setRegistro(ir[2],valor);
    }

    //Metodo para resolver un DADD
    private void dadd(int[] ir){
        int valor = context.getRegistro(ir[1])+context.getRegistro(ir[2]);
        context.setRegistro(ir[3],valor);
    }

    //Metodo para resolver un DSUB
    private void dsub(int[] ir){
        int valor = context.getRegistro(ir[1])-context.getRegistro(ir[2]);
        context.setRegistro(ir[3],valor);
    }

    //Metodo para resolver un DMUL
    private void dmul(int[] ir){
        int valor = context.getRegistro(ir[1])*context.getRegistro(ir[2]);
        context.setRegistro(ir[3],valor);
    }

    //Metodo para resolver un DDIV
    private void ddiv(int[] ir){
        int valor = context.getRegistro(ir[1])/context.getRegistro(ir[2]);
        context.setRegistro(ir[3],valor);
    }

    //Metodo para resolver un BEGZ
    private void beqz(int[] ir){
        if(context.getRegistro(ir[1])==0)
            context.setPC(context.getPC()+(4*ir[3]));
    }

    //Metodo para resolver un BNEZ
    private void bnez(int[] ir){
        if(context.getRegistro(ir[1])!=0)
            context.setPC(context.getPC()+(4*ir[3]));
    }

    //Metodo para resolver un JAL
    private void jal(int[] ir){
        context.setRegistro(31,context.getPC());
        context.setPC(context.getPC()+ir[3]);
    }

    //Metodo para resolver un JR
    private void jr(int[] ir){
        context.setPC(ir[1]);
    }

    //Metodo para procesar un LW
    private void LW(int rf, int rd, int inm) {
        int dir_mem = context.getRegistro(rf) + inm;
        int num_bloque = dir_mem / 16;
        int pos_cache = num_bloque % 4;
        int num_palabra = ( dir_mem - ( num_bloque * 16 ) ) / 4;
        ResultadoFalloCahe falloCahe = new ResultadoFalloCahe();
        BloqueCacheDatos target = verifyCacheDatos( pos_cache, num_bloque, numNucleo);
        if (target.getEtiqueta() > -1 && target.getEstado() < 2 ) {
            context.setRegistro( rd, target.getPalabras()[num_palabra]);
        } else falloCahe = falloCacheLw(num_bloque,num_palabra,pos_cache);
        if (huboFallo > 0) {
            if (falloCahe.seLogro) {
                context.setRegistro(rd, falloCahe.resultado);
            } else
                context.setPC(context.getPC() - 4);//no consiguio algo, se devuelve una instruccion para volver a empezar
        }
    }

    private ResultadoFalloCahe falloCacheLw(int bloque, int palabra, int posicionEnCache){
        huboFallo = 40;
        quantum += 40;
        ResultadoFalloCahe resultado = new ResultadoFalloCahe();
        if(MainThread.busDatos){ //Bus de datos disponible
            MainThread.busDatos =false; //Ahora el bus esta ocupado
            if(miCache.get(posicionEnCache).getEstado()==1){//1 es modificado
                guardarBloqueEnMemoria(miCache.get(posicionEnCache).getEtiqueta(), true);
            }
            try{ //Intenta bloquear la posicion en la otra cache
                semaphoreOtroCache.acquire();
                if(otroCache.get(posicionEnCache).getEtiqueta()==bloque && otroCache.get(posicionEnCache).getEstado()==1/*asumiendo que estado 1 es modificado*/){
                    guardarBloqueEnMemoria(otroCache.get(posicionEnCache).getEtiqueta(),false);
                }
            }
            catch(InterruptedException e){resultado.setSeLogro(false);}finally {semaphoreOtroCache.release();}
            copiarBloqueDesdeMemoria(bloque);
            resultado.setSeLogro(true);
            resultado.setResultado(miCache.get(posicionEnCache).getPalabras()[palabra]);
        }
        else {resultado.setSeLogro(false);}
        MainThread.busDatos =true; //Desocupa el bus de datos
        return resultado;
    }

    //Metodo para procesar un SW
    private void SW(int rd, int rf , int inm){
        int dir_mem = context.getRegistro(rd) + inm;
        int num_bloque = dir_mem / 16;
        int pos_cache = num_bloque % 4;
        int num_palabra = ( dir_mem - ( num_bloque * 16 ) ) / 4;
        boolean pudoRealizarse = true;
        BloqueCacheDatos target = verifyCacheDatos( pos_cache, num_bloque, numNucleo);
        if (target.getEtiqueta() == -1 || target.getEstado() != 1 ) {//1 es modificado
            pudoRealizarse = falloCacheSw(num_bloque, num_palabra, pos_cache);
        }
        if(pudoRealizarse){
            int[] palabras = target.getPalabras();
            palabras[num_palabra] = context.getRegistro(rf);
            target.setPalabras(palabras);
            target.setEstado(1);
        }else context.setPC(context.getPC()-4); //Se devuelve una instruccion para volverlo a intentar
    }

    //Metodo que se ejecuta cuando ocurre un fallo de cache en un SW
    private boolean falloCacheSw(int bloque, int palabra, int posicionEnCache){
        huboFallo = 40;
        quantum += 40;
        boolean resultado=false;
        if(MainThread.busDatos){ //Bus de datos disponible
            MainThread.busDatos =false; //Bus ahora ocupado
            if(miCache.get(posicionEnCache).getEstado()==1){//1 es modificado
                guardarBloqueEnMemoria(miCache.get(posicionEnCache).getEtiqueta(), true);
            }
            try{ //Intenta bloquear la posicion en la otra cache
                semaphoreOtroCache.acquire();
                if(otroCache.get(posicionEnCache).getEtiqueta()==bloque){
                    if(otroCache.get(posicionEnCache).getEstado()==1)//1 es modificado
                        guardarBloqueEnMemoria(otroCache.get(posicionEnCache).getEtiqueta(),false);
                    otroCache.get(posicionEnCache).setEstado(2);
                }
            }
            catch(InterruptedException e){}finally {semaphoreOtroCache.release();} //Libera la posicion
            resultado=true;
        }
        MainThread.busDatos =true; //Libera el bus
        return resultado;
    }

    //Metodo que verifica si los datos de un hilillo estan en la cache propia o en la del otro nucleo
    private BloqueCacheDatos verifyCacheDatos( int posicion, int numBloque, int idNucleo ){
        BloqueCacheDatos invalid = new BloqueCacheDatos();
        BloqueCacheDatos target = idNucleo == 0 ? miCache.get(posicion) : otroCache.get(posicion);
        if ( target.getEtiqueta() == numBloque ) return target;
        return invalid;
    }

    //Almacenar el memoria principal el nuevo estado de un bloque de datos en cache
    private void guardarBloqueEnMemoria(int bloque, boolean esMiCache){
        int posicionBloqueMemoria = bloque*16 / 4;
        int posicionBloqueCache = bloque%4;
        int[] palabras;
        if(esMiCache) { //Esta en mi cache
            palabras = miCache.get(posicionBloqueCache).getPalabras();
            miCache.get(posicionBloqueCache).setEstado(0);//0 es compartido
            if(otroCache.get(posicionBloqueCache).getEtiqueta()==bloque)
                otroCache.get(posicionBloqueCache).setEstado(2);//2 es invalido
        }else{ //Esta en la cache del otro nucleo
            palabras = otroCache.get(posicionBloqueCache).getPalabras();
            otroCache.get(posicionBloqueCache).setEstado(0);//0 es compartido
            if(miCache.get(posicionBloqueCache).getEtiqueta()==bloque)
                miCache.get(posicionBloqueCache).setEstado(2);//2 es invalido
        }
        for(int i = 0; i < 4; i++) { //Guarda ya el bloque en memoria principal
            memoriaPrincipalDatos[posicionBloqueMemoria] = palabras[0];
            memoriaPrincipalDatos[posicionBloqueMemoria+1] = palabras[1];
            memoriaPrincipalDatos[posicionBloqueMemoria+2] = palabras[2];
            memoriaPrincipalDatos[posicionBloqueMemoria+3] = palabras[3];
        }
    }

    //Metodo para traer un bloque de datos desde la memoria principal
    private void copiarBloqueDesdeMemoria(int bloque){
        int posicionBloqueMemoria = bloque*16 / 4;
        int posicionBloqueCache = bloque%4;
        int[] palabras = new int[4];
        palabras[0] = memoriaPrincipalDatos[posicionBloqueMemoria]; //Empieza a copiaar los datos
        palabras[1] = memoriaPrincipalDatos[posicionBloqueMemoria+1];
        palabras[2] = memoriaPrincipalDatos[posicionBloqueMemoria+2];
        palabras[3] = memoriaPrincipalDatos[posicionBloqueMemoria+3];
        miCache.get(posicionBloqueCache).setEstado(0);//0 es compartido
        miCache.get(posicionBloqueCache).setPalabras(palabras);
        miCache.get(posicionBloqueCache).setEtiqueta(bloque);

    }

    //Busca la siguiente instruccion en cache, y si no esta, la busca en el otro nucleo y/o trae de memoria principal
    private int[] siguienteInstruccion(){
        int num_bloque = context.getPC() / 16;
        int pos_cache = num_bloque % 4;
        int num_palabra = ( context.getPC() - ( num_bloque * 16 ) ) / 4;
        int[] result = {-1,-1,-1,-1};
        //try{
        //semaphoreMiCache.acquire();
        if(miCacheIns.get(pos_cache).getEtiqueta()==num_bloque){
            result=miCacheIns.get(pos_cache).getPalabra(num_palabra);
            context.setPC(context.getPC()+4);
        }
        else{ //fallo cache de instrucciones
            huboFallo = 40;
            quantum += 40;
            if(MainThread.busInstrucciones){//Bus de instrucciones disponible
                //try{
                //semaphoreOtroCache.acquire();
                MainThread.busInstrucciones = false;//Intenta bloquear el bloque en la otra cache
                int[][] instrucciones = new int[4][4];
                for(int i=0; i<4; i++){//Copia las instrucciones

                    instrucciones[0][i] = memoriaPrincipalInstrucciones[num_bloque*16+i];
                    instrucciones[1][i] = memoriaPrincipalInstrucciones[num_bloque*16+i+4];
                    instrucciones[2][i] = memoriaPrincipalInstrucciones[num_bloque*16+i+8];
                    instrucciones[3][i] = memoriaPrincipalInstrucciones[num_bloque*16+i+12];
                }
                miCacheIns.get(pos_cache).setInstrucciones(instrucciones);
                miCacheIns.get(pos_cache).setEtiqueta(num_bloque);
                //}catch (InterruptedException e) { }finally {semaphoreOtroCache.release();}
                MainThread.busInstrucciones = true;
            }
        }
        //} catch (InterruptedException e) {

        //}finally {semaphoreMiCache.release();} //Bloque la cache propia
        return result;
    }

    //Pequeña clase auxiliar para verificar si hubo un fallo de cache
    private static class ResultadoFalloCahe{
        private int resultado = 0;
        private boolean seLogro = false;

        private ResultadoFalloCahe() {
        }

        private int getResultado() {
            return resultado;
        }

        private void setResultado(int resultado) {
            this.resultado = resultado;
        }

        private boolean isSeLogro() {
            return seLogro;
        }

        private void setSeLogro(boolean seLogro) {
            this.seLogro = seLogro;
        }
    }
}

