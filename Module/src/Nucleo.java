public class Nucleo
{
    private int numNucleo;

    MainThread mainT;

    public Nucleo(){}

    public Nucleo(int numero, MainThread main)
    {
        numNucleo = numero;
        mainT = main;
    }

    public Contexto procesar(Contexto contexto)
    {
        System.out.print(contexto.getPC());
        return contexto;
    }
}

