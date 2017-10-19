package com.example.grace.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.grace.ARchitect.SampleCamActivity;
import com.example.grace.UserInformation.UserCredentials;
import com.example.grace.servercommunication.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;


public class FriendsActivity extends AppCompatActivity {

    private final static String TAG = "FriendsActivity";
    final int PENDING = 2;
    final int FRIEND = 3;
    final String NOFRIENDSERROR = "1002";
    final String REQUESTSENT = "Friend Request Sent";

    Button add_friend_button;
    TextView add_friend_input;


    private View mProgressView;
    private View mFriendsListForm;
    private FriendsListTask friendsListTask;
    private AddFriendTask addFriendTask;
    public String email;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        email = UserCredentials.email;

        setContentView(R.layout.activity_friends);


        mProgressView = findViewById(R.id.Friends_progress);
        mFriendsListForm = findViewById(R.id.friend_list_form);
        setupFriendsTable();

        setupAddFriends();
        setupActionBar();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void setupFriendsTable(){



        //Request friends data


        ServerConnection serverConnection = new ServerConnection();



        ArrayList<String> Keys = new ArrayList<String>();
        ArrayList<String> KeyTags = new ArrayList<String>();
        Keys.add(email);
        KeyTags.add("email");

        serverConnection.makeServerRequest("ListFriends",KeyTags,Keys,1,this, false);


        showProgress(true);
        friendsListTask = new FriendsListTask();
        friendsListTask.execute();




    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    public void setupAddFriends(){
        add_friend_button = (Button)findViewById(R.id.add_friends_button);
        add_friend_input = (TextView)findViewById(R.id.add_friends_input);
        add_friend_button.setOnClickListener(new View.OnClickListener() {
            @Override
                    public void onClick(View view){
                String add_friend_username = add_friend_input.getText().toString();

                add_friend_input.setText("");

                ArrayList<String> Keys = new ArrayList<String>();
                ArrayList<String> KeyTags = new ArrayList<String>();

                Keys.add(email);
                KeyTags.add("email");

                Keys.add(add_friend_username);
                KeyTags.add("user");

                ServerConnection serverConnection = new ServerConnection();
                serverConnection.makeServerRequest("SendFriendRequest", KeyTags, Keys, 2, FriendsActivity.this, false);

                showProgress(true);
                addFriendTask = new AddFriendTask();
                addFriendTask.execute();




            }
             });
    }



    public void drawFriendsTable(){
        String FirstName = null;
        int Status = 0;

        ArrayList<String>  friends  = new ArrayList<>();
        String[] values;
        ArrayList<Integer> statuses = new ArrayList<>();

        JSONObject response = JSONResponse.response;
        try {
            String responseString = response.toString();
            responseString = responseString.replace("{",  "");
            responseString = responseString.replace("}",  "");
            responseString = responseString.replace("\"", "");
            responseString = responseString.replace(":", ",");
            responseString = responseString.replace("[", "");
            responseString = responseString.replace("]", "");
            values = responseString.split(",");
            int j;

            if(!values[1].equals(NOFRIENDSERROR)) {

                for (j = 0; j < values.length; j += 3) {
                    friends.add(values[j + 1]);
                    statuses.add(Integer.parseInt(values[j + 2]));
                }
            }
            System.out.println("The response for friends list is "+ responseString);

        }catch(Exception e){
            e.printStackTrace();

        }

        TableLayout tableLayoutA;
        tableLayoutA= (TableLayout)findViewById(R.id.friends_table);


        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);




        int i;
        for (i = 0; i < friends.size(); i++){
            TableRow row = new TableRow(this);
            row.setLayoutParams(lp);

            TextView qty = new TextView(this);

            FirstName = friends.get(i);
            Status    = statuses.get(i);

            qty.setText(FirstName);


            row.addView(qty);
            row.addView(messageButton(FirstName, Status));
            row.addView(beamButton(FirstName, Status));
            row.addView(blockButton(FirstName));
            tableLayoutA.addView(row,i);

        }

        TableRow header= new TableRow(this);
        header.setLayoutParams(lp);

        TextView username  = new TextView(this);                                                                                                                                                                        new TextView(this);
        TextView message = new TextView(this);
        TextView beam = new TextView(this);
        TextView block = new TextView(this);

        username.setText("Username   ");
        message.setText("Message");
        beam.setText("Beam");
        block.setText("Block");

        header.addView(username);
        header.addView(message);
        header.addView(beam);
        header.addView(block);
        tableLayoutA.addView(header, 0);


        JSONResponse.response = null;
    }



