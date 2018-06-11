import java.util.ArrayList;
import java.util.Vector;

public class Contexto
{
    private int ID, PC;
    private int[] registros;
    private String Cache;
    private int estado; /* 0 = Bien,
                           1 = Fallo Datos,
                           2 = Fallo Instrucciones */

    public Contexto()
    {
        estado = 0;
        registros = new int[32];
    }

    public Contexto(int nID, int nPC)
    {
        ID = nID;
        PC = nPC;
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

    public String getCache()
    {
        return Cache;
    }

    public void setCache(String nCache)
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
}
