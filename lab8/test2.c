// should print 3

#include <stdio.h>

extern int _f();
int main() {
  printf("example2: %d\n", _f());
}
