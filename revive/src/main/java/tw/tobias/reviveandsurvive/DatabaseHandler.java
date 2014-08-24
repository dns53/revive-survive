package tw.tobias.reviveandsurvive;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Exception;
import java.util.*;


import jsqlite.*;

import android.util.Log;

/**
 * Created by dns on 17/08/14.
 */
public class DatabaseHandler {
    private Database db;
    private static final String TAG = "DatabaseHandler";


    DatabaseHandler(Context c) {
        try {

            File spatialDbFile = new File(c.getFilesDir(), "revive.sqlite");

            if (!spatialDbFile.getParentFile().exists()) {
                throw new RuntimeException();
            }
            boolean exists = spatialDbFile.exists();
            Log.d(TAG,"Does the db "+spatialDbFile+" exist? "+ exists);

            db = new jsqlite.Database();
            db.open(spatialDbFile.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE
                    | jsqlite.Constants.SQLITE_OPEN_CREATE);

            Log.d(TAG, "Opening database");
            Log.d(TAG, "version: " + db.dbversion());
            if(!createDB(c,exists)) {
                throw new RuntimeException();
            }
        } catch (jsqlite.Exception e) {
            e.printStackTrace();
        }
    }
    private boolean createDB(Context c,boolean exists){
            try {

                // See if we have a create_log table
                if(!exists) {
                    Log.d(TAG, "DB does not exist, create SQL_LOG table");

                    //The create_log table does not exist
                    String query="create table SQL_LOG(file text)";
                    Stmt stmt01 = db.prepare(query);
                    if(stmt01.step()) {
                        Log.d(TAG, "Error in create");
                        stmt01.close();
                        return false;
                    }
                    stmt01.close();
                }

                LinkedList<String> processed=new LinkedList();
                Stmt create3=db.prepare("select file from SQL_LOG;");
                while(create3.step()) {
                    processed.add(create3.column_string(0));
                }
                create3.close();

                String[] tmp= c.getAssets().list("sql");
                Arrays.sort(tmp);

                for(int i=0;i<tmp.length;i++) {
                    Log.d(TAG, "Loading sql file: " + tmp[i]);

                    if (processed.contains(tmp[i])) {
                        Log.d(TAG, "The file " + tmp[i]+" has already been run on this db");
                    }
                    else{
                        InputStream in = c.getAssets().open("sql/" + tmp[i]);
                        BufferedReader br = new BufferedReader(new InputStreamReader(in));
                        String line = br.readLine();
                        while (line != null) {
                            Log.d(TAG, "Running query: " + line);
                            Stmt stmt01 = db.prepare(line);
                            if (stmt01.step()) {
                                Log.d(TAG, "\n");
                            }
                            stmt01.close();
                            line=br.readLine();
                        }
                        Stmt stmt01 = db.prepare("insert into SQL_LOG(file) values('"+tmp[i]+"');");
                        stmt01.step();
                        stmt01.close();
                    }
                }

                Stmt create4=db.prepare("select * from SQL_LOG;");
                while(create4.step()) {
                    Log.d(TAG,"SQL LOG: " +create4.column_string(0));
                }

                return true;

            } catch (IOException e) {
                e.printStackTrace();
                return false;

        } catch (jsqlite.Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    private void loadFiles(){

    }
}
