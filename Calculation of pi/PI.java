import java.net.Socket;
import java.net.ServerSocket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.lang.Thread;
import java.nio.ByteBuffer;

class PI
{
  static Object lock = new Object();
  static double pi = 0;
  static class Worker extends Thread
  {
    Socket conexion;
    Worker(Socket conexion)
    {
      this.conexion = conexion;
    }
    public void run()
    {
      // Algoritmo 1
      try{
        //Crear streams de entrada y salida
        DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
        DataInputStream entrada = new DataInputStream(conexion.getInputStream());
        //Leer datos enviados por el cliente
        double x;
        x = entrada.readDouble();
        //Actualizar el valor de pi
        synchronized(lock){
          pi = x + pi;
        }
        //Cerrar los streams de entrada y salida
        salida.close();
        entrada.close();
        //Cerrar la conexión
        conexion.close();
      }catch(Exception e){
        e.printStackTrace();
      }
      
    }
  }
  public static void main(String[] args) throws Exception
  {
    if (args.length != 1)
    {
      System.err.println("Uso:");
      System.err.println("java PI <nodo>");
      System.exit(0);
    }
    int nodo = Integer.valueOf(args[0]);
    if (nodo == 0)
    {
      // Algoritmo 2
      //Crear instancia del servidor
      ServerSocket servidor = new ServerSocket(50000);
      Worker [] w = new Worker[4];
      //Se aceptan 4 conexiones con los clientes, es decir, se aceptan 4 nodos
      for(int i=0;i<4;i++){
        Socket conexion = servidor.accept();
        //Se inicializa el objeto y el hilo que atiende a cada nodo
        w[i] = new Worker(conexion);
        w[i].start();
      }
      //Se espera el termino de los 4 hilos
      for(int i=0;i<4;i++){
        w[i].join();
      }
      //Se imprime el valor de pi
      System.out.println("Valor de PI: "+pi);
    }
    else
    {
      // Algoritmo 3
      Socket conexion = null;
      //Se establece el for para reintentar conexión si el servidor no está activo
      for(;;)
        try{
          conexion = new Socket("localhost",50000);
          break;
        }catch (Exception e){
          Thread.sleep(100);
        }
      //Se crean los flujos de entrada y salida
      DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
      DataInputStream entrada = new DataInputStream(conexion.getInputStream());
      //Se declara la variable que guardará la suma del nodo
      double suma = 0;
      //Se obtienen los 10000000 terminos correspondientes al nodo
      for(int i = 0; i<10000000; i++){
        suma = 4.0/(8*i+2*(nodo-2)+3)+suma;
      }
      //Se decide el signo de la suma y se escribe al servidor
      suma = (nodo%2 == 0? -suma : suma);
      salida.writeDouble(suma);
      //Se cierran los flujos y la conexión
      salida.close();
      entrada.close();
      conexion.close();

    }
  }
}