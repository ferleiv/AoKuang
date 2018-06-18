public class Nucleo
{
    private int numNucleo;

    MainThread mainT;
    private int currentPC;

    public Nucleo(){}

    public Nucleo(int numero, MainThread main)
    {
        numNucleo = numero;
        mainT = main;
    }

    public Contexto procesar(Contexto contexto)
    {
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

    public boolean checkearEnCache(){
        int bloqueInstruccion = currentPC / 16;
        int posicionCache = currentPC % 16;
        if ( mainT.verifyCacheInstructionsCore0(posicionCache, bloqueInstruccion) == 1 ) return true; //está en el caché
        return false;
    }

    public void ejecutarInstruccion(){

    }



}

