package de.uhrenbastler.watchcheck.googledrive;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.uhrenbastler.watchcheck.tools.Logger;

/**
 * Created by clorenz on 27.03.15.
 */
public class SyncDownloader extends GoogleDriveClient {

    @Override
    protected void onStart() {
        super.onStart();
        DriveId driveId = DriveId.decodeFromString("watchcheck3.dat");
        DriveFile file = Drive.DriveApi.getFile(mGoogleApiClient, driveId);

        file.open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(DriveApi.DriveContentsResult result) {
                if (!result.getStatus().isSuccess()) {
                    // Handle error
                    return;
                }
                DriveContents driveContents = result.getDriveContents();
                InputStream data = null;
                InputStreamReader isr = null;
                BufferedReader reader = null;
                try {
                    data = driveContents.getInputStream();
                    isr = new InputStreamReader(data);
                    reader = new BufferedReader(isr);
                    String line=null;

                    while ( (line = reader.readLine()) != null )  {
                        Logger.debug("Line= "+line);
                    }
                } catch ( Exception e) {
                    Logger.error("Error on reading watchcheck3.dat from Google Drive: ",e);
                } finally {
                    if ( reader!=null ) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if ( isr!=null ) {
                        try {
                            isr.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if ( data!=null ) {
                        try {
                            data.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

    }
}
