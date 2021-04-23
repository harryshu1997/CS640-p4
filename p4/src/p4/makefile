JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

# need to remove all package declaration (packet p4) before make
CLASSES = \
	UDP.java \
	TCPacket.java 
	BufferItem.java \
	SharedData.java \
	ClientListener.java \
	ClientSender.java \
	ClientTimer.java \
	ServerListener.java \
	TCPReceiver.java \
	TCPSender.java \
	TCPend.java 

        

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class