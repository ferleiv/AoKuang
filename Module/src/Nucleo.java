public class Nucleo
{
    private int numNucleo;
    private Contexto context;

    public Nucleo(){}

    public Nucleo(int numero)
    {
        numNucleo = numero;
    }

    public Contexto procesar(Contexto contexto)
    {
        return contexto;
    }

    public void daddi(int[] ir){
        int valor = context.getRegistro(ir[1])+ir[3];
        context.setRegistro(ir[2],valor);
    }

    public void dadd(int[] ir){
        int valor = context.getRegistro(ir[1])+context.getRegistro(ir[2]);
        context.setRegistro(ir[3],valor);
    }
}

