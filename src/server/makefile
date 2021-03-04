JFLAGS = -g
JC = javac
SERVER = SeiTchizServer.java
JVM= java

.SUFFIXES: .java .class
.java.class:
		$(JC) $(JFLAGS) $*.java

CLASSES = \
		SeiTchizServer.java \

default: classes

run: classes
	$(JVM) $(SERVER) 45678

classes: $(CLASSES:.java=.class)

clean:
		$(RM) *.class