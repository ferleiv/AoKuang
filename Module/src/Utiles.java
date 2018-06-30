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


    /*public int revisarCacheLw(int bloque, int palabra){
        ResultadoFalloCahe resultadoFalloCahe = new ResultadoFalloCahe();
        int posicionEnCache = bloque%4;
        try {
            semaphoreMiCache.acquire();
            if (miCache.get(posicionEnCache).getEtiqueta() == bloque) {
                if (miCache.get(posicionEnCache).getEstado() == 2) {
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
    }*/

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

        System.out.printf("Bloque 1 palabra 1: %d \nBloque 2 palabra 2: %d \nBloque 3 palabra 3: %d \nBloque 4 palabra 1: %d \nBloque 5 palabra 2: %d \n",resultadosBuenos[0],resultadosBuenos[1],resultadosBuenos[2],resultadosBuenos[3],resultadosBuenos[4]);
    }
}
