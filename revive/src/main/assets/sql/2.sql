CREATE TABLE ALL_DATA(type integer,name text);
SELECT AddGeometryColumn('ALL_DATA', 'GEOM', 4326, 'GEOMETRY','XY');
