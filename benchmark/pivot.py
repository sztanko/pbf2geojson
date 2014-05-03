#Summary:	Converting node to string	ForkJoinPool.commonPool-worker-7	8	16383	197.654	12.064
#Summary:	parseDense	ForkJoinPool.commonPool-worker-7	8	61	2220.585	36403.032
#Summary:	parseDense - map population	ForkJoinPool.commonPool-worker-7	8	488000	80.889	0.165
#Summary:	DenseNode inner cycle	ForkJoinPool.commonPool-worker-7	8	488000	73.038	0.149
#Summary:	writing json	ForkJoinPool.commonPool-worker-7	8	16383	507.888	31.0
#Summary:	Storing object	ForkJoinPool.commonPool-worker-7	8	488000	155.442	0.318
#Summary:	SimpleNode object population	ForkJoinPool.commonPool-worker-7	8	488000	65.85	0.134
#Summary:	Converting node to string	ForkJoinPool.commonPool-worker-1	8	62486	314.413	5.031
#Summary:	parseDense	ForkJoinPool.commonPool-worker-1	8	51	2245.481	44029.039
#Summary:	parseDense - map population	ForkJoinPool.commonPool-worker-1	8	408000	85.883	0.21
#Summary:	DenseNode inner cycle	ForkJoinPool.commonPool-worker-1	8	408000	62.78	0.153
#Summary:	DenseNode inner cycle	ForkJoinPool.commonPool-worker-2	8	304000	44.903	0.147
#Summary:	writing json	ForkJoinPool.commonPool-worker-2	8	10789	156.47	14.502
#Summary:	Storing object	ForkJoinPool.commonPool-worker-2	8	304000	89.166	0.293
#Summary:	SimpleNode object population	ForkJoinPool.commonPool-worker-2	8	304000	41.134	0.135
#Summary:	Converting node to string	main	8	2231	116.523	52.229
#Summary:	parseDense	main	8	13	603.425	46417.307
#Summary:	parseDense - map population	main	8	104000	36.473	0.35
#Summary:	ParallelReaderTest - reading blocks	main	8	1	396.651	396651.0

from collections import defaultdict

import sys
#0          1                               2       3   4       5       6
#Summary:   SimpleNode object population    main    1   2742661 178.216 0.064
#Summary:   SimpleNode object population    ForkJoinPool.commonPool-worker-4    8   326661  62.031  0.189
def summarize(f):
    sums = defaultdict(float)
    counts = defaultdict(int)
    for line in file(f):
        r = line.strip().split('\t')
        k = r[1]
        c = int(r[4])
        s = float(r[5])
        sums[k]+=s
        counts[k]+=c
    out = {}
    #print sums
    for k in counts:
        out[k] = round(sums[k]*1000.0/counts[k], 3)
    return [out, sums]

single = summarize(sys.argv[1])
multi = summarize(sys.argv[2])

toPrint = []
for k in single[0]:
    s = "%s\t%.3f\t%.3f\t%.3f\t%.3f" %(k, single[0][k], multi[0][k], multi[1][k], multi[0][k] / single[0][k])
    toPrint.append([multi[0][k]/single[0][k], s])
toPrint.sort()
for k in toPrint:
    print k[1]
