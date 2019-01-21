import java.util.*;
import java.io.*;

import org.apache.hadoop.mapred.*;

import org.apache.hadoop.io.*;
import org.apache.hadoop.fs.*;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.util.*;

public class AdPredictor extends Configured implements Tool {
	
	public static class Feature {
	
		private int x [];
		private int M;
	
		public Feature(String s) {
			
			int i;
			String [] values = s.split(",");
			M = values.length;
			x = new int [M];
			for (i = 0; i < values.length; i++)
				x[i] = Integer.parseInt(values[i]);
		}
	
		public boolean isValid() {
			int i, sum = 0;
			for (i = 0; i < M; i++) sum += x[i];
			return (sum == 1);
		}
	
		public int size() { return M; }
	
		public int elementAt(int i) { return x[i]; }

		public String toString() {
			String s = "[";
			int i;
			for (i = 0; i < M; i++) {
				s += x[i];
				s += ((i < M-1) ? "," : "] ");
			}
			return "[M=" + M + "]" + s;
		}
	}
	
	public static class Impression {
	
		private int N;
		private Feature X [];
		private boolean clicked = false;
		
		public Impression (String s) throws Exception {
			int i;
			String [] features = s.split(":");
			N = features.length -1;
			X = new Feature [N];
			for (i = 0; i < N; i++) {
				X[i] = new Feature(features[i]);
				if (! X[i].isValid())
					throw new Exception ("Feature " + features[i] + " is not valid.");
			}
			clicked = (Integer.parseInt(features[N]) == 1);
		}
		
		private String _click_() {
			return (clicked) ? "Clicked" : "";
		}
		
		public int isClicked() {
			return (clicked) ? 1 : -1;
		}
	
		public String toString() {
			String s = "";
			int i;
			for (i = 0; i < N; i++) s += X[i];
			return "[N=" + N + "]: " + s + _click_();
		}
	
		public int [][] toArray() {
			int i, j, M;
			int [][] A = new int [N][];
			for (i = 0; i < N; i++) {
				M = X[i].size();
				A[i] = new int [M];
				for (j = 0; j < M; j++)
					A[i][j] = X[i].elementAt(j);
			}
			return A;
		}
	}
	
	public static class AdPredictorMapper 
		
		extends MapReduceBase 
		
		implements Mapper<LongWritable, Text, Text, IntWritable> {
		
		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();

		public void configure (JobConf configuration) {
			return;
		}

		public void map (LongWritable key, Text value, 
			
			OutputCollector<Text, IntWritable> output, Reporter reporter) 
			
			throws IOException {
			
			String line = value.toString();
			try {
				Impression impression = new Impression(line);
				word.set(impression.toString());
				output.collect(word, one);
			} catch (Exception e) {}			
		}
	}

	public static class AdPredictorReducer 
		
		extends MapReduceBase 
		
		implements Reducer<Text, IntWritable, Text, IntWritable> {

		public void reduce (Text key, Iterator<IntWritable> values,
			
			OutputCollector<Text, IntWritable> output, Reporter reporter) 
			
			throws IOException {
			
			/* Reduce task */
			int sum = 0;
			while (values.hasNext()) {
				sum += values.next().get();
			}
			output.collect(key, new IntWritable(sum));
		}
	}

	public int run (String[] args) throws Exception {
		
		JobConf configuration = new JobConf(getConf(), AdPredictor.class);
		configuration.setJobName("MyAdPredictor");

		configuration.setMapperClass (AdPredictorMapper.class);
			
		configuration.setCombinerClass (AdPredictorReducer.class);
		configuration.setReducerClass  (AdPredictorReducer.class);
		
		configuration.setInputFormat  (TextInputFormat.class);
		configuration.setOutputFormat (TextOutputFormat.class);
		
		configuration.setOutputKeyClass   (Text.class);
		configuration.setOutputValueClass (IntWritable.class);
 		
		List<String> other_args = new ArrayList<String>();
			for (int i=0; i < args.length; ++i) {
				System.out.println("argument " + i + " is " + args[i]);
				if ("-libjars".equals(args[i])) {
					++i;
				} else {
					other_args.add(args[i]);
			}
		}
		String input = other_args.get(0);
		String output = other_args.get(1);

		FileInputFormat.setInputPaths  (configuration, new Path( input));
		FileOutputFormat.setOutputPath (configuration, new Path(output));
			
		JobClient.runJob(configuration);
		return 0;
	}
		
	public static void main(String[] args) throws Exception {

		int result = ToolRunner.run(new Configuration(), new AdPredictor(), 
			args);
		
		System.out.println("Bye.");
		System.exit(result);

	}
}

