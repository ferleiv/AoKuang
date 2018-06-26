import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Nucleo extends Thread
{
    private int numNucleo;

    private ArrayList<BloqueCacheDatos> miCache;
    private ArrayList<BloqueCacheDatos> otroCache;
    private ArrayList<BloqueCacheInstrucciones> miCacheIns;
    private int[] memoriaPrincipalInstrucciones;
    private int[] memoriaPrincipalDatos;
    private boolean busDatos;
    private boolean busInstrucciones;
    private Semaphore semaphoreMiCache = new Semaphore(1);
    private Semaphore semaphoreOtroCache = new Semaphore(1);
    private Contexto context;
    private int huboFallo = 0;

    public void run()
    {
        Barrera();
    }

    public void Barrera()
    {
        try {
            MainThread.aux.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MainThread.enBarrera++;
        if(MainThread.enBarrera == 2)
        {
            System.out.println("Ahora somos 2");
            MainThread.aux.release();
            MainThread.enBarrera = 0;
            MainThread.semaforo.release(1);
            Pasar();
        }
        else
        {
            System.out.println("Espero :(");
            MainThread.aux.release();
            synchronized (MainThread.semaforo)
            {
                try
                {
                    MainThread.semaforo.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Pasar();
        }
    }

    public void Pasar()
    {
        System.out.println("Pasamos :)");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Barrera();
    }

    public Nucleo(ArrayList<BloqueCacheDatos> miCache, ArrayList<BloqueCacheDatos> otroCache, ArrayList<BloqueCacheInstrucciones> miCacheIns, int[] memoriaPrincipalInstrucciones, int[] memoriaPrincipalDatos, boolean busDatos, boolean busInstrucciones, int numero){
        this.miCache = miCache;
        this.otroCache = otroCache;
        this.miCacheIns = miCacheIns;
        this.memoriaPrincipalInstrucciones = memoriaPrincipalInstrucciones;
        this.memoriaPrincipalDatos = memoriaPrincipalDatos;
        this.busDatos = busDatos;
        this.busInstrucciones = busInstrucciones;
        this.numNucleo = numero;

    }

    public Contexto procesar(Contexto contexto)
    {
        setContexto(contexto);
        /*currentPC = contexto.getPC();
        if (checkearEnCache())*/
        //ejecutarInstruccion();
        /*else mainT.loadToCacheInstFromMem(currentPC);*/
        /*int[] instruction = mainT.getInstructionFromMem(currentPC);
        for (int i = 0; i < 4; i++)
        {
            System.out.print(instruction[i]+" ");
        }*/
        return contexto;
    }

    public Contexto getContexto()
    {
        return context;
    }

    public void setContexto(Contexto contexto) {
        context = contexto;
    }

    public Contexto resolverInstruccion(Contexto contexto,int[] ir){
        this.context = contexto;

        switch (ir[0]){
            case 8:
                daddi(ir);
                break;
            case 32:
                dadd(ir);
                break;
            case 34:
                dsub(ir);
                break;
            case 12:
                dmul(ir);
                break;
            case 14:
                ddiv(ir);
                break;
            case 4:
                beqz(ir);
                break;
            case 5:
                bnez(ir);
                break;
            case 3:
                jal(ir);
                break;
            case 2:
                jr(ir);
                break;
            case 35:
                LW(ir[1],ir[2],ir[3]);
                break;
            case 43:
                SW(ir[1],ir[2],ir[3]);
                break;
            case 63:
                break;
            default:
                //Hubo fallo en cache de instrucciones
                break;
        }
        return context;
    }

    public void daddi(int[] ir){
        int valor = context.getRegistro(ir[1])+ir[3];
        context.setRegistro(ir[2],valor);
    }

    public void dadd(int[] ir){
        int valor = context.getRegistro(ir[1])+context.getRegistro(ir[2]);
        context.setRegistro(ir[3],valor);
    }

    public void dsub(int[] ir){
        int valor = context.getRegistro(ir[1])-context.getRegistro(ir[2]);
        context.setRegistro(ir[3],valor);
    }

    public void dmul(int[] ir){
        int valor = context.getRegistro(ir[1])*context.getRegistro(ir[2]);
        context.setRegistro(ir[3],valor);
    }

    public void ddiv(int[] ir){
        int valor = context.getRegistro(ir[1])/context.getRegistro(ir[2]);
        context.setRegistro(ir[3],valor);
    }

    public void beqz(int[] ir){
        if(context.getRegistro(ir[1])==0)
            context.setPC(context.getPC()+(4*ir[3]));
    }

    public void bnez(int[] ir){
        if(context.getRegistro(ir[1])!=0)
            context.setPC(context.getPC()+(4*ir[3]));
    }

    public void jal(int[] ir){
        context.setRegistro(31,context.getPC());
        context.setPC(context.getPC()+ir[3]);
    }

    public void jr(int[] ir){
        context.setPC(ir[1]);
    }

    public void LW(int rf, int rd, int inm) {
        int dir_mem = context.getRegistro(rf) + inm;
        int num_bloque = dir_mem / 16;
        int pos_cache = num_bloque % 4;
        int num_palabra = ( dir_mem - ( num_bloque * 16 ) ) / 4;
        ResultadoFalloCahe falloCahe = new ResultadoFalloCahe();
        BloqueCacheDatos target = verifyCacheDatos( pos_cache, num_bloque, numNucleo);
        if (target.getEtiqueta() > -1 && target.getEstado() < 2 ) {
            context.setRegistro( rd, target.getPalabras()[num_palabra]);
        } else falloCahe = falloCacheLw(num_bloque,num_palabra,pos_cache);
        if(falloCahe.seLogro){ context.setRegistro( rd, falloCahe.resultado);}
        else context.setPC(context.getPC()-4);//no consiguio algo, se devuelve una instruccion para volver a empezar
    }

    public synchronized void hayFallo()
    {
        if (MainThread.candado.tryLock()) {
            try {

            } finally {
                MainThread.candado.unlock();
            }
        } else {
            System.out.println("Soy " + numNucleo + " y no obtuve el candado");
            //no consiguió el candado
        }
        Barrera();
    }

    public ResultadoFalloCahe falloCacheLw(int bloque, int palabra, int posicionEnCache){
        huboFallo = 40;
        ResultadoFalloCahe resultado = new ResultadoFalloCahe();
        if(busDatos){
            busDatos =false;
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
        busDatos =true;
        return resultado;
    }

    public void SW(int rd, int rf , int inm){
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
        }else context.setPC(context.getPC()-4);
    }

    public boolean falloCacheSw(int bloque, int palabra, int posicionEnCache){
        huboFallo = 40;
        boolean resultado=false;
        if(busDatos){
            busDatos =false;
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
        busDatos =true;
        return resultado;
    }

    public BloqueCacheDatos verifyCacheDatos( int posicion, int numBloque, int idNucleo ){
        BloqueCacheDatos invalid = new BloqueCacheDatos();
        BloqueCacheDatos target = idNucleo == 0 ? miCache.get(posicion) : otroCache.get(posicion);
        System.out.print(posicion + "   " + numBloque + "   " + target.getEtiqueta() );
        if ( target.getEtiqueta() == numBloque ) return target;
        return invalid;
    }

    private void guardarBloqueEnMemoria(int bloque, boolean esMiCache){
        int posicionBloqueMemoria = bloque*16;
        int posicionBloqueCache = bloque%4;
        int[] palabras;
        if(esMiCache) {
            palabras = miCache.get(posicionBloqueCache).getPalabras();
            miCache.get(posicionBloqueCache).setEstado(0);//0 es compartido
            if(otroCache.get(posicionBloqueCache).getEtiqueta()==bloque)
                otroCache.get(posicionBloqueCache).setEstado(2);//2 es invalido
        }else{
            palabras = otroCache.get(posicionBloqueCache).getPalabras();
            otroCache.get(posicionBloqueCache).setEstado(0);//0 es compartido
            if(miCache.get(posicionBloqueCache).getEtiqueta()==bloque)
                miCache.get(posicionBloqueCache).setEstado(2);//2 es invalido
        }
        for(int i = 0; i < 4; i++) {
            memoriaPrincipalDatos[posicionBloqueMemoria] = palabras[0];
            memoriaPrincipalDatos[posicionBloqueMemoria+4] = palabras[1];
            memoriaPrincipalDatos[posicionBloqueMemoria+8] = palabras[2];
            memoriaPrincipalDatos[posicionBloqueMemoria+12] = palabras[3];
        }
    }

    private void copiarBloqueDesdeMemoria(int bloque){
        int posicionBloqueMemoria = bloque*16;
        int posicionBloqueCache = bloque%4;
        int[] palabras = new int[4];
        palabras[0] = memoriaPrincipalDatos[posicionBloqueMemoria];
        palabras[1] = memoriaPrincipalDatos[posicionBloqueMemoria+1];
        palabras[2] = memoriaPrincipalDatos[posicionBloqueMemoria+2];
        palabras[3] = memoriaPrincipalDatos[posicionBloqueMemoria+3];
        miCache.get(posicionBloqueCache).setEstado(0);//0 es compartido
        miCache.get(posicionBloqueCache).setPalabras(palabras);
        miCache.get(posicionBloqueCache).setEtiqueta(bloque);

    }

    private int[] siguienteInstruccion(){
        int num_bloque = context.getPC() / 16;
        int pos_cache = num_bloque % 4;
        int num_palabra = ( context.getPC() - ( num_bloque * 16 ) ) / 4;
        int[] result = {-1,-1,-1,-1};
        try{
            semaphoreMiCache.acquire();
            if(miCacheIns.get(pos_cache).getEtiqueta()==num_bloque){
                result=miCacheIns.get(pos_cache).getPalabra(num_palabra);
                context.setPC(context.getPC()+4);
            }
            else{ //fallo cache de instrucciones
                huboFallo = 40;
                if(busInstrucciones){
                    try{
                        semaphoreOtroCache.acquire();
                        int[][] instrucciones = new int[4][4];
                        for(int i=0; i<4; i++){
                            instrucciones[0][i] = memoriaPrincipalInstrucciones[context.getPC()+i];
                            instrucciones[2][i] = memoriaPrincipalInstrucciones[context.getPC()+i+4];
                            instrucciones[3][i] = memoriaPrincipalInstrucciones[context.getPC()+i+8];
                            instrucciones[4][i] = memoriaPrincipalInstrucciones[context.getPC()+i+12];
                        }
                        miCacheIns.get(pos_cache).setInstrucciones(instrucciones);
                        miCacheIns.get(pos_cache).setEtiqueta(num_bloque);
                    }catch (InterruptedException e) { }finally {semaphoreOtroCache.release();}
                }
            }
        } catch (InterruptedException e) {

        }finally {semaphoreMiCache.release();}
        return result;
    }

    private static class ResultadoFalloCahe{
        private int resultado = 0;
        private boolean seLogro = false;

        public ResultadoFalloCahe() {
        }

        public int getResultado() {
            return resultado;
        }

        public void setResultado(int resultado) {
            this.resultado = resultado;
        }

        public boolean isSeLogro() {
            return seLogro;
        }

        public void setSeLogro(boolean seLogro) {
            this.seLogro = seLogro;
        }
    }
}

