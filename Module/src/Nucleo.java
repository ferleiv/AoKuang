import java.util.ArrayList;

public class Nucleo extends Thread
{
    private int numNucleo;
    private Contexto contexto;

    //MainThread mainT;
    private int currentPC;
    ArrayList<BloqueCacheDatos> cacheDatos;
    ArrayList<BloqueCacheInstrucciones> cacheInstrucciones;

    public Nucleo(){}

    public void run()
    {
        Barrera();
    }

    public void Barrera()
    {
        MainThread.enBarrera++;
        if(MainThread.enBarrera == 2)
        {
            System.out.println("Ahora somos 2");
            MainThread.enBarrera = 0;
            MainThread.semaforo.release(1);
            Pasar();
        }
        else
        {
            System.out.println("Espero :(");
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

    public Nucleo(int numero, ArrayList<BloqueCacheDatos> cacheD, ArrayList<BloqueCacheInstrucciones> cacheI)
    {
        numNucleo = numero;
        cacheDatos = cacheD;
        cacheInstrucciones = cacheI;
    }

    public Contexto procesar(Contexto contexto)
    {
        /*
        currentPC = contexto.getPC();
        if (checkearEnCache())
            ejecutarInstruccion();
        else mainT.loadToCacheInstFromMem(currentPC);
        /*int[] instruction = mainT.getInstructionFromMem(currentPC);
        for (int i = 0; i < 4; i++)
        {
            System.out.print(instruction[i]+" ");
        }*/
        return contexto;
    }

    public Contexto getContexto()
    {
        return contexto;
    }

    public void setContexto(Contexto cont)
    {
        contexto = cont;
    }

    public boolean checkearEnCache(){
        int bloqueInstruccion = currentPC / 16;
        int posicionCache = currentPC % 16;
        //if ( mainT.verifyCacheInstructionsCore0(posicionCache, bloqueInstruccion) == 1 ) return true; //está en el caché
        return false;
    }

    public void ejecutarInstruccion(){

    }

    public void daddi(int[] ir){
        int valor = contexto.getRegistro(ir[1])+ir[3];
        contexto.setRegistro(ir[2],valor);
    }

    public void dadd(int[] ir){
        int valor = contexto.getRegistro(ir[1])+contexto.getRegistro(ir[2]);
        contexto.setRegistro(ir[3],valor);
    }
}

