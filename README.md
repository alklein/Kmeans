Kmeans
======

Sequential and Parallel K-Means Clustering

Kadoop is a map-reduce framework with custom distributed file system.

## Setup

* Set the environment variables:
  > setenv PATH ${PATH}:/usr/local/lib/openmpi/bin 
  > setenv CLASSPATH ./:/usr/local/lib/openmpi/lib/mpi.jar

* Build the project: 
  > make all
  
## Examples

* Sequential:
  > java kmeans

* Parallel: 
  > mpirun --mca btl tcp,self --mca btl_tcp_if_include eth0 --hostfile hosts.txt -np 3 java -classpath ${CLASSPATH} pll_kmeans

