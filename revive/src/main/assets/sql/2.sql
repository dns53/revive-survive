CREATE TABLE ALL_DATA(type integer,name text);
SELECT AddGeometryColumn('ALL_DATA', 'geom', 4326, 'GEOMETRY');
