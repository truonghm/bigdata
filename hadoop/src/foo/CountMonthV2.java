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

public class CountMonthV2 {
	public static class CountMonthMapper extends Mapper<Object, Text, Text, IntWritable> {
		private Text yearMonth = new Text();
		private IntWritable temperature = new IntWritable();
		private int threshold;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            this.threshold = context.getConfiguration().getInt("threshold", 0);
        }

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			String[] fields = value.toString().split(":");
			String y = fields[2].trim();
			String m = fields[1].trim();
			String my = m + "-" + y;
			int temp = Integer.parseInt(fields[3].trim());
			yearMonth.set(my);
			temperature.set(temp);
			
			if (temp > threshold) {
				context.write(yearMonth, temperature);
			}
		}
	}

	public static class CountMonthReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {

			context.write(key, null);
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
        int threshold = Integer.parseInt(args[2]);
        conf.setInt("threshold", threshold);

		Job job = Job.getInstance(conf, "count month");
		job.setJarByClass(CountMonthV2.class);
		job.setMapperClass(CountMonthMapper.class);
		job.setReducerClass(CountMonthReducer.class);
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