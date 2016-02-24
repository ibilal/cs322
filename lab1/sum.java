class Test {
  public static int sum(int[] a, int n) {
    int sum = 0;
    int i = 0;
    while (i < n) {
      sum = sum + a[i];
      i = i + 1;
    }
    return sum;
  }

  public static void main(String[] x) {
    int[] a = new int[3];
    int sum;
    a[0] = 1;  
    a[1] = 2;  
    a[2] = 3;  
    sum = sum(a, 3);
    System.out.println("Array sum:");
    System.out.println(sum);
  }
}
