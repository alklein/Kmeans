import UTILS.*;

import mpi.*;
import java.io.*;
import java.net.*;
import java.util.*;

import java.util.Random;


public class pll_kmeans {


    /*
      Distance function for DNA data
     */
    private static float DNA_dist(ArrayList<Float> x, ArrayList<Float> y) {
	float result = 0;
	for (int i=0; i < x.size(); i++) {
	    if (x.get(i) > y.get(i) || y.get(i) > x.get(i)) { result += 1; }
	}
	return result;
    }

    /*
      Distance function for 2D point data
     */
    private static float Euc_dist(ArrayList<Float> x, ArrayList<Float> y) {
	float result = 0;
	for (int i=0; i < x.size(); i++) {
	    result += Math.pow(x.get(i) - y.get(i), 2);
	}
	return result;
    }

    /*
      Select small random perturbation, with expected size set by scale; 
      make positive or negative with 50-50 probability
     */
    private static float perturb(Random r, float scale) {
	float coin_flip = r.nextFloat();
	float push = r.nextFloat()/scale;
	if (coin_flip >= .5) { return push; }
	else { return -push; }
    }

    /*
      Initiate means by perturbing randomly about well-spaced hard-coded initial values
     */
    private static ArrayList<ArrayList<Float>> initiate(int k, int len, float scale) {
	ArrayList<ArrayList<Float>> result = new ArrayList<ArrayList<Float>>();
	Random r = new Random();

	float f1 = (float) .25;
	float f2 = (float) .75;

	ArrayList<Float> m1 = new ArrayList<Float>();
	m1.add(f1 + perturb(r, scale));
	m1.add(f1 + perturb(r, scale));
	result.add(m1);
	ArrayList<Float> m2 = new ArrayList<Float>();
	m2.add(f1 + perturb(r, scale));
	m2.add(f2 + perturb(r, scale));
	result.add(m2);
	ArrayList<Float> m3 = new ArrayList<Float>();
	m3.add(f2 + perturb(r, scale));
	m3.add(f1 + perturb(r, scale));
	result.add(m3);
	ArrayList<Float> m4 = new ArrayList<Float>();
	m4.add(f2 + perturb(r, scale));
	m4.add(f2 + perturb(r, scale));
	result.add(m4);
	return result;
    }

