#!/usr/bin/python
import json
import sys

def fu(a):
    if type(a)==int:
        return str(a)
    if type(a)==str:
        return a
    if type(a)==unicode:
        return a.encode("utf-8")
    return str(a)


for line in sys.stdin:
    try:
        j = json.loads(line)
        if not 'properties' in j:
            continue
        p = j['properties']
        for k in p:
            print "%s\t%s" %(fu(k),fu(p[k]))
    except:
        pass
