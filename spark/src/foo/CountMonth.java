package foo;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;

public class CountMonth {

	public static void main(String[] args) {
		String inputFile = "materials/spark-subject/meteosample.txt";
		int threshold = 20;

		SparkConf conf = new SparkConf().setAppName("CountMonth");
		JavaSparkContext sc = new JavaSparkContext(conf);

		long t1 = System.currentTimeMillis();

		JavaRDD<String> lines = sc.textFile(inputFile);

		JavaRDD<String> monthAboveThreshold = lines.mapToPair(w -> {
			String[] parts = w.split(":");
			String year = parts[2].trim();
			String month = parts[1].trim();
			String monthYear = month + "-" + year;
			int temperature = Integer.parseInt(parts[3].trim());
			return new Tuple2<String, Integer>(monthYear, temperature);
		}).filter(pair -> pair._2() > threshold)
		// .map(pair -> pair._1())
		.keys()
		.distinct();

		long count = monthAboveThreshold.count();

		System.out.println("======================");
		System.out.println("Number of months with temperature above " + threshold + ": " + count);
		System.out.println("======================");

		long t2 = System.currentTimeMillis();

		System.out.println("======================");
		System.out.println("time in ms :" + (t2 - t1));
		System.out.println("======================");

	}
}