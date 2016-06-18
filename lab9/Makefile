# Makefile for CS322 Lab9.
#

examples: example1 example2 example3

example1: example1.c
	$(CC) -g -o example1 example1.c

nested1: nested1.c
	$(CC) -g -o nested1 nested1.c

dropped1: dropped1.c
	$(CC) -g -o dropped1 dropped1.c

example2: example2.s
	$(CC) -g -o example2 example2.s

example2.s: example2.c
	$(CC) -S -O1 -o example2.s example2.c

example3: example3.s
	$(CC) -g -o example3 example3.s

example3.s: example3.c
	$(CC) -S -O1 -o example3.s example3.c

clean:
	rm -f example1 nested1 dropped1 example2.s example2 exampl3.s example3