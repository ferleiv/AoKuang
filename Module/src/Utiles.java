import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Utiles {

    private List<BloqueCacheDatos> miCache;
    private List<BloqueCacheDatos> otroCache;
    private int[] memoriaPrincipalInstrucciones;
    private int[] memoriaPrincipalDatos;
    boolean bus = true;
    Semaphore semaphoreMiCache = new Semaphore(1);
    Semaphore semaphoreOtroCache = new Semaphore(1);
    Contexto context;

    public Utiles(List<BloqueCacheDatos> miCache, List<BloqueCacheDatos> otroCache, int[] memoriaPrincipalInstrucciones, int[] memoriaPrincipalDatos) {
        this.miCache = miCache;
        this.otroCache = otroCache;
        this.memoriaPrincipalInstrucciones = memoriaPrincipalInstrucciones;
        this.memoriaPrincipalDatos = memoriaPrincipalDatos;
    }

    public int revisarCacheLw(int bloque, int palabra){
        ResultadoFalloCahe resultadoFalloCahe = new ResultadoFalloCahe();
        int posicionEnCache = bloque%4;
        try {
            semaphoreMiCache.acquire();
            if (miCache.get(posicionEnCache).getEtiqueta() == bloque) {
                if (miCache.get(posicionEnCache).getEstado() == 2/*2 es invalido*/) {
                    resultadoFalloCahe = falloCacheLw(bloque, palabra, posicionEnCache);
                } else {
                    //Todo esta bien
                }
            } else {
                resultadoFalloCahe = falloCacheLw(bloque, palabra, posicionEnCache);
            }
        }
        catch (InterruptedException e){resultadoFalloCahe.setSeLogro(false);} finally {semaphoreMiCache.release();}
        return resultadoFalloCahe.getResultado();
    }

    public ResultadoFalloCahe falloCacheLw(int bloque, int palabra, int posicionEnCache){
        ResultadoFalloCahe resultado = new ResultadoFalloCahe();
        if(bus){
            bus=false;
            if(miCache.get(posicionEnCache).getEstado()==1/*1 es modificado*/){
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
        bus=true;
        return resultado;
    }

    public boolean revisarCacheSw(int bloque, int palabra){
        boolean resultadoFalloCahe = false;
        int posicionEnCache = bloque%4;
        try {
            semaphoreMiCache.acquire();
            if (miCache.get(posicionEnCache).getEtiqueta() != bloque || miCache.get(posicionEnCache).getEstado() != 1) {
                resultadoFalloCahe = falloCacheSw(bloque, palabra, posicionEnCache);
            }

        }
        catch (InterruptedException e){} finally {semaphoreMiCache.release();}
        return resultadoFalloCahe;
    }

    public boolean falloCacheSw(int bloque, int palabra, int posicionEnCache){
        boolean resultado=false;
        if(bus){
            bus=false;
            if(miCache.get(posicionEnCache).getEstado()==1/*1 es modificado*/){
                guardarBloqueEnMemoria(miCache.get(posicionEnCache).getEtiqueta(), true);
            }
            try{ //Intenta bloquear la posicion en la otra cache
                semaphoreOtroCache.acquire();
                if(otroCache.get(posicionEnCache).getEtiqueta()==bloque){
                    if(otroCache.get(posicionEnCache).getEstado()==1/*1 es modificado*/)
                        guardarBloqueEnMemoria(otroCache.get(posicionEnCache).getEtiqueta(),false);
                    otroCache.get(posicionEnCache).setEstado(2);
                }
            }
            catch(InterruptedException e){}finally {semaphoreOtroCache.release();}
            resultado=true;
        }
        bus=true;
        return resultado;
    }

    private void guardarBloqueEnMemoria(int bloque, boolean esMiCache){
        int posicionBloqueMemoria = bloque*16;
        int posicionBloqueCache = bloque%4;
        int[] palabras;
        if(esMiCache) {
            palabras = miCache.get(posicionBloqueCache).getPalabras();
            miCache.get(posicionBloqueCache).setEstado(0/*0 es compartido*/);
            if(otroCache.get(posicionBloqueCache).getEtiqueta()==bloque)
                otroCache.get(posicionBloqueCache).setEstado(2/*2 es invalido*/);
        }else{
            palabras = otroCache.get(posicionBloqueCache).getPalabras();
            otroCache.get(posicionBloqueCache).setEstado(0/*0 es compartido*/);
            if(miCache.get(posicionBloqueCache).getEtiqueta()==bloque)
                miCache.get(posicionBloqueCache).setEstado(2/*2 es invalido*/);
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
        miCache.get(posicionBloqueCache).setEstado(0/*0 es compartido*/);
        miCache.get(posicionBloqueCache).setPalabras(palabras);
        miCache.get(posicionBloqueCache).setEtiqueta(bloque);

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
                break;
            case 43:
                break;
            case 63:
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

    public static void main(String[] args)
    {

        int[] memoriaPrincipalDatosPrueba;
        int[] memoriaPrincipalInstruccionesPrueba;
        List<Contexto> contextoList;

        List<BloqueCacheDatos> cacheDatosNucleo0Prueba;
        List<BloqueCacheDatos> cacheDatosNucleo1Prueba;

        memoriaPrincipalDatosPrueba = new int[380];
        memoriaPrincipalInstruccionesPrueba = new int[640];

        for (int i = 0; i < memoriaPrincipalInstruccionesPrueba.length; i++)
            memoriaPrincipalInstruccionesPrueba[i] = 1;
        for (int i = 0; i < memoriaPrincipalDatosPrueba.length; i++)
            memoriaPrincipalDatosPrueba[i] = i;

        cacheDatosNucleo0Prueba = new ArrayList<BloqueCacheDatos>();
        cacheDatosNucleo1Prueba = new ArrayList<BloqueCacheDatos>();

        for(int i = 0; i < 4; i++){
            BloqueCacheDatos bloqueCacheDatos1 = new BloqueCacheDatos();
            BloqueCacheDatos bloqueCacheDatos2 = new BloqueCacheDatos();
            cacheDatosNucleo0Prueba.add(bloqueCacheDatos1);
            cacheDatosNucleo1Prueba.add(bloqueCacheDatos2);
        }

        int[] palabrasNuevas = {97,98,99,100};
        cacheDatosNucleo1Prueba.get(1).setEtiqueta(5);
        cacheDatosNucleo1Prueba.get(1).setPalabras(palabrasNuevas);
        cacheDatosNucleo1Prueba.get(1).setEstado(1);

        Utiles utiles = new Utiles(cacheDatosNucleo0Prueba,cacheDatosNucleo1Prueba,memoriaPrincipalInstruccionesPrueba,memoriaPrincipalDatosPrueba);
        int[] resultadosBuenos = new int[8];
        resultadosBuenos[0] = utiles.revisarCacheLw(1,1);
        resultadosBuenos[1] = utiles.revisarCacheLw(2,2);
        resultadosBuenos[2] = utiles.revisarCacheLw(3,3);
        resultadosBuenos[3] = utiles.revisarCacheLw(4,1);
        resultadosBuenos[4] = utiles.revisarCacheLw(5,2);
        resultadosBuenos[5] = utiles.revisarCacheLw(6,3);
        resultadosBuenos[6] = utiles.revisarCacheLw(7,3);
        resultadosBuenos[7] = utiles.revisarCacheLw(22,3);

        System.out.printf("Bloque 1 palabra 1: %d \nBloque 2 palabra 2: %d \nBloque 3 palabra 3: %d \nBloque 4 palabra 1: %d \nBloque 5 palabra 2: %d \n",resultadosBuenos[0],resultadosBuenos[1],resultadosBuenos[2],resultadosBuenos[3],resultadosBuenos[4]);
    }
}