    //Button to open messaging for each friend
    private Button messageButton(final String FirstName, final int Status){

        Button messageFriend;

        if(Status == FRIEND) {
            messageFriend = new Button(this);
            messageFriend.setText(R.string.message_button_text);
            messageFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(FriendsActivity.this, MessagingActivity.class);

                    // Inform messaging activity of recipients name
                    intent.putExtra("MESSAGE_AUDIENCE", (FirstName));
                    startActivity(intent);

                }
            });
        }
        else{
            messageFriend = new Button(this);
            messageFriend.setText(R.string.friendRequest_button_text);
            messageFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    ArrayList<String> Keys = new ArrayList<String>();
                    ArrayList<String> KeyTags = new ArrayList<String>();

                    Keys.add(email);
                    KeyTags.add("email");

                    Keys.add(FirstName);
                    KeyTags.add("user");

                    ServerConnection serverConnection = new ServerConnection();
                    serverConnection.makeServerRequest("AcceptFriendRequest", KeyTags, Keys, 2, FriendsActivity.this, true);
                    finish();
                    startActivity(getIntent());

                }
            });

        }

        return messageFriend;

    }


    private Button beamButton(final String friend_username, int Status){


        Button button = new Button(this);

        if(Status == FRIEND) {
            //button.setBackgroundResource(R.drawable.ic_beam_button);
            button.setText(R.string.beam_button_text);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(FriendsActivity.this, SampleCamActivity.class);
                    intent.putExtra("user", friend_username);
                    startActivity(intent);
                }
            });

        }

        return button;

    }

    private Button blockButton(final String friend_username){


        Button button = new Button(this);
        button.setText(R.string.block_button_text);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(FriendsActivity.this, MapsMarkerActivity.class);

                intent.putExtra("user", friend_username);
                startActivity(intent);

            }
        });

        return button;

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG, "onKeyDown");
        Log.e(TAG, Integer.toString(KeyEvent.KEYCODE_BACK));
        Log.e(TAG, "keyCode = " + Integer.toString(keyCode));
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return false;
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the back button in the action bar.
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent intent = new Intent(FriendsActivity.this, NavigationBarActivity.class);
        startActivity(intent);
        finish();
        return true;

    }

    /**
     * Represents an asynchronous friendslist task used to authenticate
     * the user.
     */
    public class FriendsListTask extends AsyncTask<Void, Void, Boolean> {


        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                while(JSONResponse.response == null){
                    Thread.sleep(20);
                }
                }catch(Exception e){}


                return true;

        }

        @Override
        protected void onPostExecute(final Boolean success) {

            drawFriendsTable();
            friendsListTask = null;
            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            friendsListTask = null;
            showProgress(false);
        }
    }

    /**
     * Represents an asynchronous addfriend task used to authenticate
     * the user.
     */
    public class AddFriendTask extends AsyncTask<Void, Void, Boolean> {


        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                while(JSONResponse.response == null) {
                    Thread.sleep(20);
                    Log.e("Wating", "waiting");
                }

            if (JSONResponse.response.getString("Message").equals(REQUESTSENT)){
                        return true;

            }
            else{
                        return false;
                    }



            }catch(Exception e){e.printStackTrace();}

        return false;
        }


        @Override
        protected void onPostExecute(final Boolean success) {

            if(success){
                add_friend_input.setText("Request Sent");

            }
            else{
                add_friend_input.setText("User does not exist");

            }

            addFriendTask = null;
            showProgress(false);
            JSONResponse.response = null;
        }

        @Override
        protected void onCancelled() {
            addFriendTask = null;
            showProgress(false);
        }
    }


    /**
     * Shows the progress UI and hides the friendslist form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mFriendsListForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mFriendsListForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mFriendsListForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });




        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mFriendsListForm.setVisibility(show ? View.GONE : View.VISIBLE);

        }
    }



}