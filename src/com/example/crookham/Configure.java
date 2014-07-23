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
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class Configure extends Activity implements OnClickListener {
	EditText eurl, eusername, epassword;
	String muUid, muUsername, muPassword, muFname, muLname, muEmail, muMobile,
			muRole, muUrl;
	Button bsave, breset;
	ProgressDialog progressDialog;
	String mResponse;
	HttpPost httpPost;
	HttpClient httpClient;
	List<NameValuePair> nameValuePairs;
	SQLiteDatabase database;
	AlertDialog alertDialog;
	ImageView iexit;
	JSONObject jObject;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.configure);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		database = this.openOrCreateDatabase("RufuTech", MODE_PRIVATE, null);
		GetConfigDetails();

		eurl = (EditText) findViewById(R.id.url);
		eusername = (EditText) findViewById(R.id.username);
		epassword = (EditText) findViewById(R.id.password);
		bsave = (Button) findViewById(R.id.save);
		breset = (Button) findViewById(R.id.reset);
		bsave.setOnClickListener(this);
		breset.setOnClickListener(this);
		iexit = (ImageView) findViewById(R.id.iexit);
		iexit.setOnClickListener(this);

		if (muUrl != null && !muUrl.isEmpty()) {
			eurl.setText(muUrl);
			eusername.setText(muUsername);
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
				} while (c.moveToNext());
			}
		}
		c.close();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

		if (muUrl.length() > 5) {
			Intent intent = new Intent(Configure.this, LoginActivity.class);
			startActivity(intent);
		} else {
			ExitApp();
		}

	}

	private void ExitApp() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == iexit) {
			ExitApp();
		}
		if (v == bsave) {
			muUrl = eurl.getText().toString();
			muUsername = eusername.getText().toString();
			muPassword = epassword.getText().toString();

			if (muUsername.length() < 2) {
				Toast.makeText(getApplicationContext(), "Enter valid Username",
						Toast.LENGTH_LONG).show();

			} else if (muPassword.length() < 2) {
				Toast.makeText(getApplicationContext(), "Enter valid Password",
						Toast.LENGTH_LONG).show();
			} else {
				progressDialog = ProgressDialog.show(Configure.this,
						"In progress", "Please wait");
				new Thread(new Runnable() {
					public void run() {
						CheckUserDetails();
					}
				}).start();
			}

		}
		if (v == breset) {
			eurl.setText("");
			eusername.setText("");
			epassword.setText("");
		}
	}

	protected void CheckUserDetails() {
		// TODO Auto-generated method stub
		try {
			httpClient = new DefaultHttpClient();
			httpPost = new HttpPost(muUrl + "login.php");
			nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("username", muUsername));
			nameValuePairs.add(new BasicNameValuePair("password", muPassword));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			mResponse = httpClient.execute(httpPost, responseHandler);
			mResponse = mResponse.trim().toString();
			runOnUiThread(new Runnable() {
				public void run() {
					progressDialog.dismiss();

					if (mResponse.contains("Not Found")) {
						InvalidUserAlert();
					} else {
						try {
							jObject = new JSONObject(mResponse);

							JSONArray menuitemArray = jObject
									.getJSONArray("User");
							for (int i = 0; i < menuitemArray.length(); i++) {
								muUid = menuitemArray.getJSONObject(i)
										.getString("id").toString();
								muUsername = menuitemArray.getJSONObject(i)
										.getString("username").toString();

								muPassword = menuitemArray.getJSONObject(i)
										.getString("password");
								muFname = menuitemArray.getJSONObject(i)
										.getString("fname");
								muLname = menuitemArray.getJSONObject(i)
										.getString("lname");
								muEmail = menuitemArray.getJSONObject(i)
										.getString("email");
								muMobile = menuitemArray.getJSONObject(i)
										.getString("mobile");
								muRole = menuitemArray.getJSONObject(i)
										.getString("role");

								try {
									database.execSQL("delete from config");
									database.execSQL("insert into config (id,username,password,fname,lname,email,mobile,url,role)values ('"
											+ muUid
											+ "','"
											+ muUsername
											+ "','"
											+ muPassword
											+ "','"
											+ muFname
											+ "','"
											+ muLname
											+ "','"
											+ muEmail
											+ "','"
											+ muMobile
											+ "','"
											+ muUrl
											+ "','"
											+ muRole
											+ "')");
									Intent intent = new Intent(Configure.this,
											LoginActivity.class);
									startActivity(intent);
								} catch (Exception exception) {
									System.err.println("Error in inserting"
											+ exception.toString());
								}
							}
						} catch (Exception e) {
							System.err.println("Error in parsing"
									+ e.toString());
						}

					}
				}
			});

		} catch (final Exception exception) {
			runOnUiThread(new Runnable() {
				public void run() {
					progressDialog.dismiss();
					System.err.println("Error" + exception.toString());
					ErrorinConnection();
				}
			});

		}

	}

	@SuppressWarnings("deprecation")
	protected void ErrorinConnection() {
		// TODO Auto-generated method stub

		alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("Error");
		alertDialog.setIcon(R.drawable.warning);
		alertDialog.setMessage("Error in Network connection");
		alertDialog.setButton("Try again",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						alertDialog.cancel();
					}
				});
		alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);

			}
		});
		alertDialog.show();
		alertDialog.setCancelable(false);
		alertDialog.setCanceledOnTouchOutside(false);
	}

	@SuppressWarnings("deprecation")
	protected void InvalidUserAlert() {
		// TODO Auto-generated method stub
		alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setMessage(getText(R.string.credentialnotcorrect));
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				alertDialog.cancel();
			}
		});

		alertDialog.show();
		alertDialog.setCancelable(false);
		alertDialog.setCanceledOnTouchOutside(false);
	}

}
