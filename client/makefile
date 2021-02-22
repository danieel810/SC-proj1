JFLAGS = -g
JC = javac
SERVER = SeiTchiz.java
JVM= java

.SUFFIXES: .java .class
.java.class:
		$(JC) $(JFLAGS) $*.java

CLASSES = \
		SeiTchiz.java \

default: classes

run: classes
	$(JVM) $(SERVER) 127.0.0.1:45678 Ricardo

classes: $(CLASSES:.java=.class)

clean:
		$(RM) *.class