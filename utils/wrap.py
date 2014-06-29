#!/usr/bin/python
import sys
import json

#Purpose of this scirpt is to make a single geoJSON out of file that contains a separate geojson file in every line.

out={}
out['type']='FeatureCollection'
out['features']=[]

c=1;
for line in sys.stdin:
    j=json.loads(line)
    if not 'properties' in j:
            continue
    f={'type': 'Feature'}
    f['properties']=j['properties']
    f['id']=c
    j['properties']=None
    del j['properties']
    f['geometry']=j
    out['features'].append(f)
    c+=1
out['properties']={'count':len(out)}
print json.dumps(out)
