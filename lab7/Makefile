# Makefile for CS322 Lab7.
#

examples: example0_32 example0 example1 example1_O1 example2 example2_O1 

example0_32: example0.c
	$(CC) -m32 -g -o example0_32 example0.c

example0: example0.s
	$(CC) -g -o example0 example0.s

example0.s: example0.c
	$(CC) -S -o example0.s example0.c

example1: example1.s
	$(CC) -g -o example1 example1.s

example1.s: example1.c
	$(CC) -S -o example1.s example1.c

example1_O1: example1_O1.s
	$(CC) -g -o example1_O1 example1_O1.s

example1_O1.s: example1.c
	$(CC) -O1 -S -o example1_O1.s example1.c

example2: example2.s
	$(CC) -g -o example2 example2.s

example2.s: example2.c
	$(CC) -S -o example2.s example2.c

example2_O1: example2_O1.s
	$(CC) -g -o example2_O1 example2_O1.s

example2_O1.s: example2.c
	$(CC) -O1 -S -o example2_O1.s example2.c

clean:
	rm -f example*.s example[012] example[012]_*
