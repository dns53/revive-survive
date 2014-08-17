package tw.tobias.reviveandsurvive;

import android.content.Context;

import java.io.File;

import jsqlite.*;

/**
 * Created by dns on 17/08/14.
 */
public class DatabaseHandler {
    private Database db;

    DatabaseHandler(Context c){
        try {

            //File sdcardDir = ResourcesManager.getInstance(context).getSdcardDir();


            File spatialDbFile = new File(c.getFilesDir(), "shp/italy.sqlite");

            if (!spatialDbFile.getParentFile().exists()) {
                throw new RuntimeException();
            }
            db = new jsqlite.Database();
            db.open(spatialDbFile.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE
                    | jsqlite.Constants.SQLITE_OPEN_CREATE);
        } catch (jsqlite.Exception e) {
            e.printStackTrace();
        }
    }
}
