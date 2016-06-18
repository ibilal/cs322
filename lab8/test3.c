// should print -40

#include <stdio.h>

extern int _f(int);
int main() {
  printf("example3: %d\n", _f(0));
}
