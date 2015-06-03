package com.covea.hackathon.hackathonapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;


public class MainActivity extends ActionBarActivity {

    //First We Declare Titles And Icons For Our Navigation Drawer List View
    //This Icons And Titles Are holded in an Array as you can see

    String TITLES[] = {"Home","Connexion","Enregistrement","Shop","Equipe"};
    int ICONS[] = {R.drawable.ic_home,R.drawable.ic_cnx,R.drawable.ic_enr,R.drawable.ic_shop,R.drawable.ic_team};

    private static final String CNX_REQ="0";
    private static final String ENR_REQ="1";
    private static final String PROFIL_REQ="2";

    //Similarly we Create a String Resource for the name and email in the header view
    //And we also create a int resource for profile picture in the header view

    String NAME = "";
    String EMAIL = "user@mail.com";
    int PROFILE = R.drawable.ic_user;

    public static Context context;
    private static String HttpReq;
    public static String statusReturn="200";


    private Toolbar toolbar;                              // Declaring the Toolbar Object

    RecyclerView mRecyclerView;                           // Declaring RecyclerView
    RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    DrawerLayout Drawer;                                  // Declaring DrawerLayout

    ActionBarDrawerToggle mDrawerToggle;                  // Declaring Action Bar Drawer Toggle

    public static DefaultHttpClient httpClient;
    ProgressDialog mProgressDialog;
    static JSONObject jsonResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    /* Assinging the toolbar object ot the view
    and setting the the Action bar to our toolbar
     */
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        context = this;

        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View

        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size

        // initialize http client
        httpClient = new DefaultHttpClient();

       // check if setting saved
        SharedPreferences prefs = context.getSharedPreferences(getResources().getString(R.string.settingFile),MODE_PRIVATE);
        String restoredUser = prefs.getString("user", null);
        if (restoredUser != null)
        {
            NAME = restoredUser;
        }
        else
        {
            NAME="";
        }
        mAdapter = new MyAdapter(TITLES,ICONS,NAME,EMAIL,PROFILE);       // Creating the Adapter of MyAdapter class(which we are going to see in a bit)
        // And passing the titles,icons,header view name, header view email,
        // and header view profile picture

        mRecyclerView.setAdapter(mAdapter);                              // Setting the adapter to RecyclerView

