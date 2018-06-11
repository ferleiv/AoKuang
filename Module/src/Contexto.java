import java.util.ArrayList;
import java.util.Vector;

public class Contexto
{
    private int ID, PC;
    private int[] registros;

    public Contexto()
    {
        registros = new int[32];
    }

    public Contexto(int nID, int nPC)
    {
        ID = nID;
        PC = nPC;
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
}
