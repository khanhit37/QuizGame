package it.splineyellow.quizgame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class StartGameActivity extends Activity {

    public final static String EXTRA_MESSAGE = "it.splineyellow.quizgame.MESSAGE";

    String dstAddress = "thebertozz.no-ip.org";
    int dstPort = 9533;

    int actualCategoryPosition;

    int turn;
    int myID;

    public static final String TAG = "onItemClick --> posizione : ";

    public Integer[] mThumbIds = {};

    String questionData;

    InetAddress serverAddr;

    String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);

        Intent intent = getIntent();
        try {
            message = intent.getStringExtra(ConnectionActivity.EXTRA_MESSAGE);
        } catch (NullPointerException e) {
            e.printStackTrace();
            message = intent.getStringExtra(ScoreActivity.EXTRA_MESSAGE);
        }
        final String[] categories = message.toLowerCase().split(",");

        mThumbIds = categoriesOrder(categories);
        turn = Integer.parseInt(categories[0]);
        myID = Integer.parseInt(categories[1]);

        setTitle("Nuova Partita");

        TextView textView = (TextView) findViewById(R.id.placeholder);
        textView.setText("Turno: " + categories[0]);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                actualCategoryPosition = position;
                goToQuestion(categories[actualCategoryPosition+2]);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){

        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu){
        MenuItem item = menu.findItem(R.id.action_settings);

        try {
            item.setVisible(false);
        } catch (NullPointerException n) {
            n.printStackTrace();
        }

        return super.onPrepareOptionsMenu(menu);
    }

    public class ImageAdapter extends BaseAdapter {

        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }


        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(mThumbIds[position]);

            imageView.setLayoutParams(new GridView.LayoutParams(
                    (int)mContext.getResources().getDimension(R.dimen.width),
                    (int)mContext.getResources().getDimension(R.dimen.height)));


            return imageView;
        }

    }

    public Integer[] categoriesOrder(String[] categories) {
        Integer[] categoriesId = {R.drawable.arte, R.drawable.cinema, R.drawable.geografia,
                R.drawable.informatica, R.drawable.letteratura, R.drawable.matematica,
                R.drawable.musica, R.drawable.sport, R.drawable.storia};

        for (int i = 2; i <= 10; i++) {
            if (categories[i].equals("arte")) categoriesId[i-2] = R.drawable.arte;
            if (categories[i].equals("cinema")) categoriesId[i-2] = R.drawable.cinema;
            if (categories[i].equals("geografia")) categoriesId[i-2] = R.drawable.geografia;
            if (categories[i].equals("informatica")) categoriesId[i-2] = R.drawable.informatica;
            if (categories[i].equals("letteratura")) categoriesId[i-2] = R.drawable.letteratura;
            if (categories[i].equals("matematica")) categoriesId[i-2] = R.drawable.matematica;
            if (categories[i].equals("musica")) categoriesId[i-2] = R.drawable.musica;
            if (categories[i].equals("sport")) categoriesId[i-2] = R.drawable.sport;
            if (categories[i].equals("storia")) categoriesId[i-2] = R.drawable.storia;
        }

        return categoriesId;
    }

    public void goToQuestion(String i) {

        new SocketTask().execute();

        Intent intent = new Intent (this, QuestionActivity.class);
        intent.putExtra(EXTRA_MESSAGE, i);
        intent.putExtra("Categories", message);
        startActivity(intent);
    }

    public class SocketTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            DatagramSocket notFirstTurnSocket = null;

            DatagramSocket datagramSocketTurnEqualsMyId = null;

            try {
                serverAddr = InetAddress.getByName(dstAddress);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            questionData = Integer.toString(actualCategoryPosition + 1);
            byte[] buffer = questionData.getBytes();

            //struttura dati per tabellone

            boolean firstTurn = true;
            boolean game = true;

            while (game) {

                if (!firstTurn) {

                    Log.v("TESTIF", "Dentro a !firstTurn");

                    DatagramPacket receivePacket;

                    try {
                        notFirstTurnSocket = new DatagramSocket();
                        notFirstTurnSocket.setReuseAddress(true);
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                    byte[] receiveBuffer = new byte[4096];
                    receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length,
                            serverAddr, dstPort);

                    try {
                        notFirstTurnSocket.receive(receivePacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //SPLITTARE E GESTIRE, BLACK MAGIC

                    //STAMPA DI PROVA

                    String test = receivePacket.getData().toString();
                    Log.v("STRINGA TEST", test);

                    notFirstTurnSocket.close();

                }

                if (turn == 2) {

                    Log.v("FINE PARTITA", "fine partita");
                    game = false;

                }

                else if (turn == myID) {

                    Log.v("TESTIF", "Dentro turn == myID");

                    // send della categoria

                    datagramSocketTurnEqualsMyId = null;

                    try {
                        datagramSocketTurnEqualsMyId = new DatagramSocket();
                        datagramSocketTurnEqualsMyId.setReuseAddress(true);
                        DatagramPacket dp;
                        dp = new DatagramPacket(buffer, buffer.length, serverAddr, dstPort);
                        Log.v("StartGameActivity", questionData);
                        datagramSocketTurnEqualsMyId.send(dp);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // receive delle domande

                    byte[] receiveBuffer = new byte[8192];
                    DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length,
                            serverAddr, dstPort);

                    while (firstTurn) {
                        try {
                            Log.v("TRY della receive", "Parte la receive");
                            datagramSocketTurnEqualsMyId.receive(packet);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        String questions = new String(packet.getData(), 0, packet.getLength());
                        Log.v("STRINGA TEST2", questions);

                        firstTurn = false;

                    }

                    datagramSocketTurnEqualsMyId.close();
                }
            }



            return null;

        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

}

