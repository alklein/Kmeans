.SUFFIXES: .java .class
.java.class:
	javac $<

CLASSES = kmeans.class UTILS/Constants.class

all: $(CLASSES)

kmeans: kmeans.class
Constants: Constants.class

clean:
	rm $(CLASSES)

