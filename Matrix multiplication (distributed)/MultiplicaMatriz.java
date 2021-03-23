import java.net.Socket;
import java.net.ServerSocket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.lang.Thread;
import java.nio.ByteBuffer;
import java.util.Scanner;

class MultiplicaMatriz
{
  static int N;
  static long[][] A;
  static long[][] B;
  static long[][] C;

  static class Worker extends Thread{
    Socket conexion;
    Worker(Socket conexion){
      this.conexion = conexion;
    }
    public void run(){
      try{
        //Crear streams de entrada y salida
        DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
        DataInputStream entrada = new DataInputStream(conexion.getInputStream());
        
        //Enviar el tamaño de las matrices N
        salida.writeInt(N);

        //Leer el nodo que se conectó
        int nodo;
        nodo = entrada.readInt();

        //Enviar la parte correspondiente de A
        int inicioA = (nodo <= 2 ? 0 : N/2);
        ByteBuffer bbMatrixA = ByteBuffer.allocate((N*N/2)*8);
        for(int i=inicioA; i<(N/2)+inicioA; i++)
          for(int j=0; j<N; j++)
            bbMatrixA.putLong(A[i][j]);
        byte[] bytesMatrixA = bbMatrixA.array();
        salida.write(bytesMatrixA);

        //Enviar la parte correspondiente de B
        int inicioB = (nodo%2 ==0 ? N/2 : 0);
        ByteBuffer bbMatrixB = ByteBuffer.allocate((N*N/2)*8);
        for(int i=inicioB; i<(N/2)+inicioB; i++)
          for(int j=0; j<N; j++)
            bbMatrixB.putLong(B[i][j]);
        byte[] bytesMatrixB = bbMatrixB.array();
        salida.write(bytesMatrixB);


        //Se recibe la parte de la matriz C
        byte[] bytesReadC = new byte[(N*N/4)*8];
        read(entrada,bytesReadC,0,(N*N/4)*8);
        ByteBuffer bbReadC = ByteBuffer.wrap(bytesReadC);
        int inicioI = (nodo<=2 ? 0 : N/2);
        int inicioJ = (nodo%2 == 0 ? N/2 : 0);
        for (int i = inicioI; i < (N/2)+inicioI; i++)
          for(int j=inicioJ; j<(N/2)+inicioJ; j++)
            C[i][j] = bbReadC.getLong();

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

  //Función para imprimir una matrix de tamaño NxN
  private static void printMatrix(long[][] matrix){
    for(int i=0; i<N; i++){
      for(int j=0; j<N; j++){
        System.out.print(matrix[i][j]+"\t");
      }
      System.out.println();
    }
  }

  //Función para leer los datos completos de una entrada
  static void read(DataInputStream f,byte[] b,int posicion,int longitud) throws Exception{
    while (longitud > 0){
      int n = f.read(b,posicion,longitud);
      posicion += n;
      longitud -= n;
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
    { //Nodo 0, servidor
      Scanner in = new Scanner(System.in);
      System.out.println("Enter N (matrix size): ");
      N = in.nextInt();

      //Crear instancia del servidor
      ServerSocket servidor = new ServerSocket(50000);
      Worker [] w = new Worker[4];

      //Declarar las matrices
      A = new long[N][N];
      B = new long[N][N];
      C = new long[N][N];

      //Inicializar matrices A y B
      for (int i = 0; i < N; i++)
        for (int j = 0; j < N; j++){
          A[i][j] = i - 2*j;
          B[i][j] = i + 2*j;
          C[i][j] = 0;
        }

      //Si la matriz es de 10x10 o menor se imprimen las matrices A y B
      if(N<=10){
        System.out.println("Matriz A:");
        printMatrix(A);
        System.out.println("Matriz B:");
        printMatrix(B);
      }

      //Transponer matriz B
      for (int i = 0; i < N; i++)
        for (int j = 0; j < i; j++){
          long x = B[i][j];
          B[i][j] = B[j][i];
          B[j][i] = x;
        } 

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

      //Se calcula el checksum y se imprime
      long checksum=0;
      for(int i=0; i<N; i++)
        for(int j=0; j<N; j++)
          checksum+=C[i][j];
      System.out.println("\nValor de Checksum: "+checksum+"\n");
      
      //Si la matriz es de 10x10 o menor se imprime C (resultado de AxB)
      if(N<=10){
        System.out.println("Matriz C:");
        printMatrix(C);
      }
    }
    else
    { //Nodos 1,2,3,4
      //Se establece el for para reintentar conexión si el servidor no está activo
      Socket conexion = null;
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
      
      //Leer el tamaño N de las matrices
      N = entrada.readInt();

      //Se envia el nodo que se está ejecutando al servidor
      salida.writeInt(nodo);

      //Se crean los espacios para la matriz
      A = new long[N/2][N];
      B = new long[N/2][N];
      C = new long[N/2][N/2];

      //Inicializar matriz C
      for (int i = 0; i < N/2; i++)
        for (int j = 0; j < N/2; j++)
          C[i][j] = 0;

      //Se recibe la parte de la matriz A
      byte[] bytesReadA = new byte[(N*N/2)*8];
      read(entrada,bytesReadA,0,(N*N/2)*8);
      ByteBuffer bbReadA = ByteBuffer.wrap(bytesReadA);
      for (int i = 0; i < N/2; i++)
        for(int j=0; j<N; j++)
          A[i][j] = bbReadA.getLong();   

      //Se recibe la parte de la matriz B
      byte[] bytesReadB = new byte[(N*N/2)*8];
      read(entrada,bytesReadB,0,(N*N/2)*8);
      ByteBuffer bbReadB = ByteBuffer.wrap(bytesReadB);
      for (int i = 0; i < N/2; i++)
        for(int j=0; j<N; j++)
          B[i][j] = bbReadB.getLong(); 

      //Se calcula la multiplicación renglón a renglón
      for(int i=0; i<N/2;i++)
        for(int j=0; j<N/2; j++)
          for(int k=0; k<N; k++)
            C[i][j]+= A[i][k]*B[j][k];

      //Se envia la matriz C al servidor
      ByteBuffer bbMatrixC = ByteBuffer.allocate((N*N/4)*8);
      for(int i=0; i<N/2; i++)
        for(int j=0; j<N/2; j++)
          bbMatrixC.putLong(C[i][j]);
      byte[] bytesMatrixC = bbMatrixC.array();
      salida.write(bytesMatrixC);

      //Se cierran los flujos y la conexión
      salida.close();
      entrada.close();
      conexion.close();
    }
  }
}