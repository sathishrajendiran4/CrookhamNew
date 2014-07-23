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
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.crookham.R;
import com.example.crookham.admin.GPSTracker;

@SuppressLint("SimpleDateFormat")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FMStartRogueing extends Activity implements OnClickListener {
	String muUid, muUsername, muPassword, muFname, muLname, muEmail, muMobile,
			muUrl, muRole;
	SQLiteDatabase database;
	String mTaskid, mTasktype, mTasklocation, mTaskdate, mTaskstatus,
			mTaskassignedto;
	TextView ttaskid, ttasktype, ttasklocation, ttaskdate, ttaskstatus, tname,
			texttimer;
	Button bstart;
	ImageView iexit;
	HttpPost httpPost;
	HttpClient httpClient;
	List<NameValuePair> nameValuePairs;
	String mResponse;
	ProgressDialog progressDialog;
	JSONObject jObject;

	private long startTime = 0L;
	private Handler myHandler = new Handler();
	long timeInMillies = 0L;
	long timeSwap = 0L;
	long finalTime = 0L;
	Button bverifylocation;
	Location location;
	LocationManager locationManager;
	NumberPicker numberofworkers;
	String mNumberofWorkers = "1";
	GPSTracker gps;
	double latitude, longitude;
	String lat, lon;
	TextView tstartinfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.startdetasseling);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		locationManager = (LocationManager) FMStartRogueing.this
				.getSystemService(Context.LOCATION_SERVICE);

		iexit = (ImageView) findViewById(R.id.iexit);
		ttaskid = (TextView) findViewById(R.id.taskid);
		ttasktype = (TextView) findViewById(R.id.tasktype);
		ttasklocation = (TextView) findViewById(R.id.tasklocation);
		ttaskdate = (TextView) findViewById(R.id.taskdate);
		ttaskstatus = (TextView) findViewById(R.id.taskstatus);
		tname = (TextView) findViewById(R.id.tname);
		bstart = (Button) findViewById(R.id.bstart);
		texttimer = (TextView) findViewById(R.id.textTimer);
		texttimer.setVisibility(View.INVISIBLE);
		bverifylocation = (Button) findViewById(R.id.bverifylocation);
		numberofworkers = (NumberPicker) findViewById(R.id.numberofworkers);

		bverifylocation.setOnClickListener(this);

		database = this.openOrCreateDatabase("RufuTech", MODE_PRIVATE, null);
		GetConfigDetails();
		GetSelectedTaskfromSqlite();
		ttaskid.setText(getText(R.string.taskid) + ": " + mTaskid);
		ttasktype.setText(getText(R.string.tasktype) + ": " + mTasktype);
		ttasklocation.setText(getText(R.string.tasklocation) + ": "
				+ mTasklocation);
		ttaskdate.setText(getText(R.string.taskdate) + ": " + mTaskdate);
		ttaskstatus.setText(getText(R.string.status) + ": " + mTaskstatus);
		tname.setText(getText(R.string.inspector) + ": " + mTaskassignedto);

		iexit.setOnClickListener(this);
		bstart.setOnClickListener(this);
		numberofworkers.setMaxValue(10);
		numberofworkers.setMinValue(1);
		numberofworkers
				.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		numberofworkers
				.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
					@Override
					public void onValueChange(NumberPicker picker, int oldVal,
							int newVal) {
						mNumberofWorkers = String.valueOf(newVal);
					}
				});
		tstartinfo = (TextView) findViewById(R.id.tstartinfo);
		tstartinfo.setVisibility(View.INVISIBLE);
		bstart.setVisibility(View.INVISIBLE);
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

	private void GetSelectedTaskfromSqlite() {
		// TODO Auto-generated method stub

		Cursor c = database.rawQuery("SELECT * from selectedtask", null);

		if (c != null) {
			if (c.moveToFirst()) {
				do {
					mTaskid = c.getString(c.getColumnIndex("id"));
					mTasktype = c.getString(c.getColumnIndex("type"));
					mTasklocation = c.getString(c.getColumnIndex("location"));
					mTaskdate = c.getString(c.getColumnIndex("date"));
					mTaskstatus = c.getString(c.getColumnIndex("status"));
					mTaskassignedto = c.getString(c
							.getColumnIndex("assignedto"));
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
		if (v == bstart) {
			StartAlert();
			texttimer.setVisibility(View.VISIBLE);
		}
		if (v == bverifylocation) {
			GetCurrentLocation();

		}
	}

	private void GetCurrentLocation() {
		// TODO Auto-generated method stub
		gps = new GPSTracker(FMStartRogueing.this);

		// check if GPS enabled
		if (gps.canGetLocation()) {
			location = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			final double dmylat1 = location.getLatitude();
			final double dmylon1 = location.getLongitude();

			progressDialog = ProgressDialog.show(FMStartRogueing.this,
					"In progress", getText(R.string.pleasewait));
			new Thread(new Runnable() {
				public void run() {
					VerifyLocation(dmylat1, dmylon1);
				}
			}).start();
		} else {
			gps.showSettingsAlert();
		}

	}

	protected void VerifyLocation(double dmylat1, double dmylon1) {
		// TODO Auto-generated method stub

		try {
			httpClient = new DefaultHttpClient();
			httpPost = new HttpPost(muUrl + "checklocation.php");
			nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("taskid", mTaskid));
			nameValuePairs.add(new BasicNameValuePair("lat", String
					.valueOf(dmylat1)));
			nameValuePairs.add(new BasicNameValuePair("lon", String
					.valueOf(dmylon1)));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			mResponse = httpClient.execute(httpPost, responseHandler);

			mResponse = mResponse.trim().toString();
			runOnUiThread(new Runnable() {
				@SuppressWarnings("deprecation")
				public void run() {
					progressDialog.cancel();
					if (!mResponse.equals("Not found")) {
						AlertDialog alertDialog = new AlertDialog.Builder(
								FMStartRogueing.this).create();
						alertDialog.setTitle("Valid Location");
						alertDialog.setIcon(R.drawable.success);
						alertDialog.setButton("OK",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										dialog.cancel();
										bstart.setVisibility(View.VISIBLE);
										tstartinfo.setVisibility(View.VISIBLE);
									}
								});
						alertDialog.setCancelable(false);
						alertDialog.setCanceledOnTouchOutside(false);
						alertDialog.show();
					} else {
						AlertDialog alertDialog = new AlertDialog.Builder(
								FMStartRogueing.this).create();
						alertDialog.setTitle("Location not found");
						alertDialog.setIcon(R.drawable.wrong);
						alertDialog.setButton(getText(R.string.skipthisstep),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										dialog.cancel();
										bstart.setVisibility(View.VISIBLE);
										tstartinfo.setVisibility(View.VISIBLE);
									}
								});
						alertDialog.setCancelable(false);
						alertDialog.setCanceledOnTouchOutside(false);
						alertDialog.show();
					}

				}
			});
		} catch (Exception e) {
			runOnUiThread(new Runnable() {
				public void run() {
					progressDialog.cancel();
					Toast("Error in Connection");
				}
			});
		}

	}

	protected void callHome() {
		// TODO Auto-generated method stub
		myHandler.removeCallbacks(updateTimerMethod);
		Intent intent = new Intent(this, FMHome.class);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		myHandler.removeCallbacks(updateTimerMethod);
		Intent intent = new Intent(FMStartRogueing.this, FMGetTasks.class);
		startActivity(intent);
	}

	@SuppressWarnings("deprecation")
	private void StartAlert() {
		// TODO Auto-generated method stub
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(getText(R.string.confirmation));

		alertDialog.setMessage("Are you sure want to start");
		alertDialog.setButton("Yes", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				progressDialog = ProgressDialog.show(FMStartRogueing.this,
						"In progress", "Please wait");
				new Thread(new Runnable() {
					public void run() {

						UpdateTaskStatus("In Progress");

					}

				}).start();

			}
		});
		alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}
		});
		alertDialog.show();
		alertDialog.setCancelable(false);
		alertDialog.setCanceledOnTouchOutside(false);
	}

	private void UpdateTaskStatus(String status) {
		// TODO Auto-generated method stub

		try {
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String DateTime = formatter.format(new Date());
			httpClient = new DefaultHttpClient();
			httpPost = new HttpPost(muUrl + "updaterogueing.php");
			nameValuePairs = new ArrayList<NameValuePair>(2);
			if (mTaskid.contains("RR")) {
				mTaskid = mTaskid.substring(2);
			}
			System.err.println("Taskid" + mTaskid);

			nameValuePairs.add(new BasicNameValuePair("taskid", mTaskid));
			nameValuePairs.add(new BasicNameValuePair("status", status));
			nameValuePairs.add(new BasicNameValuePair("noofworkers",
					mNumberofWorkers));
			nameValuePairs.add(new BasicNameValuePair("datetime", DateTime));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			mResponse = httpClient.execute(httpPost, responseHandler);
			System.err.println(mResponse);
			mResponse = mResponse.trim().toString();
			runOnUiThread(new Runnable() {
				public void run() {
					progressDialog.cancel();
					if (mResponse.contains("Success")) {
						startTime = SystemClock.uptimeMillis();
						myHandler.postDelayed(updateTimerMethod, 0);
						new java.util.Timer().schedule(
								new java.util.TimerTask() {
									@Override
									public void run() {
										// your code here
										runOnUiThread(new Runnable() {
											public void run() {
												Intent intent = new Intent(
														FMStartRogueing.this,
														FMGetTasks.class);
												startActivity(intent);
											}
										});
									}
								}, 3000);
						/*
						 * Intent intent = new Intent(AdminStartRogueing.this,
						 * AdminGetTasks.class); startActivity(intent);
						 */
					} else {
						ErrorAlert();
					}

				}
			});

		} catch (Exception exception) {
			System.err.println(exception.toString());
			runOnUiThread(new Runnable() {
				public void run() {
					progressDialog.cancel();
					Toast("Error in Connection");
				}
			});

		}
	}

	private Runnable updateTimerMethod = new Runnable() {

		public void run() {
			// System.err.println(startTime);
			timeInMillies = (SystemClock.uptimeMillis()) - (startTime);
			// System.err.println(timeInMillies);
			finalTime = timeSwap + timeInMillies;
			int seconds = (int) (finalTime / 1000);
			int minutes = seconds / 60;
			int hh = minutes / 60;
			seconds = seconds % 60;
			texttimer.setText("" + hh + ":" + minutes + ":"
					+ String.format("%02d", seconds));
			myHandler.postDelayed(this, 0);
			texttimer.setVisibility(View.VISIBLE);
		}

	};

	protected void Toast(String string) {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT)
				.show();
	}

	@SuppressWarnings("deprecation")
	protected void ErrorAlert() {
		// TODO Auto-generated method stub
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("Error");
		alertDialog
				.setTitle("Sorry unable to update the status please try again");
		alertDialog.setButton("Try again",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}
				});
		alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(FMStartRogueing.this,
						FMGetTasks.class);
				startActivity(intent);
			}
		});
		alertDialog.show();
		alertDialog.setCancelable(false);
		alertDialog.setCanceledOnTouchOutside(false);
	}

}
