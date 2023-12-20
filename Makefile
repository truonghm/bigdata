URL = https://sd-160040.dedibox.fr/hagimont/resources-N7/bigdata/3IN-bigdata.html
DIR = materials
FILES = https://sd-160040.dedibox.fr/hagimont/software/jdk-8u202-linux-x64.tar.gz \
        https://sd-160040.dedibox.fr/hagimont/software/eclipse-jee-2019-09-R-linux-gtk-x86_64.tar.gz \
        https://sd-160040.dedibox.fr/hagimont/software/hadoop-2.7.1.tar.gz \
        https://sd-160040.dedibox.fr/hagimont/software/spark-2.4.3-bin-hadoop2.7.tgz

HADOOP_JAR_PATHS := lib/*:${HADOOP_HOME}/share/hadoop/common/hadoop-common-2.7.1.jar:${HADOOP_HOME}/share/hadoop/mapreduce/hadoop-mapreduce-client-common-2.7.1.jar:${HADOOP_HOME}/share/hadoop/mapreduce/hadoop-mapreduce-client-core-2.7.1.jar

SPARK_JAR_PATHS := lib/*:${SPARK_HOME}/jars/spark-core_2.11-2.4.3.jar:${SPARK_HOME}/jars/scala-library-2.11.12.jar:${SPARK_HOME}/jars/hadoop-common-2.7.3.jar

materials: download-slides download-softwares

download-slides:
	mkdir -p $(DIR)
	wget --recursive --no-parent --reject="html,htm" --no-directories --timeout=30 --tries=5 --waitretry=5 --directory-prefix=$(DIR) $(URL)
	find $(DIR) -name "*.html" -type f -delete
	find $(DIR) -name "*.htm" -type f -delete
	find $(DIR) -name "*.tmp" -type f -delete

download-softwares:
	mkdir -p $(DIR)
	$(foreach file,$(FILES),wget -P $(DIR) $(file);)

.PHONY: all download_site download_files

start-hdfs:
	hdfs namenode -format 
	start-dfs.sh
	jps 

build-hadoop:
	mkdir -p hadoop/bin
	javac -cp $(HADOOP_JAR_PATHS) -d hadoop/bin hadoop/src/foo/*.java
	jar cf hadoop.jar -C hadoop/bin foo

prepare-wordcount:
	-hdfs dfs -mkdir /input-wc
	-hdfs dfs -put materials/hadoop-subject/filesample.txt /input-wc

run-wordcount:
	-hdfs dfs -rm -r /output-wc
	hadoop jar hadoop.jar foo.WordCount /input-wc /output-wc
	hdfs dfs -cat '/output-wc/*'

stop-hdfs:
	stop-dfs.sh
	-rm -rf /tmp/hadoop-truonghm
	jps

wordcount: build-hadoop prepare-wordcount run-wordcount

prepare-maxtemp:
	-hdfs dfs -mkdir /input-maxtemp
	-hdfs dfs -put materials/hadoop-subject/meteosample.txt /input-maxtemp

run-maxtemp:
	-hdfs dfs -rm -r /output-maxtemp
	hadoop jar hadoop.jar foo.MaxTemp /input-maxtemp /output-maxtemp
	hdfs dfs -cat '/output-maxtemp/*'

maxtemp: build-hadoop prepare-maxtemp run-maxtemp

prepare-countmonth:
	-hdfs dfs -mkdir /input-countmonth
	-hdfs dfs -put materials/hadoop-subject/meteosample.txt /input-countmonth

run-countmonth:
	-hdfs dfs -rm -r /output-countmonth
	hadoop jar hadoop.jar foo.CountMonthV2 /input-countmonth /output-countmonth 10
	hdfs dfs -cat '/output-countmonth/*' | wc -l

countmonth: build-hadoop prepare-countmonth run-countmonth

build-spark:
	mkdir -p spark/bin
	javac -cp $(SPARK_JAR_PATHS) -d spark/bin spark/src/foo/*.java
	jar cf spark.jar -C spark/bin foo

run-wordcount-spark:
	-rm -rf result-maxtemp
	spark-submit --class foo.WordCount --master local spark.jar
	cat result-wordcount/part-00000

wordcount-spark: build-spark run-wordcount-spark

run-maxtemp-spark:
	-rm -rf result-maxtemp
	spark-submit --class foo.MaxTemp --master local spark.jar
	cat result-maxtemp/part-00000

maxtemp-spark: build-spark run-maxtemp-spark

run-countmonth-spark:
	spark-submit --class foo.CountMonth --master local spark.jar

countmonth-spark: build-spark run-countmonth-spark