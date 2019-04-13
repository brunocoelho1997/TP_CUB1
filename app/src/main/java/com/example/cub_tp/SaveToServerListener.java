package com.example.cub_tp;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
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

    public SaveToServerListener(Context context) {
        this.jSch = new JSch();
        this.context = context;
    }



    @Override
    public void onClick(View v) {
        new RetrieveFeedTask().execute();
    }

    private void configureSFTPConnection() throws JSchException, SftpException {
        //jSch.addIdentity(privateKey);
        //Log.d("STPConnection", "STP Connection: Private Key Added.");
        //session = jSch.getSession(SFTPUSER,SFTPHOST,SFTPPORT);

        //create a session
        session = jSch.getSession(SFTPUSER,SFTPHOST,SFTPPORT);
        Log.d("STPConnection", "STP Connection: session created.");

        //config the session with the password
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setPassword(privateKey);

        session.connect();
        channel = session.openChannel("sftp");
        channel.connect();
        Log.d("STPConnection", "STP Connection: shell channel connected.....");

        channelSftp = (ChannelSftp)channel;
        channelSftp.cd(SFTP_WORKING_DIR);
        Log.d("STPConnection", "STP Connection: Changed the directory to: " + SFTP_WORKING_DIR);

    }


    class RetrieveFeedTask extends AsyncTask<String, Void, Void> {

        //font: https://stackoverflow.com/questions/6343166/how-do-i-fix-android-os-networkonmainthreadexception
        private Exception exception;

        protected Void doInBackground(String... urls) {
            try {
                if(channelSftp == null)
                    configureSFTPConnection();

                String filePathFrom = ANDROID_BASE_FILE_PATH + FILENAME + FILE_EXTENSION;

                String timeStamp = new SimpleDateFormat("dd_MM_yyyy_HHmmss").format(System.currentTimeMillis());
                String filePathTo = FILENAME + timeStamp + FILE_EXTENSION;

                channelSftp.put(filePathFrom, filePathTo);

                //clear the content of the file (deleting the file)
                File file = new File(filePathFrom);
                file.delete();

                Log.d("STPConnection", "STP Connection: The file was sent");

                Toast.makeText(context, "The file was sent", Toast.LENGTH_SHORT).show();

            } catch (JSchException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

                closeConnection();
            } catch (SftpException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                closeConnection();
            }catch (Exception e){
                closeConnection();
            }
            return null;
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