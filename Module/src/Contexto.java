import java.util.ArrayList;
import java.util.Vector;

public class Contexto
{   //Clase para la abstranccion del contexto de cada hilillo
    private int ID, PC; //Identificador de hilillo, Program Counter
    private int[] registros; //Vector de los registros
    private int Cache; /* 0 = Nucleo 0
                          1 = Nucleo 1 */

    private int estado; /* 0 = Bien,
                           1 = Fallo Datos,
                           2 = Fallo Instrucciones */

    //Constructor vacio que inicializa de forma muy basica
    public Contexto()
    {
        estado = 0;
        registros = new int[32];
        for(int i = 0; i < 32; i++)
            registros[i] = 0;
    }

    //Getter del identificador de hilillo
    public int getID()
    {
        return ID;
    }

    //Setter del identificador del hilillo
    public void setID(int nID)
    {
        ID = nID;
    }

    //Getter del Program Counter
    public int getPC()
    {
        return PC;
    }

    //Setter del Program Counter
    public void setPC(int nPC)
    {
        PC = nPC;
    }

    //Getter de los registros
    public int[] getRegistros()
    {
        return registros;
    }

    //Setter de los registros
    public void setRegistros(int[] nRegs)
    {
        registros = nRegs;
    }

    //Getter del numero de cache en el que esta
    public int getCache()
    {
        return Cache;
    }

    //Setter de la cache en la que se encuentra
    public void setCache(int nCache)
    {
        Cache = nCache;
    }

    //Getter del estado en el que se encuentra
    public int getEstado()
    {
        return estado;
    }

    //Setter del estado en el que esta
    public void setEstado(int nEstado)
    {
        estado = nEstado;
    }

    //Getter de un registro en especifico
    public int getRegistro(int posicion){return registros[posicion];}

    //Setter de un registro especifico
    public void setRegistro(int posicion, int valor){registros[posicion]=valor;}
}
