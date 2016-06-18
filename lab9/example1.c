#include <stdio.h>

/*int f(int a, int b, int c) {
  c = c + 1;
  return a + b * c;
}

int k(int z) {
  return z+42;
}

int g(int p, int q) {
  return k(p) - f(p,q,q) + f(p,p,q);
}

int h(int m) {
  int n = (m>0) ? 0 : 42;
  int r = k(m);
  return g(n,r) + g(m,r);
}
*/
int main() {
  int w = 10;
  
  
  int h() {

    int k(z) {
      return z+42;
    }

    int g(int p) {
    
      int q = k(w);

      int f(int b) {
        
        return p + b * (q + 1);
      }

      return k(p) - f(q) + f(p);
    }
    
    int n = (w>0) ? 0 : 42;
    int r = k(w);
    return g(n) + g(w);
  }
  printf("%d\n",h());
}

  
  