    private static ArrayList<ArrayList<Float>> load_floats(String filename) {
	ArrayList<ArrayList<Float>> result = new ArrayList<ArrayList<Float>>();
	try {
	    FileInputStream fis = new FileInputStream(filename);
	    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	    String line;
	    while ((line = br.readLine()) != null) {
		ArrayList<Float> cur_point = new ArrayList<Float>();
		String split_line[] = line.split("\t");
		for (int i=0; i < split_line.length; i++) {
		    String cur_str = split_line[i];
		    cur_point.add(Float.parseFloat(cur_str));
		}
		result.add(cur_point);
	    }
	    br.close();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return result;
    }

    private static HashMap <ArrayList<Float>, ArrayList<ArrayList<Float>>> assign(ArrayList<ArrayList<Float>> data, ArrayList<ArrayList<Float>> means, UTILS.Constants.METRIC m) {
	HashMap <ArrayList<Float>, ArrayList<ArrayList<Float>>> result = new HashMap <ArrayList<Float>, ArrayList<ArrayList<Float>>>();
	for (int i=0; i < means.size(); i++) {
	    ArrayList<ArrayList<Float>> points = new ArrayList<ArrayList<Float>>();
	    result.put(means.get(i), points);
	}
	for (int j=0; j < data.size(); j++) {
	    ArrayList<Float> cur_point = data.get(j);
	    ArrayList<Float> best_mean = means.get(0);
	    float best_dist = Euc_dist(cur_point, best_mean);
	    for (int k=1; k < means.size(); k++) {
		float cur_dist;
		if (m == Constants.METRIC.EUCLIDEAN) {
		    cur_dist = Euc_dist(cur_point, means.get(k));
		} else {
		    cur_dist = DNA_dist(cur_point, means.get(k));
		}
		if (cur_dist < best_dist) {
		    best_dist = cur_dist;
		    best_mean = means.get(k);
		}
	    }
	    ArrayList<ArrayList<Float>> old_points = result.get(best_mean);
	    old_points.add(cur_point);
	    result.put(best_mean, old_points);
	}
	return result;
    }


    public static void main(String[] args) throws MPIException {

	String data_file = "2D_data.txt";
	int mu = 3;
	int k = 4;
	UTILS.Constants.METRIC m = Constants.METRIC.EUCLIDEAN;
	ArrayList<ArrayList<Float>> data = load_floats(data_file);
	ArrayList<ArrayList<Float>> means = initiate(k, data.get(0).size(), 20);       

	MPI.Init(args);
	int myrank = MPI.COMM_WORLD.Rank();

	int points_per_worker = data.size() / MPI.COMM_WORLD.Size();
	int start = points_per_worker * myrank;
	int finish = points_per_worker * (myrank + 1);
	if (myrank == MPI.COMM_WORLD.Size() - 1) {
	    finish = data.size(); // leftovers
	}
	float[] myrange = {start, finish};

	ArrayList<ArrayList<Float>> mydata = new ArrayList<ArrayList<Float>>();
	for (int i=0; i < data.size(); i++) {
	    if ( (i >= start) && (i < finish)) {
		mydata.add(data.get(i));
	    }
	}

	int mydata_len = mydata.size();

	// start loop to mu here
	HashMap <ArrayList<Float>, ArrayList<ArrayList<Float>>> assignments = assign(mydata, means, m);	
	
	int[] partial_counts = new int[k];
	int[] total_counts;

	int[] partial_sums_0 = new int[k];
	int[] total_sums_0;

	int[] partial_sums_1 = new int[k];
	int[] total_sums_1;

	for (int i=0; i < means.size(); i++) {
	    ArrayList<Float> cur_mean = means.get(i);
	    ArrayList<ArrayList<Float>> his_points = assignments.get(cur_mean);
	    int partial_count = 0;
	    int partial_sum_0 = 0;
	    int partial_sum_1 = 0;
	    for (int j=0; j < his_points.size(); j++) {
		ArrayList<Float> cur_point = his_points.get(j);
		partial_count += 1;
		partial_sum_0 += cur_point.get(0);
		partial_sum_1 += cur_point.get(1);
	    }
	    partial_counts[i] = partial_count;
	    partial_sums_0[i] = partial_sum_0;
	    partial_sums_1[i] = partial_sum_1;
	}

	// TODO: reduce all the partial things;
	// recompute the means

	float[] myarr = {1, 2, 3, 4, 5};
	float[] result = new float[5];	

	if (myrank == 0) { System.out.println("Total length of data: " + Integer.toString(data.size())); }
	System.out.println("My rank: " + Integer.toString(myrank) + " my start: " + Integer.toString(start) + " my finish: " + Integer.toString(finish));

	MPI.COMM_WORLD.Reduce(myarr, 0, result, 0, 5, MPI.FLOAT, MPI.SUM, 0);
	if (myrank == 0) {
	    for (int i=0; i < result.length; i++) {
		System.out.println("Result: " + Float.toString(result[i]));
	    }
	}

	/*

	//int source;  // Rank of sender
	//int dest;    // Rank of receiver 
	//int tag=50;  // Tag for messages

	if (myrank == 0) {
	    // MASTER
	    dest = 0;
	    String myhost = MPI.Get_processor_name();
	    char [] message = ("Greetings from master").toCharArray();
	    MPI.COMM_WORLD.Send(message, 0, message.length, MPI.CHAR,dest, tag);
	    char [] m1 = new char [1000] ;
	    char [] m2 = new char [1000] ;
	    MPI.MPI_Reduce(m1, m2, 0, MPI_COMM_WORLD);
	} else {
	    // WORKER
	    char [] message = new char [1000];
	    Status s = MPI.COMM_WORLD.Recv(message, 0, 60, MPI.CHAR, MPI.ANY_SOURCE, tag);
	    char [] message = ("Worker response to " + message).toCharArray();
	    MPI.COMM_WORLD.Send(message, 0, message.length, MPI.CHAR,dest, tag);
	    }*/

	/*
	  if master:
	  while phase < mu:
	    send jobs;
	    block until receipt;
	    reduce result;
	    increment phase
	  end computation


	  if worker:
	  receive job
	  perform work
	  send result
	 */

	/*
 	int my_rank; // Rank of process


	int      p = MPI.COMM_WORLD.Size() ;

	if(myrank != 0) {
	    dest=0;
	    String myhost = MPI.Get_processor_name();
	    char [] message = ("Greetings from process " + myrank+" on "+myhost).toCharArray() ;
	    MPI.COMM_WORLD.Send(message, 0, message.length, MPI.CHAR,dest, tag) ;
	}
	else {  // my_rank == 0
	    for (source =1;source < p;source++) {
		char [] message = new char [60] ;
		Status s = MPI.COMM_WORLD.Recv(message, 0, 60, MPI.CHAR, MPI.ANY_SOURCE, tag) ;
		int nrecv = s.Get_count(MPI.CHAR);
		String s1 = new String(message);
		System.out.println("received: " + s1.substring(0,nrecv) + " : ") ;
	    }
	    }*/

	MPI.Finalize();
    }

}