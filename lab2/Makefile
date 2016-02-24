# Makefile for CS322 Lab3.
#
JFLAGS = -g
JC = javac
JCC = javacc

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

irgen: 	ast/Ast0.class ast/Ast0Parser.class ir/IR0.class IR0Gen.class

clean:
	'rm' ast/*.class ir/*.class *.class


