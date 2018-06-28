import java.util.ArrayList;
import java.util.Vector;

public class Contexto
{
    private int ID, PC, index;
    private int[] registros;
    private int Cache; /* 0 = Nucleo 0
                          1 = Nucleo 1 */

    private int estado; /* 0 = Bien,
                           1 = Fallo Datos,
                           2 = Fallo Instrucciones */

    public Contexto()
    {
        estado = 0;
        registros = new int[32];
    }

    public Contexto(int nID, int nPC, int idx)
    {
        ID = nID;
        PC = nPC;
        index = idx;
        estado = 0;
        registros = new int[32];
    }

    public int getID()
    {
        return ID;
    }

    public void setID(int nID)
    {
        ID = nID;
    }

    public int getPC()
    {
        return PC;
    }

    public void setPC(int nPC)
    {
        PC = nPC;
    }

    public int[] getRegistros()
    {
        return registros;
    }

    public void setRegistros(int[] nRegs)
    {
        registros = nRegs;
    }

    public int getCache()
    {
        return Cache;
    }

    public void setCache(int nCache)
    {
        Cache = nCache;
    }

    public int getEstado()
    {
        return estado;
    }

    public void setEstado(int nEstado)
    {
        estado = nEstado;
    }

    public void setRegistro(int posicion, int valor){registros[posicion]=valor;}

    public int getRegistro(int posicion){return registros[posicion];}
}
