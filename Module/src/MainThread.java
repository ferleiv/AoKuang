import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainThread
{
    public static void main(String[] args)
    {
        MainThread mainThread = new MainThread();
        int posicion=0;
        posicion=mainThread.leerHilillos("C:\\Users\\pjmq2\\Documents\\MEGAsync\\UCR\\Informatica\\CI-1323 Arquitectura de Computadores\\Proyecto\\AoKuang\\Module\\Hilillos\\0.txt",posicion);
        posicion=mainThread.leerHilillos("C:\\Users\\pjmq2\\Documents\\MEGAsync\\UCR\\Informatica\\CI-1323 Arquitectura de Computadores\\Proyecto\\AoKuang\\Module\\Hilillos\\1.txt",posicion);
        posicion=mainThread.leerHilillos("C:\\Users\\pjmq2\\Documents\\MEGAsync\\UCR\\Informatica\\CI-1323 Arquitectura de Computadores\\Proyecto\\AoKuang\\Module\\Hilillos\\2.txt",posicion);
        posicion=mainThread.leerHilillos("C:\\Users\\pjmq2\\Documents\\MEGAsync\\UCR\\Informatica\\CI-1323 Arquitectura de Computadores\\Proyecto\\AoKuang\\Module\\Hilillos\\3.txt",posicion);
        posicion=mainThread.leerHilillos("C:\\Users\\pjmq2\\Documents\\MEGAsync\\UCR\\Informatica\\CI-1323 Arquitectura de Computadores\\Proyecto\\AoKuang\\Module\\Hilillos\\4.txt",posicion);
        posicion=mainThread.leerHilillos("C:\\Users\\pjmq2\\Documents\\MEGAsync\\UCR\\Informatica\\CI-1323 Arquitectura de Computadores\\Proyecto\\AoKuang\\Module\\Hilillos\\5.txt",posicion);

    }

    private int[] memoriaPrincipalDatos;
    private int[] memoriaPrincipalInstrucciones;
    private List<Contexto> contextoList;
    private boolean busDatos=true;
    private boolean busInstrucciones=true;
    private List<BloqueCacheDatos> cacheDatosNucleo0;
    private List<BloqueCacheDatos> cacheDatosNucleo1;
    private List<BloqueCacheInstrucciones> cacheInstruccionesNucleo0;
    private List<BloqueCacheInstrucciones> cacheInstruccionesNucleo1;

    public MainThread() {
        memoriaPrincipalDatos = new int[96];
        memoriaPrincipalInstrucciones = new int[640];

        for (int i = 0; i < memoriaPrincipalInstrucciones.length; i++)
            memoriaPrincipalInstrucciones[i] = 1;
        for (int i = 0; i < memoriaPrincipalDatos.length; i++)
            memoriaPrincipalDatos[i] = 1;
        contextoList = new ArrayList<Contexto>();
        cacheDatosNucleo0 = new ArrayList<BloqueCacheDatos>();
        cacheDatosNucleo1 = new ArrayList<BloqueCacheDatos>();
        cacheInstruccionesNucleo0 = new ArrayList<BloqueCacheInstrucciones>();
        cacheInstruccionesNucleo1 = new ArrayList<BloqueCacheInstrucciones>();
    }

    private int leerHilillos (String ruta, int posicionMemInstr){
        Contexto contexto = new Contexto();
        contexto.setPC(posicionMemInstr);
        contexto.setID(Integer.parseInt(""+ruta.charAt(ruta.length() - 5)));
        try {
            BufferedReader br = new BufferedReader(new FileReader(ruta));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                String[] data = line.split(" ");
                for(int i = 0; i < data.length ; i++){
                    memoriaPrincipalInstrucciones[posicionMemInstr]=Integer.parseInt(data[i]);
                    posicionMemInstr++;
                }
                line = br.readLine();
            }
            String everything = sb.toString();
            System.out.print(everything);
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        contextoList.add(contexto);
        return posicionMemInstr;
    }
}
