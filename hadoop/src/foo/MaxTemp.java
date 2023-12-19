package foo;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class MaxTemp {
	public static class MaxTempMapper extends Mapper<Object, Text, Text, IntWritable> {
		private Text year = new Text();
		private IntWritable temperature = new IntWritable();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			String[] fields = value.toString().split(":");
			// String y = fields[2];
			// int temp = Integer.parseInt(fields[3]);
			year.set(fields[2].trim());
			temperature.set(Integer.parseInt(fields[3].trim()));

			context.write(year, temperature);
		}
	}

	public static class MaxTempReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable result = new IntWritable();

		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			int maxTemp = -273;
			for (IntWritable value : values) {
				maxTemp = Integer.max(maxTemp, value.get());
			}

			result.set(maxTemp);
			context.write(key, result);
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "Max Temperature");
		job.setJarByClass(MaxTemp.class);
		job.setMapperClass(MaxTempMapper.class);
		job.setReducerClass(MaxTempReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		long t1 = System.currentTimeMillis();
		int res = job.waitForCompletion(true) ? 0 : 1;
		long t2 = System.currentTimeMillis();
		System.out.println("time in ms =" + (t2 - t1));
		System.exit(res);
	}
}
