package dimi.stats;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;

public class StatsCollector {
	final private static ThreadLocal<StatsCollector> t = new ThreadLocal<StatsCollector>(){
        @Override protected StatsCollector initialValue() {
            return new StatsCollector();
    }
};
	private static long ts = -1;
	public static int numThreads;
	final private static ConcurrentMap<StatsCollector,Object> threads = new ConcurrentHashMap<StatsCollector, Object>();
	
	public static void reset()
	{
		ts = -1;
		threads.clear();
	}
	
	public static StatsCollector getInstance() {
		if (ts == -1) {
			ts = System.nanoTime();
		}
		final StatsCollector sc = t.get();
		if(sc==null)
		{
			System.out.println("Strange");
		}
		threads.putIfAbsent(sc, 't');
		numThreads=threads.size();
		return sc;
	}

	private TObjectLongMap<String> counters;
	private TObjectLongMap<String> sums;

	private final long[] q;
	private int c;

	private final String threadName;
	
	public StatsCollector() {
		q = new long[500];
		c = 0;
		counters = new TObjectLongHashMap<String>();
		sums = new TObjectLongHashMap<String>();
		threadName = Thread.currentThread().getName();
	}

	public StatsCollector start() {
		final long startTs = System.nanoTime();
		q[c] = startTs;
		c++;
		return this;
	}
	
	public StatsCollector endWithFlag(final String name, final int count, final boolean printFlag)
	{
		c--;
		
		final long endTs = System.nanoTime();
		final  long sTs = q[c];
		final int numT = Thread.activeCount();
		final long diff = endTs-sTs;
		counters.adjustOrPutValue(name, 1l, 1l);
		sums.adjustOrPutValue(name, diff, diff);
		if(false && (printFlag || diff>10000000))
		{
		String s = name
				+"\t"+threadName
				+"\t"+StatsCollector.numThreads
				+"\t"+numT
				+"\t"+(endTs-ts)/1000000
				+"\t"+(diff)/1000/1000.0
				+"\t"+count;
		System.out.println(s);
		
		}
		return this;
	}
	public StatsCollector end(final String name, int count)
	{
		return endWithFlag(name, count, true);
	}
	public StatsCollector endSilent(final String name, int count)
	{
		return endWithFlag(name, count, false);
	}
	
	public void printSummary()
	{
		for(String key: counters.keySet())
		{
			final String s= "Summary:"
					+"\t"+key
					+"\t"+threadName
					+"\t"+StatsCollector.numThreads
					+"\t"+counters.get(key)
					+"\t"+sums.get(key)/1000000.0
					+"\t"+sums.get(key)/counters.get(key)/1000.0;
			//System.out.println(s);
		}
	}
	
	public static void printSummaryForAll(){
		threads.keySet().stream().forEach(t->t.printSummary());	
	}
	
	
	public int hashCode()
	{
		return threadName.hashCode();
	}
	
	public boolean equals(StatsCollector other)
	{
		return threadName.equals(other.threadName);
	}
	
	/*public static void printSummary()
	{
		t.
	}*/
}
