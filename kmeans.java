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

    private static ArrayList<Float> initiate(int len, UTILS.Constants.METRIC m) {
	ArrayList<Float> result = new ArrayList<Float>();
	Random r = new Random();
	if (m == Constants.METRIC.EUCLIDEAN) {
	    for (int i=0; i < len; i++) {
		float next = r.nextFloat();
		result.add(next);
	    }
	} else if (m == Constants.METRIC.EDIT_DISTANCE) {
	    // TODO
	}
	return result;
    }

    private static ArrayList<Float> kmeans(ArrayList<ArrayList<Float>> data, int k, UTILS.Constants.METRIC m) {
	ArrayList<Float> result = new ArrayList<Float>();
	result = initiate(data.get(0).size(), m);
	// TODO

	///////
	return result;
    }

    private static void list_all(ArrayList<ArrayList<Float>> data) {
	for (int i=0; i < data.size(); i++) {
	    ArrayList<Float> cur_point = data.get(i);
	    System.out.println(cur_point.get(0).toString() + "\t" + cur_point.get(1).toString());
	}
    }

    private static void list_line(ArrayList<Float> line) {
	System.out.println(line.get(0).toString() + "\t" + line.get(1).toString());
    }

    public static void main(String[] args) {
	String data_file = "2D_data.txt";
	ArrayList<ArrayList<Float>> data = load_floats(data_file);
	list_all(data);

	ArrayList<Float> means = kmeans(data, 4, Constants.METRIC.EUCLIDEAN);
	list_line(means);
    }

}
