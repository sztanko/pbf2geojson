echo "cd /home/dimi/workspace/pbf2geojson; mvn clean compile package" | ssh vbox &> /dev/null
java8="/Library/Java/JavaVirtualMachines/jdk1.8.0.jdk/Contents/Home/bin/java"
opts="-server -Xmx10000M -Xms7800M -Xmn5000M -verbose:gc -XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:MaxGCPauseMillis=50"
fname="/Users/demetersztanko/tmp/england-latest.osm.pbf"
# -XX:MinHeapFreeRatio=50"
rm -rf out.*.txt

echo "Single"
time $java8 $opts -jar target/pbf2geojson-0.0.1-SNAPSHOT.jar $fname a.json single &> out.single.all.txt
echo 
echo
echo "multi"
time $java8 $opts -jar target/pbf2geojson-0.0.1-SNAPSHOT.jar $fname a.json multi &> out.multi.all.txt

cat out.single.all.txt |  grep Summary > out.single.summary.txt
cat out.multi.all.txt | grep Summary > out.multi.summary.txt
python pivot.py out.single.summary.txt out.multi.summary.txt 

