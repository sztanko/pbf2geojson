fname=$1
#echo "cd /home/dimi/workspace/pbf2geojson; mvn clean compile package" | ssh vbox &> /dev/null
#java8=`which java`
java8="/Library/Java/JavaVirtualMachines/jdk1.8.0.jdk/Contents/Home/bin/java"
#opts="-server -Xmx8000M -Xms7800M -Xmn4000M -verbose:gc -XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:MaxGCPauseMillis=10"
opts="-server -Xmx9000M -Xms2800M -Xmn1500M -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:StartFlightRecording=duration=600s,filename=flightTroveCMulti1.jfr -XX:NewRatio=1 -XX:MaxDirectMemorySize=120G"
#opts="-server -Xmx9000M -Xms2800M -Xmn1500M -XX:MaxDirectMemorySize=120G"
#opts="-server -Xmx6000M -Xms5800M -Xmn2000M -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:StartFlightRecording=duration=600s,filename=flightTroveC.jfr"
#,settings=default"

#opts="-server -Xmx10000M -Xms8800M -verbose:gc -XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"

#fname="/Users/demetersztanko/tmp/england-latest.osm.pbf"
#fname="/Users/demetersztanko/tmp/greater-london-latest.osm.pbf"
# -XX:MinHeapFreeRatio=50"
time $java8 $opts -jar target/pbf2geojson-0.0.1-SNAPSHOT.jar $fname e.json
