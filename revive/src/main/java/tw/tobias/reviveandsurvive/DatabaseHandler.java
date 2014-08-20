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
            boolean createDB = spatialDbFile.exists();

            db = new jsqlite.Database();
            db.open(spatialDbFile.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE
                    | jsqlite.Constants.SQLITE_OPEN_CREATE);
            Log.d(TAG, "Opening database");
            Log.d(TAG, "version: " + db.dbversion());
            if(createDB){
                createDB(c);
            }
        } catch (jsqlite.Exception e) {
            e.printStackTrace();
        }
    }
    private boolean createDB(Context c){
            try {

                String[] tmp= c.getAssets().list("sql");
                Arrays.sort(tmp);


                for(int i=0;i<tmp.length;i++){
                    Log.d(TAG,"Loading sql file: "+tmp[i]);

                    InputStream in=c.getAssets().open("sql/"+tmp[i]);
                    BufferedReader br=new BufferedReader(new InputStreamReader(in));
                    String line=br.readLine();
                    while(line!=null){
                        Log.d(TAG,"Running query: "+line);
                        Stmt stmt01 = db.prepare(line);
                        if (stmt01.step()) {
                            Log.d(TAG, "\n");
                        }
                    }

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
