int a[10];

int g(int *c, int*d) {
  return c[1] + d[2];
}

int f(int n) {
  int i;
  int b[10];
  for (i = 0; i < 10; i++)
    a[i] = a[i] + b[i] * n;
  return g(a,b);
}

int main (int argc, char **argv) {
  f(4);
  return 5;
}

  
