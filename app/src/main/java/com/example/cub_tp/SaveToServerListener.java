package com.example.cub_tp;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.text.SimpleDateFormat;

import static com.example.cub_tp.Config.*;

public class SaveToServerListener implements View.OnClickListener {

    JSch jSch = null;
    Session session = null;
    Channel channel = null;
    ChannelSftp channelSftp = null;
    Context context;
    Button btnSaveToServer;

    public SaveToServerListener(Context context, Button btnSaveToServer) {
        this.jSch = new JSch();
        this.context = context;
        this.btnSaveToServer = btnSaveToServer;
    }

    @Override
    public void onClick(View v) {
        new RetrieveFeedTask(context).execute();
    }

    private void configureSFTPConnection() throws Exception {

        boolean result = FileManager.loadFileServerConfigData();

        if(!result)
            throw new Exception("error");

        //create a session
        session = jSch.getSession(FileManager.sftUser,FileManager.sftHost,FileManager.sftPort);
        Log.d("STPConnection", "STP Connection: session created.");

        //config the session with the password
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setPassword(FileManager.privateKey);

        session.connect();
        channel = session.openChannel("sftp");
        channel.connect();
        Log.d("STPConnection", "STP Connection: shell channel connected.....");

        channelSftp = (ChannelSftp)channel;
        channelSftp.cd(FileManager.sftWorkingDir);
        Log.d("STPConnection", "STP Connection: Changed the directory to: " + FileManager.sftWorkingDir);

    }


    class RetrieveFeedTask extends AsyncTask<String, Void, Void> {

        private Context context;

        private boolean fileSent = false;

        public RetrieveFeedTask(Context context) {
            this.context = context;
        }

        protected Void doInBackground(String... urls) {
            try {
                if(channelSftp == null)
                {
                    try{
                        //if the config file of the server isn't defined or wrong defined will be catch the exception
                        configureSFTPConnection();
                    }catch (Exception e)
                    {
                        fileSent = false;
                    }

                }

                String timeStamp = new SimpleDateFormat("dd_MM_yyyy_HHmmss").format(System.currentTimeMillis());
                //for csv file
                String filePathFrom = ANDROID_BASE_FILE_PATH + FILENAME + FILE_EXTENSION;
                File file = new File(filePathFrom);
                if(file.exists())
                {
                    String filePathTo = FILENAME + timeStamp + FILE_EXTENSION;
                    channelSftp.put(filePathFrom, filePathTo);
                    //clear the content of the file (deleting the file)
                    file.delete();
                }

                //for arff file
                filePathFrom = ANDROID_BASE_FILE_PATH + FILENAME + FILE_EXTENSION_ARFF;
                file = new File(filePathFrom);
                if(file.exists())
                {
                    String filePathTo = FILENAME + timeStamp + FILE_EXTENSION_ARFF;
                    channelSftp.put(filePathFrom, filePathTo);
                    //clear the content of the file (deleting the file)
                    file = new File(filePathFrom);
                    file.delete();
                }

                fileSent = true;
                Log.d("STPConnection", "STP Connection: The file was sent");

            }catch (Exception e){
                fileSent = false;
                closeConnection();
                Log.d("SaveToServerListener", "" + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(fileSent)
            {
                btnSaveToServer.setEnabled(false);
                Toast.makeText(context, "The file was sent", Toast.LENGTH_LONG).show();
            }
            else
            {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.server_wrong_data_title)
                        .setMessage(context.getString(R.string.server_worng_data_desc) + Config.FILENAME_SERVER_CONFIG + "" + Config.FILE_EXTENSION_SERVER_CONFIG )

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }

        public void closeConnection(){
            if(channelSftp!=null){
                channelSftp.disconnect();
                channelSftp.exit();
            }
            if(channel!=null) channel.disconnect();

            if(session!=null) session.disconnect();
        }
    }
}