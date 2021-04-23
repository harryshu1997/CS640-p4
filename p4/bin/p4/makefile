JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	BufferItem.java \
	ClientListener.java \
	ClientSender.java \
	ClientTimer.java \
	ServerListener.java \
	SharedData.java \
	TCPacket.java \
	TCPReceiver.java \
	TCPSender.java \
	UDP.java \
	TCPend.java 

        

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class