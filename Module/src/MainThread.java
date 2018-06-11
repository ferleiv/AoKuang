import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class MainThread
{
    public static void main(String[] args)
    {
        MainThread mainThread = new MainThread();
        mainThread.leerHilillos("C:\\Users\\pjmq2\\Documents\\MEGAsync\\UCR\\Informatica\\CI-1323 Arquitectura de Computadores\\Proyecto\\AoKuang\\Module\\Hilillos\\0.txt");
    }

    public MainThread() {
    }

    private void leerHilillos (String ruta){
        try {
            BufferedReader br = new BufferedReader(new FileReader(ruta));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
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
    }
}
