import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Nucleo extends Thread
{ //Clase que representa un nucleo de ejecucion, usan un hilo
    private int numNucleo; //Si este nucleo es el 0 o el 1
    private ArrayList<BloqueCacheDatos> miCache; //Cache de datos propia usada durante el procesamiento
    private ArrayList<BloqueCacheDatos> otroCache; //Cache del otro nucleo cuando hace falta cambiar un estado
    private ArrayList<BloqueCacheInstrucciones> miCacheIns; //Cache de instrucciones propia para el procesamiento
    private int[] memoriaPrincipalInstrucciones; //Memoria compartida de instrucciones
    private int[] memoriaPrincipalDatos; //Memoria compartida de datos
    private Semaphore semaphoreMiCache = new Semaphore(1); //Semaforo que se le asigna el candado para la posicion que ocupa
    private Semaphore semaphoreOtroCache = new Semaphore(1); //Para el candado de la otra cache cuando hay que bloquear
    private Contexto context; //Contexto del hilillo que se va a procesar
    private int huboFallo = 0;  //Numero que se marca en 40 cuando hay un fallo
    private int quantum; //Quantum restante del hilillo actual
    private boolean terminado = false; //Booleano que indica si se llego a la instruccion final del hilillo

    //Constructor que prepara el nucleo en su totalidad para procesar
    public Nucleo(ArrayList<BloqueCacheDatos> miCache, ArrayList<BloqueCacheDatos> otroCache, ArrayList<BloqueCacheInstrucciones> miCacheIns, int[] memoriaPrincipalInstrucciones,
                  int[] memoriaPrincipalDatos, boolean busDatos, boolean busInstrucciones, int numero){
        this.miCache = miCache;
        this.otroCache = otroCache;
        this.miCacheIns = miCacheIns;
        this.memoriaPrincipalInstrucciones = memoriaPrincipalInstrucciones;
        this.memoriaPrincipalDatos = memoriaPrincipalDatos;
        this.numNucleo = numero;
        this.context = new Contexto();
    }

    //Metodo que se llama cuando se hace Nucleo.start()
    public void run() {
        procesar();
    }

    //Barrera en la que ambos hilos se encuentran en cada tic del reloj
    private void Barrera(){
        try {
            MainThread.semauxforo.acquire(); //Semaforo para evitar race conditions con la variable enBarrera
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MainThread.enBarrera++; //Hay uno mas esperando en la barrera
        if(MainThread.enBarrera == 2) //Verifica si ya son dos o es el primer hilo en entrar
        {
            MainThread.semauxforo.release(); //Se termina la seccion critica con enBarrera
            MainThread.enBarrera = 0; //Al ser 2 ya pueden pasar, entonces se devuelve a 0
            MainThread.semaforo.release(1); //Se da un permiso para que el otro pase
            System.out.println("Reloj: " + MainThread.reloj + " Nucleo: " + numNucleo + " Hilo: " + context.getID());
            MainThread.reloj++; //Ha pasado un tic
            quantum--; //Se ha gastado uno de quantum
        }
        else
        {
            MainThread.semauxforo.release(); //Se termina la seccion critica con enBarrera
            synchronized (MainThread.semaforo)
            {
                try
                {
                    MainThread.semaforo.acquire(); //Espera hasta que llegue el otro
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        check_thread_state();
    }

    //Se llama cuando no es necesario pasar por la barrera pues solo queda un hilo en ejecucion
    private void Pasar(){
        MainThread.reloj++;
        quantum--;
        check_thread_state();
    }

    //Metodo para cuidar el procesamiento rapido o lento y si hay que pasar por la barrera
    private void procesar() {
        this.quantum = MainThread.quantum; //Quantum restante
        while ( !terminado && quantum > 0) { //Si no ha terminado y todavia tiene quantum
            if (huboFallo < 1) { //Por si todavia quedan ciclos que deba pasar en fallo
                if(!MainThread.rapido){
                    try{
                        sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                resolverInstruccion(siguienteInstruccion());
            } else huboFallo--; //Un ciclo en fallo menos
            if (MainThread.hilillos_completados < 5) { //Verifica si todavia el otro nucleo tiene trabajo
                Barrera();
            } else Pasar(); //Esta solo, entonces puede pasar de tic libremente
        }
    }


    private void check_thread_state(){
        if (terminado) { //Si ya se corrio el finalizar del hilillo
            MainThread.contextosCompletados.add(context); //Agrega el contexto a la lista de terminados
            if (MainThread.contextoList.size() > 0 ){ //Si todavia hay contextos por ejecutar
                setContexto(MainThread.contextoList.get(0)); //Obtiene el primero en la cola round-robin
                MainThread.contextoList.remove(context); //Lo desencola
                huboFallo = 0;
                terminado = false;
                procesar();
            } else { //Va a quedar el otro nucleo solo, por lo que concedemos permisos en los semaforos
                MainThread.semauxforo.release();
                MainThread.semaforo.release(1);
            }
        }
        if (quantum < 1) { //Se quedo sin quantum
            MainThread.contextoList.add(context); //Vuelve a encolar el contexto
            setContexto(MainThread.contextoList.get(0)); //Cambio de contexto, trae el que sigue
            MainThread.contextoList.remove(context); //Desencola
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

    //Determina que hace la instruccion actual
    private void resolverInstruccion(int[] ir){
        switch (ir[0]){ //Revisa el primer numero
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
                //System.out.println("\n\n ----- Nucleo " + numNucleo + " termino hilillo " + context.getID() + "------\n");
                break;
            default:
                //Hubo fallo en cache de instrucciones
                break;
        }
    }

    //Hacer una suma con constante
    private void daddi(int[] ir){
        int valor = context.getRegistro(ir[1])+ir[3];
        context.setRegistro(ir[2],valor);
    }

    //Hacer una suma entre registros
    private void dadd(int[] ir){
        int valor = context.getRegistro(ir[1])+context.getRegistro(ir[2]);
        context.setRegistro(ir[3],valor);
    }

    //Realizar una resta
    private void dsub(int[] ir){
        int valor = context.getRegistro(ir[1])-context.getRegistro(ir[2]);
        context.setRegistro(ir[3],valor);
    }

    //Realizar una multiplicacion
    private void dmul(int[] ir){
        int valor = context.getRegistro(ir[1])*context.getRegistro(ir[2]);
        context.setRegistro(ir[3],valor);
    }

    //Realizar una division
    private void ddiv(int[] ir){
        int valor = context.getRegistro(ir[1])/context.getRegistro(ir[2]);
        context.setRegistro(ir[3],valor);
    }

    //Branch EQual Zero
    private void beqz(int[] ir){
        if(context.getRegistro(ir[1])==0)
            context.setPC(context.getPC()+(4*ir[3]));
    }

    //Branch Not Equal Zero
    private void bnez(int[] ir){
        if(context.getRegistro(ir[1])!=0)
            context.setPC(context.getPC()+(4*ir[3]));
    }

    //Jump And Link
    private void jal(int[] ir){
        context.setRegistro(31,context.getPC());
        context.setPC(context.getPC()+ir[3]);
    }

    //Jump Register
    private void jr(int[] ir){
        context.setPC(ir[1]);
    }

    //Resolver un load
    private void LW(int rf, int rd, int inm) {
        int dir_mem = context.getRegistro(rf) + inm; //Numero en el registro + inmediato
        int num_bloque = dir_mem / 16; //Bloque en memoria principal
        int pos_cache = num_bloque % 4; //Bloque en cache
        int num_palabra = ( dir_mem - ( num_bloque * 16 ) ) / 4;
        ResultadoFalloCahe falloCahe = new ResultadoFalloCahe();
        BloqueCacheDatos target = verifyCacheDatos( pos_cache, num_bloque, numNucleo);
        if (target.getEtiqueta() > -1 && target.getEstado() < 2 ) {
            context.setRegistro( rd, target.getPalabras()[num_palabra]);
        } else falloCahe = falloCacheLw(num_bloque,num_palabra,pos_cache);
        if (huboFallo > 0) { //Si hubo fallo
            if (falloCahe.seLogro) {
                context.setRegistro(rd, falloCahe.resultado);
            } else
                context.setPC(context.getPC() - 4); //No consiguio algo, se devuelve una instruccion para volver a empezar
        }
    }

    //Caso de fallo de cache en un LW
    private ResultadoFalloCahe falloCacheLw(int bloque, int palabra, int posicionEnCache){
        huboFallo = 40; //Ciclos que debe pasar en fallo
        quantum += 40;
        if(numNucleo==0){ //Asigna los semaforos respectivamente segun sea el nucleo 0 o 1
            semaphoreMiCache=MainThread.candadosN0[posicionEnCache];
            semaphoreOtroCache=MainThread.candadosN1[posicionEnCache];
        }else {
            semaphoreMiCache=MainThread.candadosN1[posicionEnCache];
            semaphoreOtroCache=MainThread.candadosN0[posicionEnCache];
        }
        ResultadoFalloCahe resultado = new ResultadoFalloCahe();
        if(MainThread.busDatos){ //Bus de datos disponible
            MainThread.busDatos = false; //Lo marca ocupado
            if(miCache.get(posicionEnCache).getEstado() == 1){ //1 es modificado
                guardarBloqueEnMemoria(miCache.get(posicionEnCache).getEtiqueta(), true);
            }
            try{ //Intenta bloquear la posicion en la otra cache
                semaphoreOtroCache.acquire();
                if(otroCache.get(posicionEnCache).getEtiqueta()==bloque && otroCache.get(posicionEnCache).getEstado() == 1/*estado 1 es modificado*/){
                    guardarBloqueEnMemoria(otroCache.get(posicionEnCache).getEtiqueta(),false);
                    otroCache.get(posicionEnCache).setEtiqueta(0);
                }
            }
            catch(InterruptedException e){resultado.setSeLogro(false);}finally {semaphoreOtroCache.release();}
            copiarBloqueDesdeMemoria(bloque);
            resultado.setSeLogro(true); //Logra resolver el fallo
            resultado.setResultado(miCache.get(posicionEnCache).getPalabras()[palabra]);
        }
        else {resultado.setSeLogro(false);}
        MainThread.busDatos =true; //Desocupa el bus
        return resultado;
    }

    //Resolver un SW
    private void SW(int rd, int rf , int inm){
        int dir_mem = context.getRegistro(rd) + inm; //Numero en registro + inmediato
        int num_bloque = dir_mem / 16; //Bloque en memoria principal
        int pos_cache = num_bloque % 4; //Bloque en cache
        int num_palabra = ( dir_mem - ( num_bloque * 16 ) ) / 4;
        boolean pudoRealizarse = true;
        BloqueCacheDatos target = verifyCacheDatos( pos_cache, num_bloque, numNucleo);
        if (target.getEtiqueta() == -1 || target.getEstado() == 2 ) {//2 es invalido
            pudoRealizarse = falloCacheSw(num_bloque, num_palabra, pos_cache);
            target = verifyCacheDatos( pos_cache, num_bloque, numNucleo);
        }
        if(pudoRealizarse){
            if (target.getEtiqueta() != 1){
                try{ //Intenta bloquear la posicion en la otra cache
                    semaphoreOtroCache.acquire();
                    if(otroCache.get(pos_cache).getEtiqueta() == num_bloque && otroCache.get(pos_cache).getEstado() == 0){
                        otroCache.get(pos_cache).setEstado(2);
                    }
                }
                catch(InterruptedException e){}finally {semaphoreOtroCache.release();}
            }
            int[] palabras = target.getPalabras();
            palabras[num_palabra] = context.getRegistro(rf);
            target.setPalabras(palabras);
            target.setEstado(1); //Lo marca modificado
        } else context.setPC(context.getPC()-4);
    }

    //Caso de fallo de cache en un SW
    private boolean falloCacheSw(int bloque, int palabra, int posicionEnCache){
        huboFallo = 40; //Ciclos que debe pasar en fallo
        quantum += 40;
        boolean resultado=false;
        if(MainThread.busDatos){ //Si el bus de datos esta desocupado
            MainThread.busDatos = false; //Lo ocupa
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
            catch(InterruptedException e){}finally {semaphoreOtroCache.release();}
            resultado=true;
        }
        copiarBloqueDesdeMemoria(bloque);
        MainThread.busDatos =true; //Desocupado el bus
        return resultado;
    }

    private BloqueCacheDatos verifyCacheDatos( int posicion, int numBloque, int idNucleo ){
        BloqueCacheDatos invalid = new BloqueCacheDatos();
        BloqueCacheDatos target = idNucleo == 0 ? miCache.get(posicion) : otroCache.get(posicion);
        if ( target.getEtiqueta() == numBloque ) return target;
        return invalid;
    }

    //Guarda un bloque en la memoria principal
    private void guardarBloqueEnMemoria(int bloque, boolean esMiCache){
        int posicionBloqueMemoria = bloque*16 / 4;
        int posicionBloqueCache = bloque%4;
        int[] palabras;
        if(esMiCache) {
            palabras = miCache.get(posicionBloqueCache).getPalabras();
            miCache.get(posicionBloqueCache).setEstado(0);//0 es compartido
        }else{
            palabras = otroCache.get(posicionBloqueCache).getPalabras();
        }
        for(int i = 0; i < 4; i++) { //Escribe el bloque en memoria
            memoriaPrincipalDatos[posicionBloqueMemoria] = palabras[0];
            memoriaPrincipalDatos[posicionBloqueMemoria+1] = palabras[1];
            memoriaPrincipalDatos[posicionBloqueMemoria+2] = palabras[2];
            memoriaPrincipalDatos[posicionBloqueMemoria+3] = palabras[3];
        }
    }

    //Se trae un bloque desde la memoria principal
    private void copiarBloqueDesdeMemoria(int bloque){
        int posicionBloqueMemoria = bloque*16 / 4;
        int posicionBloqueCache = bloque%4;
        int[] palabras = new int[4];
        palabras[0] = memoriaPrincipalDatos[posicionBloqueMemoria]; //Trae el bloque
        palabras[1] = memoriaPrincipalDatos[posicionBloqueMemoria+1];
        palabras[2] = memoriaPrincipalDatos[posicionBloqueMemoria+2];
        palabras[3] = memoriaPrincipalDatos[posicionBloqueMemoria+3];
        miCache.get(posicionBloqueCache).setEstado(0);//0 es compartido
        miCache.get(posicionBloqueCache).setPalabras(palabras);
        miCache.get(posicionBloqueCache).setEtiqueta(bloque);

    }

    //Busca la siguiente instruccion en cache, y si no esta, la busca en el otro nucleo y/o trae de memoria principal
    private int[] siguienteInstruccion(){
        int num_bloque = context.getPC() / 16; //Bloque en memoria principal
        int pos_cache = num_bloque % 4; //Bloque en la cache
        int num_palabra = ( context.getPC() - ( num_bloque * 16 ) ) / 4;
        int[] result = {-1,-1,-1,-1};
        //try{
        //semaphoreMiCache.acquire();
        if(miCacheIns.get(pos_cache).getEtiqueta()==num_bloque){
            result=miCacheIns.get(pos_cache).getPalabra(num_palabra);
            context.setPC(context.getPC()+4);
        }
        else{ //fallo cache de instrucciones
            huboFallo = 40; //Ciclos en fallo
            quantum += 40;
            if(MainThread.busInstrucciones){ //Bus de instrucciones disponible
                //try{
                //semaphoreOtroCache.acquire();
                MainThread.busInstrucciones = false; //Lo ocupa
                int[][] instrucciones = new int[4][4];
                for(int i=0; i<4; i++){ //Mueve el bloque
                    instrucciones[0][i] = memoriaPrincipalInstrucciones[num_bloque*16+i];
                    instrucciones[1][i] = memoriaPrincipalInstrucciones[num_bloque*16+i+4];
                    instrucciones[2][i] = memoriaPrincipalInstrucciones[num_bloque*16+i+8];
                    instrucciones[3][i] = memoriaPrincipalInstrucciones[num_bloque*16+i+12];
                }
                miCacheIns.get(pos_cache).setInstrucciones(instrucciones);
                miCacheIns.get(pos_cache).setEtiqueta(num_bloque);
                //}catch (InterruptedException e) { }finally {semaphoreOtroCache.release();}
                MainThread.busInstrucciones = true; //Desocupa el bus
            }
        }
        //} catch (InterruptedException e) {

        //}finally {semaphoreMiCache.release();}
        return result;
    }

    //Clase auxiliar para fallos de cache
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

