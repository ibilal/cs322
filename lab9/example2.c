#include <stdio.h>

int f(int x,int y) {
  int h(int u) {
    return y*u;
  }
  int g (int z) {
    return h(x+h(z));
  }
  return g(x+y) + g(0);
}

int main() {
  printf("%d\n",f(1,2));
}
