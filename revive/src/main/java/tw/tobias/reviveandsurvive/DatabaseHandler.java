package tw.tobias.reviveandsurvive;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Exception;
import java.util.*;


import jsqlite.*;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by dns on 17/08/14.
 */
public class DatabaseHandler {
    private Database db;
    private static final String TAG = "DatabaseHandler";

    AssetManager am;

    DatabaseHandler(Context c) {
        am=c.getAssets();

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
            loadFiles();

        } catch (jsqlite.Exception e) {
            e.printStackTrace();
        } catch (IOException e) {
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
    private void loadFiles() throws IOException, jsqlite.Exception {
        LinkedList<String> processed=new LinkedList();
        Stmt create3=db.prepare("select filename from IMPORT_LOG;");
        while(create3.step()) {
            processed.add(create3.column_string(0));
        }

        String[] res = am.list("csv");
        for (int i = 0; i < res.length; i++) {
            if (!processed.contains(res[i])) {
                Log.d(TAG, "loading from: " + res[i]);
                new loadCsv().execute(res[i]);
            }
            else{
                Log.d(TAG, res[i]+"already loaded");
            }
        }
    }

    private void load(String file) throws IOException,jsqlite.Exception {
        Log.d(TAG, "Loading file: "+file);
        InputStream in = am.open("csv/" + file);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line = br.readLine();
        Stmt stmt01 = db.prepare("insert into ALL_DATA(TYPE,NAME,GEOM) values (?,?,ST_GeomFromText(?));");

        while (line != null) {


            StringTokenizer st = new StringTokenizer(line, "|");
            int index1=line.indexOf('|');
            int id=Integer.parseInt(line.substring(0, index1));
            int index2=line.indexOf('|',index1+1);
            String name=line.substring(index1+1,index2);
            String geom=line.substring(index2+1,line.length());


/*                    Log.d(TAG, "token1 "+st.nextToken());
                    Log.d(TAG, "token2 "+st.nextToken());
                    Log.d(TAG, "token3 "+st.nextToken());
*/

            stmt01.bind(1, id);
            stmt01.bind(2, name);
            stmt01.bind(3, geom);
            stmt01.step();
            stmt01.reset();
            line = br.readLine();
        }
        stmt01.close();
        Stmt stmt02 = db.prepare("insert into IMPORT_LOG(filename) values (?);");
        stmt02.bind(1,file);
        stmt02.step();
        stmt02.close();

        Log.d(TAG, "Finished loading file: "+file);
    }

    private class loadCsv extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                for(int i=0;i<strings.length;i++) {
                    load(strings[i]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (jsqlite.Exception e) {
                e.printStackTrace();
            }
            return Boolean.TRUE;
        }

    }

}
