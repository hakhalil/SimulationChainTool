
import clr
import csv
import math
import os
import sys

clr.AddReference('RevitAPI') 
clr.AddReference('RevitAPIUI') 
from Autodesk.Revit.DB import * 
 
walls = FilteredElementCollector(doc).OfClass(Wall).ToElements()
get_coords = lambda w: (w.X, w.Y, w.Z)

csv_name = "%s.csv" % os.path.splitext(os.path.basename(doc.PathName))[0]
header = ("id", "wall_len", "src_x", "src_y", "src_z", "dst_x", "dst_y", "dst_z")

with open(csv_name, 'wb') as csv_file:
	csv_writer = csv.writer(csv_file, delimiter=',')
	csv_writer.writerow(header)
	for wall in walls:
		xyz0 = wall.Location.Curve.GetEndPoint(0)
		xyz1 = wall.Location.Curve.GetEndPoint(1)
		wall_id = wall.Id.ToString()
		wall_len = xyz0.DistanceTo(xyz1)
		#wall_type = wall.GetType().ToString()
		csv_writer.writerow((wall_id, wall_len) + get_coords(xyz0) + get_coords(xyz1))
