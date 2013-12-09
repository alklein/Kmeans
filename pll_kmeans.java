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

    /*
      Print out a single 2D "vector"
     */
    private static void list_2D_means(ArrayList<ArrayList<Float>> means) {
	for (int i=0; i < means.size(); i++) {
	    ArrayList<Float> line = means.get(i);
	    System.out.println(line.get(0).toString() + "\t" + line.get(1).toString());
	}
	System.out.println("\n");
    }


    public static void main(String[] args) throws MPIException {

	// Set up constants and data
	String data_file = "2D_data.txt";
	int mu = 5;
	int k = 4;
	UTILS.Constants.METRIC m = Constants.METRIC.EUCLIDEAN;
	ArrayList<ArrayList<Float>> data = load_floats(data_file);

	// Initialize means in array-list and array form
	ArrayList<ArrayList<Float>> means = initiate(k, data.get(0).size(), 20);       
	float[][] hard_means = new float[k][2];
	for (int i=0; i < k; i++) {
	    float[] row = new float[2];
	    row[0] = means.get(i).get(0);
	    row[1] = means.get(i).get(1);
	    hard_means[i] = row;
	}

	// Set up MPI
	MPI.Init(args);
	int myrank = MPI.COMM_WORLD.Rank();

	// Partition data 
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

	// Recompute kmeans for mu iterations
	int count = 0;
	while (count < mu) {

	    HashMap <ArrayList<Float>, ArrayList<ArrayList<Float>>> assignments = assign(mydata, means, m);	
	    
	    float[] partial_counts = new float[k];
	    float[] total_counts = new float[k];
	    
	    float[] partial_sums_0 = new float[k];
	    float[] total_sums_0 = new float[k];
	    
	    float[] partial_sums_1 = new float[k];
	    float[] total_sums_1 = new float[k];
	    
	    // compute partial (local) sums and counts
	    for (int i=0; i < k; i++) {
		ArrayList<Float> cur_mean = means.get(i);
		ArrayList<ArrayList<Float>> his_points = assignments.get(cur_mean);
		float partial_count = (float) 0;
		float partial_sum_0 = (float) 0;
		float partial_sum_1 = (float) 0;
		for (int j=0; j < his_points.size(); j++) {
		    ArrayList<Float> cur_point = his_points.get(j);
		    partial_count += (float) 1;
		    partial_sum_0 += cur_point.get(0);
		    partial_sum_1 += cur_point.get(1);
		}
		partial_counts[i] = partial_count;
		partial_sums_0[i] = partial_sum_0;
		partial_sums_1[i] = partial_sum_1;
	    }
	    
	    // reduce to total sum and count for each mean
	    MPI.COMM_WORLD.Allreduce(partial_counts, 0, total_counts, 0, k, MPI.FLOAT, MPI.SUM); //, 0);
	    MPI.COMM_WORLD.Allreduce(partial_sums_0, 0, total_sums_0, 0, k, MPI.FLOAT, MPI.SUM); //, 0);
	    MPI.COMM_WORLD.Allreduce(partial_sums_1, 0, total_sums_1, 0, k, MPI.FLOAT, MPI.SUM); //, 0);
	    
	    // recompute means
	    ArrayList<ArrayList<Float>> new_means = new ArrayList<ArrayList<Float>>();
	    for (int i=0; i < k; i++) {
		ArrayList<Float> new_mean = new ArrayList<Float>();
		float xval = total_sums_0[i] / total_counts[i];
		float yval = total_sums_1[i] / total_counts[i];
		new_mean.add(xval);
		new_mean.add(yval);
		new_means.add(new_mean);
	    }	    

	    // master prints out new means
	    if (myrank == 0) {
		list_2D_means(new_means);
	    }
	    
	    means = new_means;
	    count += 1;
	}

	MPI.Finalize();
    }

}