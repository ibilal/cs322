#include <stdio.h>

int f(int x,int y) {
  int g (int z) {
    int h(int u) {
      return y*u-z;
    }
    return h(x+z) + h(0);
  }
  return g(x+y) + g(0);
}

int main() {
  printf("%d\n",f(1,2));
}
