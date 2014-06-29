
import sys

import json

for line in sys.stdin:
    l = json.loads(line)
    if 'properties' in l
    p = l['properties']
    for k in p:
        print "%s\t%s" %(k, p[k])

