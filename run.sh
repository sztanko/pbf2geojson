#echo "cd /home/dimi/workspace/pbf2geojson; mvn clean compile package" | ssh vbox &> /dev/null
java8=`which java`
java8="/Library/Java/JavaVirtualMachines/jdk1.8.0.jdk/Contents/Home/bin/java"
#opts="-server -Xmx6000M -Xms5800M -Xmn2000M -verbose:gc -XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:MaxGCPauseMillis=10"
opts="-server -Xmx6000M -Xms5800M -Xmn2000M"

#opts="-server -Xmx10000M -Xms8800M -verbose:gc -XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"

fname=$1
#fname="/Users/demetersztanko/tmp/england-latest.osm.pbf"
#fname="/Users/demetersztanko/tmp/greater-london-latest.osm.pbf"
# -XX:MinHeapFreeRatio=50"
time $java8 $opts -jar target/pbf2geojson-0.0.1-SNAPSHOT.jar $fname /dev/stdout
