JC = javac

.SUFFIXES: 
	.java .class

# Run ' make dns ' to compile and excecute the java program

dns:
		$(JC) $*IDNSresolver.java
		java  $*IDNSresolver

CLASSES = IDNSresolver.java

# Run ' make clean ' to remove all .class files
clean:
		$(RM) *.class