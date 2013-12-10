.SUFFIXES: .java .class
.java.class:
	javac $<

CLASSES = kmeans.class pll_kmeans.class UTILS/Constants.class

all: $(CLASSES)

kmeans: kmeans.class
pll_kmeans: pll_kmeans.class
Constants: Constants.class

clean:
	rm $(CLASSES)

