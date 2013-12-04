.SUFFIXES: .java .class
.java.class:
	javac $<

CLASSES = kmeans.class

all: $(CLASSES)

kmeans: kmeans.class

clean:
	rm $(CLASSES)

