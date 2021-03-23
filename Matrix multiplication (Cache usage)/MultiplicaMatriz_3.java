class MultiplicaMatriz_3
{
  static int N = 1000;
  static int BLOQUES = 2;
  static long[][] A = new long[N][N];
  static long[][] B = new long[N][N];
  static long[][] C = new long[N][N];

  public static void main(String[] args)
  {
    long t1 = System.currentTimeMillis();

    // inicializa las matrices A y B
    for (int i = 0; i < N; i++)
      for (int j = 0; j < N; j++)
      {
        A[i][j] = i - 2*j;
        B[i][j] = i + 2 * j;
        C[i][j] = 0;
      }

    // transpone la matriz B, la matriz traspuesta queda en B
    for (int i = 0; i < N; i++)
      for (int j = 0; j < i; j++)
      {
        long x = B[i][j];
        B[i][j] = B[j][i];
        B[j][i] = x;
      }

    // multiplica la matriz A y la matriz B, el resultado queda en la matriz C
    int elemBloque = N/BLOQUES;
    for (int i = 0; i < N; i=i+elemBloque)
      for (int j = 0; j < N; j=j+elemBloque)
        for(int ii=i;ii<(i+elemBloque) && ii < N ; ii++)
          for(int jj=j;jj<(j+elemBloque) && jj < N ; jj++)
            for (int k = 0; k < N; k++)
              C[ii][jj] += A[ii][k] * B[jj][k];

    long sum=0;
    for (int i = 0; i < N; i++)
      for (int j = 0; j < N; j++)
        sum=sum+C[i][j];
    System.out.println("Sum: " + sum);

    long t2 = System.currentTimeMillis();
    System.out.println("Tiempo: " + (t2 - t1) + "ms");
  }
}