        final GestureDetector mGestureDetector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener() {

            @Override public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

        });


        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                View child = recyclerView.findChildViewUnder(motionEvent.getX(),motionEvent.getY());



                if(child!=null && mGestureDetector.onTouchEvent(motionEvent)){
                    Drawer.closeDrawers();

                    FragmentManager fragmentManager = getSupportFragmentManager();

                    int position = recyclerView.getChildPosition(child);

                    switch (position) {
                        case 1:
                            fragmentManager.beginTransaction()
                                    .replace(R.id.container, PlaceholderFragment.newInstance(position))
                                    .commit() ;
                            break;

                        case 2:
                            fragmentManager.beginTransaction()
                                    .replace(R.id.container, CnxFragment.newInstance(position))
                                    .commit() ;
                            break;

                        case 3:
                            fragmentManager.beginTransaction()
                                    .replace(R.id.container, EnrFragment.newInstance(position))
                                    .commit() ;
                            break;

                        case 4:
                            fragmentManager.beginTransaction()
                                    .replace(R.id.container, BlankFragment.newInstance(position))
                                    .commit() ;
                            break;

                        case 5:
                            fragmentManager.beginTransaction()
                                    .replace(R.id.container, AboutFragment.newInstance(position))
                                    .commit() ;
                            break;
                    }

                    //Toast.makeText(MainActivity.this,"The Item Clicked is: "+recyclerView.getChildPosition(child), Toast.LENGTH_SHORT).show();

                    return true;

                }

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

            }
        });



        mLayoutManager = new LinearLayoutManager(this);                 // Creating a layout Manager

        mRecyclerView.setLayoutManager(mLayoutManager);                 // Setting the layout Manager


        Drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);        // Drawer object Assigned to the view
        mDrawerToggle = new ActionBarDrawerToggle(this,Drawer,toolbar,R.string.openDrawer,R.string.closeDrawer){

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // code here will execute once the drawer is opened( As I dont want anything happened whe drawer is
                // open I am not going to put anything here)
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Code here will execute once drawer is closed
            }



        }; // Drawer Toggle Object Made
        Drawer.setDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
        mDrawerToggle.syncState();               // Finally we set the drawer toggle sync State

        // inflate main fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(1))
                .commit() ;

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

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (id == R.id.action_search) {
            return true;
        }

        if (id == R.id.action_user) {

            fragmentManager.beginTransaction()
                    .replace(R.id.container, PlaceholderFragment.newInstance(1))
                    .commit() ;
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    /**
     * Connexion fragment containing a simple view.
     */
    public static class CnxFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static CnxFragment newInstance(int sectionNumber) {
            CnxFragment fragment = new CnxFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public CnxFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_cnx, container, false);

            // retrieve user password if previously saved
            SharedPreferences prefs = context.getSharedPreferences(getResources().getString(R.string.settingFile),MODE_PRIVATE);

            Boolean restoredSaveIndicator = prefs.getBoolean("saveInd", false);
            String restoredUser = prefs.getString("user", null);
            String restoredPsw = prefs.getString("password", null);

            TextView myTextView;
            CheckBox myCheckBox;

            if (restoredSaveIndicator) {
                if (restoredUser != null) {
                    myTextView = (TextView) rootView.findViewById(R.id.editTextCnxUserNameId);
                    myTextView.setText(restoredUser);
                }
                if (restoredUser != null) {
                    myTextView = (TextView) rootView.findViewById(R.id.editTextCnxPaswId);
                    myTextView.setText(restoredPsw);
                }

                myCheckBox = (CheckBox)rootView.findViewById(R.id.checkBoxSaveCnxId);
                myCheckBox.setChecked(true);
            }
            return rootView;
        }
    }


    public void userCnx(View v) {
        //retrieve text entered
        if (isNetworkOnline()) {
            // hide start game button until download finished
            Button myButton = (Button) findViewById(R.id.buttonCnxUserId);
            myButton.setVisibility(View.INVISIBLE);

            TextView myTextuNameView = (TextView) findViewById(R.id.editTextCnxUserNameId);
            String uName = myTextuNameView.getText().toString();

            TextView myTextuPswView = (TextView) findViewById(R.id.editTextCnxPaswId);
            String uPsw = myTextuPswView.getText().toString();

            boolean isChecked = ((CheckBox) findViewById(R.id.checkBoxSaveCnxId)).isChecked();

            // save preferences
            SharedPreferences.Editor editor = context.getSharedPreferences(getResources().getString(R.string.settingFile),MODE_PRIVATE).edit();

            if (isChecked)
            {
                editor.putString("user", uName);
                editor.putString("password", uPsw);
                editor.putBoolean("saveInd",true);
                editor.apply();
            }
            else
            {
                editor.putBoolean("saveInd",false);
                editor.apply();
            }
            mProgressDialog = ProgressDialog.show(this, getString(R.string.textConnect),
                    "", true);

            String searchUrl = getString(R.string.serveurUrl);

            // request for user cnx
            HttpReq=CNX_REQ;
            HttpGetTask myDownloading = (HttpGetTask) new HttpGetTask(this, searchUrl).execute();

        } else {
            Toast.makeText(this, R.string.no_network, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Enregistrement fragment containing a simple view.
     */
    public static class EnrFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static EnrFragment newInstance(int sectionNumber) {
            EnrFragment fragment = new EnrFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public EnrFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_enr, container, false);
            return rootView;
        }
    }

    /**
     * About fragment containing a simple view.
     */
    public static class AboutFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static AboutFragment newInstance(int sectionNumber) {
            AboutFragment fragment = new AboutFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public AboutFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_about, container, false);
            return rootView;
        }
    }

    /**
     * Blank fragment containing a simple view.
     */
    public static class BlankFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static BlankFragment newInstance(int sectionNumber) {
            BlankFragment fragment = new BlankFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public BlankFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_blank, container, false);
            return rootView;
        }
    }




    // Network dialog management

    public boolean isNetworkOnline() {
        boolean status = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                status = true;
            } else {
                netInfo = cm.getNetworkInfo(1);
                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
                    status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return status;
    }

   // backgroud http get
   class HttpGetTask extends AsyncTask<String, Integer, String> {

       private Context contextHttpTask;
       private String theUrl;
       private String jsonReturn;

       public HttpGetTask(Context context, String qUrl) {
           super();

           this.contextHttpTask = context;
           theUrl = qUrl;
       }


       @Override
       protected void onPostExecute(String result) {
           super.onPostExecute(result);

           // stop workinq animation
           mProgressDialog.cancel();

           FragmentManager fragmentManager = getSupportFragmentManager();

           switch (HttpReq){
               case CNX_REQ:
                   if (statusReturn.equals("200"))
                   {
                       // go back home
                       fragmentManager.beginTransaction()
                               .replace(R.id.container, PlaceholderFragment.newInstance(1))
                               .commit() ;
                   }



                   break;

               default:
                   // go back home
                   fragmentManager.beginTransaction()
                           .replace(R.id.container, PlaceholderFragment.newInstance(1))
                           .commit() ;
           }

           try {
               displayResult(jsonReturn);
           } catch (JSONException e) {
               e.printStackTrace();
           }

       }


       @Override
       protected String doInBackground(String... params) {

           // launch download
           try {
               jsonReturn = HttpGetTask(theUrl);
           } catch (IOException e) {
               e.printStackTrace();
           } catch (URISyntaxException e) {
               e.printStackTrace();
           }

           return "ok";
       }


       public String HttpGetTask(String urlStr) throws IOException, URISyntaxException {
           StringBuilder builder = new StringBuilder();
           HttpGet httpGet = new HttpGet(urlStr);
           String loginValue;
           String passwordValue;

           // add login/password basic authentication
           if (HttpReq == CNX_REQ) {
               SharedPreferences prefs = context.getSharedPreferences(getResources().getString(R.string.settingFile), MODE_PRIVATE);

               loginValue = prefs.getString("user", null);
               passwordValue = prefs.getString("password", null);

           String credentials = loginValue + ":" + passwordValue;
           String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
           httpGet.addHeader("Authorization", "Basic " + base64EncodedCredentials);
           }
           try {

               HttpResponse response = httpClient.execute(httpGet);
               StatusLine statusLine = response.getStatusLine();
               final int statusCode = statusLine.getStatusCode();

               if(statusCode == 200){
                   HttpEntity entity = response.getEntity();
                   InputStream content = entity.getContent();
                   BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                   String line;
                   while((line = reader.readLine()) != null){
                       builder.append(line);
                   }
               }
               else
               {
                   runOnUiThread(new Runnable() {
                       public void run() {
                           statusReturn=String.valueOf(statusCode);
                       }
                   });
                   return "ERR" + statusCode;
               }
           } catch (Exception e) {
               return e.toString();
           }
           return builder.toString();
       }
   }



    public void displayResult(String readJSON) throws JSONException {


        JSONObject jsonObject;
        JSONObject jsonObjectL2;

        jsonObject = new JSONObject(readJSON);
        //jsonObjectL2 = jsonObject.getJSONObject("data");

        jsonResult = jsonObject;

        // set new fragment to display result
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, BlankFragment.newInstance(1))
                .commit();
    }
}