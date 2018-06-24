public class Nucleo
{
    private int numNucleo;

    MainThread mainT;
    private int currentPC;
    private Contexto contexto;

    public Nucleo(){}

    public Nucleo(int numero, MainThread main)
    {
        numNucleo = numero;
        mainT = main;
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
        return contexto;
    }

    public void setContexto(Contexto cont)
    {
        contexto = cont;
    }

    public boolean checkearEnCache(){
        int bloqueInstruccion = currentPC / 16;
        int posicionCache = bloqueInstruccion % 4;
        if ( mainT.verifyCacheInstructionsCore0(posicionCache, bloqueInstruccion) == true ) {
            System.out.print("si está");
            return true;
        }
        return false;
    }

    /*Método de prueba para probar LW*/
    public void ejecutarInstruccion(){
        LW(0, 11, 308);
    }

    public void LW(int rf, int rd, int inm) {
        int dir_mem = contexto.getRegistro(rf) + inm;
        int num_bloque = dir_mem / 16;
        int pos_cache = num_bloque % 4;
        BloqueCacheDatos target = mainT.verifyCacheDatos( pos_cache, num_bloque, numNucleo);
        if (target.getEtiqueta() > -1 && target.getEstado() < 2 ) {
            int num_palabra = ( dir_mem - ( num_bloque * 16 ) ) / 4;
            contexto.setRegistro( rd, target.getPalabras()[num_palabra]);
        } else fallo_cache();
    }

    /* Si hay fallo caché en LW*/
    public void fallo_cache(){
        System.out.print("\nFallo de cache");
    }
}

