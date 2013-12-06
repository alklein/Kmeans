import UTILS.*;

import java.io.*;
import java.net.*;
import java.util.*;

import java.util.Random;


public class kmeans {

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

    /*
      Distance function for DNA data
     */
    private static float DNA_dist(ArrayList<Float> x, ArrayList<Float> y) {
	float result = 0;
	for (int i=0; i < x.size(); i++) {
	    if (x.get(i) != y.get(i)) { result += 1; }
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
    private static ArrayList<ArrayList<Float>> initiate(int k, int len, UTILS.Constants.METRIC m, float scale) {
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

    /*
      Randomly select initial values of means
     */
    private static ArrayList<ArrayList<Float>> initiate_random(int k, int len, UTILS.Constants.METRIC m) {
	ArrayList<ArrayList<Float>> result = new ArrayList<ArrayList<Float>>();
	Random r = new Random();
	for (int i=0; i < k; i++) {
	    ArrayList<Float> cur_mean = new ArrayList<Float>();
	    if (m == Constants.METRIC.EUCLIDEAN) {
		for (int j=0; j < len; j++) {
		    cur_mean.add(r.nextFloat());
		}
	    } else if (m == Constants.METRIC.EDIT_DISTANCE) {
		// TODO
	    }
	    result.add(cur_mean);
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
		// TODO: check metric type
		float cur_dist = Euc_dist(cur_point, means.get(k));
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
      Recalculate each mean as the centroid of the points assigned to it
     */
    private static ArrayList<ArrayList<Float>> recenter(HashMap <ArrayList<Float>, ArrayList<ArrayList<Float>>> assignments) {
	ArrayList<ArrayList<Float>> result = new ArrayList<ArrayList<Float>>();
	for (ArrayList<Float> mean : assignments.keySet()) {
	    ArrayList<ArrayList<Float>> points = assignments.get(mean);
	    ArrayList<Float> new_mean = new ArrayList<Float>();
	    for (int i=0; i < mean.size(); i++) {
		float sum = 0;
		for (int k=0; k < points.size(); k++) {
		    ArrayList<Float> point = points.get(k);
		    sum += point.get(i);
		}
		float avg = sum / points.size();
		new_mean.add(avg);
	    }
	    result.add(new_mean);
	}
	return result;
    }

    /*
      Compute means iteratively for mu rounds
     */
    private static ArrayList<ArrayList<Float>> kmeans(ArrayList<ArrayList<Float>> data, int k, UTILS.Constants.METRIC m, int mu) {
	ArrayList<ArrayList<Float>> means = initiate(k, data.get(0).size(), m, 20);
	int count = 0;
	while (count < mu) {
	    list_means(means);
	    HashMap <ArrayList<Float>, ArrayList<ArrayList<Float>>> assignments = assign(data, means, m);
	    means = recenter(assignments);
	    count += 1;
	}
	return means;
    }

    /*
      Print out a list of "vectors"
     */
    private static void list_all(ArrayList<ArrayList<Float>> data) {
	for (int i=0; i < data.size(); i++) {
	    ArrayList<Float> cur_point = data.get(i);
	    System.out.println(cur_point.get(0).toString() + "\t" + cur_point.get(1).toString());
	}
    }

    /*
      Print out a single "vector"
     */
    private static void list_means(ArrayList<ArrayList<Float>> means) {
	for (int i=0; i < means.size(); i++) {
	    ArrayList<Float> line = means.get(i);
	    System.out.println(line.get(0).toString() + "\t" + line.get(1).toString());
	}
	System.out.println("\n");
    }

    public static void main(String[] args) {
	String data_file = "2D_data.txt";
	ArrayList<ArrayList<Float>> data = load_floats(data_file);
	//list_all(data);

	ArrayList<ArrayList<Float>> means = kmeans(data, 4, Constants.METRIC.EUCLIDEAN, 5);
	list_means(means);
    }

}
