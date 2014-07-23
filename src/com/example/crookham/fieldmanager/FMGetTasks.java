package com.example.crookham.fieldmanager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.crookham.R;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint({ "SdCardPath", "SimpleDateFormat", "DefaultLocale" })
public class FMGetTasks extends Activity implements OnClickListener {

	String muUid, muUsername, muPassword, muFname, muLname, muEmail, muMobile,
			muUrl, muRole;
	ProgressDialog progressDialog;
	HttpPost httpPost;
	HttpClient httpClient;
	List<NameValuePair> nameValuePairs;
	ListView mtasklistview;
	FMGetTaskList adapter;
	JSONObject jObject;
	String mResponse;
	SQLiteDatabase database;
	ArrayList<HashMap<String, String>> mtasklist;
	ImageView iexit;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.admingettasks);

		database = this.openOrCreateDatabase("RufuTech", MODE_PRIVATE, null);
		try {

			database.execSQL("create table if not exists selectedtask(id int ,type varchar(50),location varchar(50),assignedto varchar(30),date varchar(20),status varchar(20))");
		} catch (Exception e) {
			System.err.println("Error in creating table" + e.toString());
		}
		try {
			database.execSQL("delete from selectedtask");

		} catch (Exception e) {
			System.err.println("Error in deleting table" + e.toString());
		}
		mtasklistview = (ListView) findViewById(R.id.tasklistview);
		iexit = (ImageView) findViewById(R.id.iexit);

		iexit.setOnClickListener(this);
		GetConfigDetails();
		GettingTaskfromServer();

	}

	private void GettingTaskfromServer() {
		// TODO Auto-generated method stub
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(" yyyy-MM-dd");
			String mCurDate = formatter.format(new Date());
			mtasklistview.setVisibility(View.VISIBLE);
			httpClient = new DefaultHttpClient();
			httpPost = new HttpPost(muUrl + "gettasks.php");
			nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("userid", muUid));
			nameValuePairs.add(new BasicNameValuePair("date", mCurDate));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			mResponse = httpClient.execute(httpPost, responseHandler);
			mResponse = mResponse.trim().toString();
			try {
				jObject = new JSONObject(mResponse);
				mtasklist = new ArrayList<HashMap<String, String>>();

				JSONArray menuitemArray = jObject.getJSONArray("Tasks");
				adapter = new FMGetTaskList(getApplicationContext(),
						menuitemArray);

				mtasklistview.setAdapter(adapter);
				mtasklistview.setCacheColorHint(0);

			} catch (Exception exception) {
				NoTaskFoundAlert();
				System.err.println(exception.toString());
			}
		} catch (final Exception exception) {
			System.err
					.println("Error in communication:" + exception.toString());

		}

	}

	@SuppressWarnings("deprecation")
	protected void NoTaskFoundAlert() {
		// TODO Auto-generated method stub
		AlertDialog alertDialog = new AlertDialog.Builder(FMGetTasks.this)
				.create();
		alertDialog.setTitle("Sorry");
		alertDialog.setMessage("No Tasks available for you");
		alertDialog.setButton("Back", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(FMGetTasks.this, FMHome.class);
				startActivity(intent);
			}
		});
		alertDialog.show();
		alertDialog.setCancelable(false);
		alertDialog.setCanceledOnTouchOutside(false);
	}

	private void GetConfigDetails() {
		// TODO Auto-generated method stub
		Cursor c = database.rawQuery("select * from config", null);
		if (c != null) {
			if (c.moveToFirst()) {
				do {

					muUid = c.getString(c.getColumnIndex("id"));
					muUsername = c.getString(c.getColumnIndex("username"));
					muPassword = c.getString(c.getColumnIndex("password"));
					muFname = c.getString(c.getColumnIndex("fname"));
					muLname = c.getString(c.getColumnIndex("lname"));
					muEmail = c.getString(c.getColumnIndex("email"));
					muMobile = c.getString(c.getColumnIndex("mobile"));
					muUrl = c.getString(c.getColumnIndex("url"));
					muRole = c.getString(c.getColumnIndex("role"));

				} while (c.moveToNext());
			}
		}
		c.close();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == iexit) {
			callHome();
		}
	}

	protected void callHome() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, FMHome.class);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(FMGetTasks.this, FMHome.class);
		startActivity(intent);
	}
}
