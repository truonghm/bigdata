package foo;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;

public class MaxTemp {

	public static void main(String[] args) {
		String inputFile = "materials/spark-subject/meteosample.txt";
		String outputFile = "result-maxtemp";

		SparkConf conf = new SparkConf().setAppName("MaxTemp");
		JavaSparkContext sc = new JavaSparkContext(conf);

		long t1 = System.currentTimeMillis();

		JavaRDD<String> data = sc.textFile(inputFile).flatMap(s -> Arrays.asList(s.split("\n")).iterator());

		JavaPairRDD<String, Integer> maxTemp = data.mapToPair(w -> {
			String[] parts = w.split(":");
			String year = parts[2].trim();
			int temperature = Integer.parseInt(parts[3].trim());
			return new Tuple2<String, Integer>(year, temperature);
		}).reduceByKey((c1, c2) -> Integer.max(c1, c2));
		
		maxTemp.saveAsTextFile(outputFile);

		long t2 = System.currentTimeMillis();

		System.out.println("======================");
		System.out.println("time in ms :" + (t2 - t1));
		System.out.println("======================");

	}
}