#include "stdio.h"

int a = 3;
int b;

void g() {
  a = 7;
}

int f(int c) {
  int d = a + c;
  b = d - 6;
  g();
  return b * 10;
}

int main (int argc, char **argv) {
  int x = f(4);
  return x + 5;
}

  
