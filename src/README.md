Kmeans
======

Sequential and Parallel K-Means Clustering

## Setup

* Move into the source directory:
  > cd src

* Set the PATH variable:
  > setenv PATH ${PATH}:/usr/local/lib/openmpi/bin 

* Set the CLASSPATH variable:
  > setenv CLASSPATH ./:/usr/local/lib/openmpi/lib/mpi.jar

* Build the project: 
  > make all

* If desired, generate new data:
  > python make_data.py
  
## Examples

* Sequential:
  > java kmeans

* Parallel: 
  > mpirun --mca btl tcp,self --mca btl_tcp_if_include eth0 --hostfile hosts.txt -np 3 java -classpath ${CLASSPATH} pll_kmeans

