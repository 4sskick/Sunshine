package app.sunshine.android.example.com.sunshinever001;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            String[] dataDummy = {
                    "Today - Sunny - 88/63",
                    "Tomorrow - Sunny - 88/63",
                    "Weds - Foggy - 70/43",
                    "Thurs - Sunny - 88/63",
                    "Fri - Sunny - 88/63",
                    "Sat - Foggy - 70/43",
                    "Mon - Sunny - 100/90"
            };
            ArrayAdapter<String> listForecastAdapter;

            List<String> listDataDummy = new ArrayList<String>(Arrays.asList(dataDummy));
            /*
            ArrayList ini berisikan data yang tipe datanya array of string
            ketika dimasukkkan data kemudian data diubah menjadi raw data kemudian
            dikirimkan ke array adapter untuk ditampilkan.
            misal banyak data ada 50 tapi device screen hnya mampu untuk tampilkan 10 data
            maka 10 data yang diproses dulu sisanya setelah di scroll dari data 11-seterusnya
             */

            listForecastAdapter = new ArrayAdapter<String>(
                    getActivity(),//menggunakan getActiity karena class menggunakan fragment
                    R.layout.list_item_forecast,//layout dari listview
                    R.id.list_item_forecast_textview,//id text yg dipke bwt diisikan nanti
                    listDataDummy//data yang mau diisikan
            );

            /*
            lakukan reference view dari data pada adapterArray
             */
            ListView listLayout = (ListView)rootView.findViewById(R.id.listview_forecast);
            listForecastAdapter.setDropDownViewResource(R.layout.list_item_forecast);
            listLayout.setAdapter(listForecastAdapter);

            return rootView;
        }
    }
}
