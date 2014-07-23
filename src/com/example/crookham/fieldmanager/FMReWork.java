package com.example.crookham.fieldmanager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.crookham.LoginActivity;
import com.example.crookham.R;

@SuppressLint("SimpleDateFormat")
public class FMReWork extends Activity implements OnClickListener {
	SQLiteDatabase database;
	String muUid, muUsername, muPassword, muFname, muLname, muEmail, muMobile,
			muUrl, muRole;
	ImageView iexit;
	TextView twelcome, tdate, ttaskid, tfield, trework, ttasktype;
	String mLastTaskid, mTasktype, mLocation, mLocationid, mRework, mTaskid;
	HttpPost httpPost;
	HttpClient httpClient;
	List<NameValuePair> nameValuePairs;
	Button brework, bnewtask;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.adminrework);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		mLocation = getIntent().getStringExtra("locationname");
		mTaskid = getIntent().getStringExtra("taskid");
		mTasktype = getIntent().getStringExtra("tasktype");
		database = this.openOrCreateDatabase("RufuTech", MODE_PRIVATE, null);
		iexit = (ImageView) findViewById(R.id.iexit);
		twelcome = (TextView) findViewById(R.id.twelcome);
		tdate = (TextView) findViewById(R.id.tdate);
		ttaskid = (TextView) findViewById(R.id.taskid);
		tfield = (TextView) findViewById(R.id.location);
		trework = (TextView) findViewById(R.id.rework);
		brework = (Button) findViewById(R.id.brework);
		ttasktype = (TextView) findViewById(R.id.ttasktype);
		ttasktype.setText(getText(R.string.tasktype) + ": " + mTasktype);

		tfield.setText(getText(R.string.tasklocation) + ": " + mLocation);

		bnewtask = (Button) findViewById(R.id.bnewtask);

		iexit.setOnClickListener(this);
		brework.setOnClickListener(this);
		bnewtask.setOnClickListener(this);
		GetConfigDetails();
		SimpleDateFormat formatter = new SimpleDateFormat("EEEE MMM-dd-yyyy");
		String now = formatter.format(new Date());
		twelcome.setText("Hi " + muFname + " " + muLname + " !");
		tdate.setText(now);
		new GetLastTaskid().execute();
		new GetRecount().execute();
	}

	private class GetLastTaskid extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(FMReWork.this);

		protected void onPreExecute() {
			Dialog.setMessage(getText(R.string.pleasewait));
			Dialog.show();
			Dialog.setCancelable(false);
			Dialog.setCanceledOnTouchOutside(false);
		}

		protected Void doInBackground(String... urls) {
			runOnUiThread(new Runnable() {
				public void run() {
					GetLastTaskId();
				}
			});

			return null;
		}

		protected void GetLastTaskId() {
			// TODO Auto-generated method stub
			try {
				httpClient = new DefaultHttpClient();
				httpPost = new HttpPost(muUrl + "getlasttaskid.php");
				nameValuePairs = new ArrayList<NameValuePair>(2);
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				mLastTaskid = httpClient.execute(httpPost, responseHandler);
				mLastTaskid = mLastTaskid.trim().toString();
				Integer taskid = Integer.parseInt(mLastTaskid) + 1;
				if (taskid <= 9) {
					mLastTaskid = "0000" + String.valueOf(taskid);
				} else if (taskid <= 99) {
					mLastTaskid = "0000" + String.valueOf(taskid);
				} else if (taskid < 999) {
					mLastTaskid = "000	" + String.valueOf(taskid);
				}
				ttaskid.setText(getText(R.string.taskid) + ": R" + mLastTaskid);

			} catch (final Exception e) {
				// e.printStackTrace();
				runOnUiThread(new Runnable() {
					public void run() {
						System.err.println(e.toString());
						ErrorinConnection();
					}
				});
			}

		}

		protected void onPostExecute(Void unused) {
			// NOTE: You can call UI Element here.

			// Close progress dialog
			Dialog.dismiss();
		}

	}

	private class GetRecount extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(FMReWork.this);

		protected void onPreExecute() {
			Dialog.setMessage(getText(R.string.pleasewait));
			Dialog.show();
			Dialog.setCancelable(false);
			Dialog.setCanceledOnTouchOutside(false);
		}

		protected Void doInBackground(String... urls) {
			runOnUiThread(new Runnable() {
				public void run() {
					GetLastTaskId();
				}
			});

			return null;
		}

		protected void GetLastTaskId() {
			// TODO Auto-generated method stub
			try {
				httpClient = new DefaultHttpClient();
				httpPost = new HttpPost(muUrl + "getreworkcount.php");
				nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("taskid", mTaskid));
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				mRework = httpClient.execute(httpPost, responseHandler);
				mRework = mRework.trim().toString();
				trework.setText(getText(R.string.rework) + ": " + mRework);

			} catch (final Exception e) {
				// e.printStackTrace();
				runOnUiThread(new Runnable() {
					public void run() {
						System.err.println(e.toString());
						ErrorinConnection();
					}
				});
			}

		}

		protected void onPostExecute(Void unused) {
			// NOTE: You can call UI Element here.

			// Close progress dialog
			Dialog.dismiss();
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == iexit) {
			callHome();
		}
		if (v == brework) {
			if (mTasktype.contains("DT")) {
				Intent intent = new Intent(this, FMDeTasselingRework.class);
				intent.putExtra("locationname", mLocation);
				intent.putExtra("rework", mRework);
				intent.putExtra("taskid", mTaskid);
				startActivity(intent);
			}
			if (mTasktype.contains("RO")) {
				Intent intent = new Intent(this, FMRogueingRework.class);
				intent.putExtra("locationname", mLocation);
				intent.putExtra("rework", mRework);
				intent.putExtra("taskid", mTaskid);
				startActivity(intent);
			}
		}
		if (v == bnewtask) {
			if (mTasktype.contains("DT")) {
				Intent intent = new Intent(this, FMAddDeTesseling.class);
				startActivity(intent);
			}
			if (mTasktype.contains("RO")) {
				Intent intent = new Intent(this, FMAddRogueing.class);
				startActivity(intent);
			}
		}
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
	public void onBackPressed() {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("deprecation")
	private void ErrorinConnection() {
		// TODO Auto-generated method stub
		AlertDialog alertDialog = new AlertDialog.Builder(FMReWork.this)
				.create();
		alertDialog.setTitle("Error");
		alertDialog.setMessage("Error in Connection");
		alertDialog.setButton("Try Again",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(FMReWork.this,
								LoginActivity.class);
						startActivity(intent);
					}
				});
		alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				callHome();
			}
		});
		alertDialog.show();
		alertDialog.setCancelable(false);
		alertDialog.setCanceledOnTouchOutside(false);

	}

	private void callHome() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(FMReWork.this, FMHome.class);
		startActivity(intent);
	}

}
