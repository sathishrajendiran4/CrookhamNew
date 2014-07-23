package com.example.crookham;

import java.util.ArrayList;
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
import android.widget.ImageView;

import com.example.crookham.admin.AdminHome;
import com.example.crookham.crew.CrewHome;
import com.example.crookham.fieldmanager.FMHome;

@SuppressLint("NewApi")
public class LoginActivity extends Activity implements OnClickListener {
	SQLiteDatabase database;
	String muUid, muUsername, muPassword, muFname, muLname, muEmail, muMobile,
			muUrl, muRole;
	String mValiduser;
	HttpPost httpPost;
	HttpClient httpClient;
	List<NameValuePair> nameValuePairs;
	ImageView iexit;   

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.loginactivity);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		database = this.openOrCreateDatabase("RufuTech", MODE_PRIVATE, null);
		try {
			database.execSQL("Create table if not exists config (id int, username varchar(30),"
					+ " password varchar(30), fname varchar(30), lname varchar(30), "
					+ "email varchar(50),mobile varchar(20),url varchar(100),role varchar(30))");
			database.execSQL("create table if not exists locations ( name varchar(100),variety varchar(50),polygon blob)");
			System.err.println("Success");
		} catch (Exception e) {
			System.err.println("Error" + e.toString());
		}

		GetConfigDetails();
		if (muUrl != null && !muUrl.isEmpty()) {
			new VerifyUser().execute();
		} else {
			Intent intent = new Intent(LoginActivity.this, Configure.class);
			startActivity(intent);
		}

		iexit = (ImageView) findViewById(R.id.iexit);
		iexit.setOnClickListener(this);
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

	private class VerifyUser extends AsyncTask<String, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(LoginActivity.this);

		protected void onPreExecute() {
			Dialog.setMessage(getText(R.string.validatinguser));
			Dialog.show();
			Dialog.setCancelable(false);
			Dialog.setCanceledOnTouchOutside(false);
		}

		protected Void doInBackground(String... urls) {
			runOnUiThread(new Runnable() {
				public void run() {
					ValidatingUser();
				}
			});

			return null;
		}

		protected void onPostExecute(Void unused) {
			// NOTE: You can call UI Element here.

			// Close progress dialog
			Dialog.dismiss();
		}

	}

	public void ValidatingUser() {
		// TODO Auto-generated method stub
		try {
			httpClient = new DefaultHttpClient();
			httpPost = new HttpPost(muUrl + "validatinguser.php");
			nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("username", muUsername));
			nameValuePairs.add(new BasicNameValuePair("password", muPassword));
			nameValuePairs.add(new BasicNameValuePair("role", muRole));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			mValiduser = httpClient.execute(httpPost, responseHandler);
			mValiduser = mValiduser.trim().toString();
			if (mValiduser.contains("Invalid")) {
				InvalidUserAlert();
			} else {
				if (muRole.equals("Admin")) {
					Intent intent = new Intent(LoginActivity.this,
							AdminHome.class);
					startActivity(intent);
				} else if (muRole.equals("Crew")) {
					Intent intent = new Intent(LoginActivity.this,
							CrewHome.class);
					startActivity(intent);
				} else if (muRole.equals("Field Manager")) {
					Intent intent = new Intent(LoginActivity.this, FMHome.class);
					startActivity(intent);
				}

				else {
					System.err.println(muRole);
				}
			}

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

	@SuppressWarnings("deprecation")
	private void InvalidUserAlert() {
		// TODO Auto-generated method stub
		AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this)
				.create();
		alertDialog.setTitle("Error");
		alertDialog.setMessage(getText(R.string.credentialnotcorrect));
		alertDialog.setButton("Configure",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(LoginActivity.this,
								Configure.class);
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

	@SuppressWarnings("deprecation")
	private void ErrorinConnection() {
		// TODO Auto-generated method stub
		AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this)
				.create();
		alertDialog.setTitle("Error");
		alertDialog.setMessage("Error in Connection");
		alertDialog.setButton("Admin", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(LoginActivity.this, Configure.class);
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

	protected void callHome() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == iexit) {
			callHome();
		}
	}
}
