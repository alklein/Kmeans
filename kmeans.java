import java.net.*;
import java.util.*;
import java.io.*;

public class kmeans {

    public static ArrayList<ArrayList<Float>> load_floats(String filename) {
	ArrayList<ArrayList<Float>> result = new ArrayList<ArrayList<Float>>();
	try {
	    FileInputStream fis = new FileInputStream(filename);
	    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	    String line;
	    while ((line = br.readLine()) != null) {
		ArrayList<Float> cur_point = new ArrayList<Float>();
		String split_line[] = line.split("\\r?\\n");
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

    public static void main(String[] args) {
	String data_file = "2D_data.txt";
	ArrayList<ArrayList<Float>> data = load_floats(data_file);
	for (int i=0; i < data.size(); i++) {
	    ArrayList<Float> cur_point = data.get(i);
	    System.out.println(cur_point.get(0).toString() + "\t" + cur_point.get(1).toString());
	}
    }

}
