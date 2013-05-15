/**
 * Copyright 2013 Bhavyanshu Parasher   
 * This file is part of "ITM University Android Application".
 * "ITM University Android Application" is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free Software Foundation, 
 * either version 3 of the License, or (at your option) any later version.
 * "ITM University Android Application" is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with "ITM University Android Application". 
 * If not, see http://www.gnu.org/licenses/.
 */

/**
 * This activity is used to fetch all questions in Talky.
 */
package com.wbs.itm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
 
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.analytics.tracking.android.EasyTracker;
 
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
 
public class AllQuestionActivity extends ListActivity {
 
    private ProgressDialog pDialog;
    JSONParser jParser = new JSONParser();
    ArrayList<HashMap<String, String>> questionsList;
    private static String url_all_questions = "http://yourdomain.com/app/fetchqs.php";
 
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_QUES = "ques";
    private static final String TAG_PID = "qid";
    private static final String TAG_NAME = "name";
    private static final String TAG_INFO = "question";
    private static final String TAG_DATE = "date";
    JSONArray questions = null;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_ques);
        questionsList = new ArrayList<HashMap<String, String>>();
        new LoadAllQuestions().execute();
        ListView lv = getListView();
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
            	String pid = ((TextView) view.findViewById(R.id.qid)).getText()
                        .toString();
            	String question = ((TextView) view.findViewById(R.id.question)).getText()
                        .toString();
                Intent myIntent = new Intent(AllQuestionActivity.this, SelectedQuestionActivity.class);
                myIntent.putExtra("TAG_PID", pid);
                myIntent.putExtra("TAG_QUESTION", question);
     		   AllQuestionActivity.this.startActivity(myIntent);            
            }
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if result code 100
        if (resultCode == 100) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }
 
    /**
     * Background Async Task to Load all questions by making HTTP Request
     * */
    class LoadAllQuestions extends AsyncTask<String, String, String> {
 
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AllQuestionActivity.this);
            pDialog.setMessage("Loading Questions. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
 
        /**
         * getting data from url
         * */
        @Override
		protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            JSONObject json = jParser.makeHttpRequest(url_all_questions, "GET", params);
            Log.d("All questions: ", json.toString()); //Important for logcat analysis
            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    questions = json.getJSONArray(TAG_QUES);
                    for (int i = 0; i < questions.length(); i++) {
                        JSONObject c = questions.getJSONObject(i);
                        String id = c.getString(TAG_PID);
                        String name = c.getString(TAG_NAME);
                        String info = c.getString(TAG_INFO);
                        String date = c.getString(TAG_DATE);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(TAG_PID, id);
                        map.put(TAG_NAME, name);
                        map.put(TAG_INFO, info);
                        map.put(TAG_DATE, date);
                        questionsList.add(map);
                    }
                } else {
                	//If nothing found then call activity for adding a new question ;)
                    Intent i = new Intent(getApplicationContext(),
                            NewQuestionActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
		protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                @Override
				public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                    		AllQuestionActivity.this, questionsList,
                            R.layout.list_item_ques, new String[] { TAG_PID,
                                    TAG_NAME, TAG_INFO, TAG_DATE },
                            new int[] { R.id.qid, R.id.name, R.id.question, R.id.date});
                    setListAdapter(adapter);
                }
            });
        }
    }
    
    @Override
    protected void onStart() {
       super.onStart();
       EasyTracker.getInstance().activityStart(this); // Add this method
    }
  
    @Override
    protected void onStop() {
       super.onStop();
       EasyTracker.getInstance().activityStop(this); // Add this method
    }
}
