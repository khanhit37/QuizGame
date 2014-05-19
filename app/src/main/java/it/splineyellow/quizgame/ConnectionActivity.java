package it.splineyellow.quizgame;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.sql.SQLException;


public class ConnectionActivity extends Activity {

    public final static String EXTRA_MESSAGE = "it.splineyellow.quizgame.MESSAGE";

    String dstAddress = "thebertozz.no-ip.org";
    int dstPort = 9533;

    String userData;

    String nick;
    int myID;
    String timestamp;
    String color;
    int turn;
    String[] categories;
    String enemyNick;

    UtentiDatabaseAdapter db = new UtentiDatabaseAdapter(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_activity);

        try {
            db.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        userData = db.getCurrentUser();
        db.close();

        new MyClientTask().execute();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.connection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class MyClientTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            DatagramSocket ds = null;
            try {
                InetAddress serverAddr = InetAddress.getByName(dstAddress);
                byte[] buffer = userData.getBytes();
                ds = new DatagramSocket();
                DatagramPacket dp;
                dp = new DatagramPacket(buffer, buffer.length, serverAddr, dstPort);
                ds.send(dp);
            } catch (Exception e) {
                e.printStackTrace();
            }

            InetAddress inetAddress = null;

            try {
                inetAddress = InetAddress.getByName(dstAddress);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            byte[] receiveBuffer = new byte[4096];

            while (true) {
                DatagramPacket datagramPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length, inetAddress, dstPort);


                try {
                    ds.receive(datagramPacket);
                    Log.v("Tentativo ricezione UDP: ", "In ricezione");
                } catch (NullPointerException n) {
                    n.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                String received = "Ricevuto da: " + datagramPacket.getAddress() + ", "
                        + datagramPacket.getPort() + ", "
                        + new String(datagramPacket.getData(), 0, datagramPacket.getLength());

                Log.v("Tentativo ricezione UDP: ", received);
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    public void goToStartGameActivity () {
        Intent intent = new Intent(this, StartGameActivity.class);
        String message = nick + "," + enemyNick + "," + color + "," + turn;
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void backToMenu() {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }

}