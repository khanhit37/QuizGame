package it.splineyellow.quizgame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import java.util.Collections;
import java.util.Random;

public class StartGameActivity extends Activity {

    public static final String TAG = "onItemClick --> posizione : ";

    public Integer[] mThumbIds = {R.drawable.arte, R.drawable.cinema, R.drawable.geografia,
            R.drawable.informatica, R.drawable.letteratura, R.drawable.matematica,
            R.drawable.musica, R.drawable.sport, R.drawable.storia};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);

        Intent intent = getIntent();
        String message = intent.getStringExtra(ConnectionActivity.EXTRA_MESSAGE);
        /*String[] myFabData = message.split(",");
        String nick = myFabData[0];
        String enemy = myFabData[1];
        String color = myFabData[2];
        String turn = myFabData[3];*/

        setTitle("Nuova Partita");

        TextView textView = (TextView) findViewById(R.id.placeholder);
        //textView.setText("n = " + nick + ", c = " + enemy + ", " +
        //        " t = " + turn + ", c = " + color);
        textView.setText(message);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Log.d(TAG, "" + position);
                //Starts from 0
                Toast.makeText(StartGameActivity.this, "Elemento in posizione: " + position, Toast.LENGTH_SHORT).show();
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

    private int[] shuffleArray(int[] array) {
        int index;
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            index = random.nextInt(i + 1);
            if (index != i) {
                array[index] ^= array[i];
                array[i] ^= array[index];
                array[index] ^= array[i];
            }
        }
        return array;
    }
}

