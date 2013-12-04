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

    private static ArrayList<ArrayList<Float>> initiate(int k, int len, UTILS.Constants.METRIC m) {
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
	// TODO
	return result;
    }

    private static ArrayList<ArrayList<Float>> recenter(HashMap <ArrayList<Float>, ArrayList<ArrayList<Float>>> assignments) {
	ArrayList<ArrayList<Float>> result = new ArrayList<ArrayList<Float>>();
	// TODO
	return result;
    }

    // TODO: finish implementing
    private static ArrayList<ArrayList<Float>> kmeans(ArrayList<ArrayList<Float>> data, int k, UTILS.Constants.METRIC m, int mu) {
	ArrayList<ArrayList<Float>> means = new ArrayList<ArrayList<Float>>();
	means = initiate(k, data.get(0).size(), m);
	int count = 0;

	while (count < mu) {
	    HashMap <ArrayList<Float>, ArrayList<ArrayList<Float>>> assignments = assign(data, means, m);
	    means = recenter(assignments);
	    count += 1;
	}

	return means;
    }

    private static void list_all(ArrayList<ArrayList<Float>> data) {
	for (int i=0; i < data.size(); i++) {
	    ArrayList<Float> cur_point = data.get(i);
	    System.out.println(cur_point.get(0).toString() + "\t" + cur_point.get(1).toString());
	}
    }

    private static void list_means(ArrayList<ArrayList<Float>> means) {
	for (int i=0; i < means.size(); i++) {
	    ArrayList<Float> line = means.get(i);
	    System.out.println(line.get(0).toString() + "\t" + line.get(1).toString());
	}
    }

    public static void main(String[] args) {
	String data_file = "2D_data.txt";
	ArrayList<ArrayList<Float>> data = load_floats(data_file);
	list_all(data);

	ArrayList<ArrayList<Float>> means = kmeans(data, 4, Constants.METRIC.EUCLIDEAN, 10);
	list_means(means);
    }

}
