package com.example.crookham.crew;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.GpsStatus;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.crookham.R;
import com.example.crookham.admin.AdminGetTasks;
import com.example.crookham.admin.AdminHome;
import com.example.crookham.admin.GPSTracker;

@SuppressLint("SimpleDateFormat")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class InProgressCrewDetasseling extends Activity implements OnClickListener,
		OnItemSelectedListener {
	String muUid, muUsername, muPassword, muFname, muLname, muEmail, muMobile,
			muUrl, muRole;
	SQLiteDatabase database;
	String mTaskid, mTasktype, mTasklocation, mTaskdate, mTaskstatus,
			mTaskassignedto;
	TextView ttaskid, ttasktype, ttasklocation, ttaskdate, ttaskstatus, tname,
			tnumberofworkers, tstartdate, ttimer;
	Button bpermanentstop, btempstop;
	ImageView iexit;
	HttpPost httpPost;
	HttpClient httpClient;
	List<NameValuePair> nameValuePairs;
	String mResponse;
	ProgressDialog progressDialog;
	JSONObject jObject;
	GpsStatus status1;
	java.util.Date d1 = null;

	long timeInMillies = 0L;
	long timeSwap = 0L;
	long finalTime = 0L;
	Button bverifylocation;
	Location location;
	LocationManager locationManager;
	String mNumberofWorkers, mStartDate, mComments, mID, Time, mWorkedtime;
	Spinner scomments;
	ArrayList<String> commentsarray;
	GPSTracker gps;
	double latitude, longitude;
	String lat, lon, toParse;
	static long diffSeconds;
	static long diffMinutes;
	static long diffHours;
	String[] time;
	Timer tickTock;
	TimerTask tickTockTask;
	private Handler myHandler = new Handler();
	long Time1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.inprogressdetasseling);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		locationManager = (LocationManager) InProgressCrewDetasseling.this
				.getSystemService(Context.LOCATION_SERVICE);

		iexit = (ImageView) findViewById(R.id.iexit);
		ttaskid = (TextView) findViewById(R.id.taskid);
		ttasktype = (TextView) findViewById(R.id.tasktype);
		ttasklocation = (TextView) findViewById(R.id.tasklocation);
		ttaskdate = (TextView) findViewById(R.id.taskdate);
		ttaskstatus = (TextView) findViewById(R.id.taskstatus);
		tname = (TextView) findViewById(R.id.tname);
		bpermanentstop = (Button) findViewById(R.id.permanentstop);
		bverifylocation = (Button) findViewById(R.id.bverifylocation);
		tnumberofworkers = (TextView) findViewById(R.id.tnumberofworkers);
		tstartdate = (TextView) findViewById(R.id.tstartdate);
		ttimer = (TextView) findViewById(R.id.textTimer);
		scomments = (Spinner) findViewById(R.id.scomments);
		btempstop = (Button) findViewById(R.id.temperarorystop);

		scomments.setOnItemSelectedListener(this);
		btempstop.setOnClickListener(this);

		bverifylocation.setOnClickListener(this);

		database = this.openOrCreateDatabase("RufuTech", MODE_PRIVATE, null);
		GetConfigDetails();
		GetSelectedTaskfromSqlite();
		progressDialog = ProgressDialog.show(InProgressCrewDetasseling.this,
				"In progress", "Please wait");
		new Thread(new Runnable() {
			public void run() {
				GetTaskDetails();
			}
		}).start();
		ttaskid.setText(getText(R.string.taskid) + ": " + mTaskid);
		ttasktype.setText(getText(R.string.tasktype) + ": " + mTasktype);
		ttasklocation.setText(getText(R.string.tasklocation) + ": "
				+ mTasklocation);
		ttaskdate.setText(getText(R.string.taskdate) + ": " + mTaskdate);
		ttaskstatus.setText(getText(R.string.status) + ": " + mTaskstatus);
		tname.setText(getText(R.string.inspector) + ": " + mTaskassignedto);

		iexit.setOnClickListener(this);
		bpermanentstop.setOnClickListener(this);

		commentsarray = new ArrayList<String>();
		commentsarray.add("");
		commentsarray.add("Open Hot Spot");
		commentsarray.add("Pulled Too Deep");
		commentsarray.add("Bad Pull");
		commentsarray.add("Good De-Tassel Quality");
		commentsarray.add("Fair De-Tassel Quality");
		commentsarray.add("Poor De-Tassel Qquality");
		ArrayAdapter<String> mAdapter1 = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, commentsarray);
		scomments.setAdapter(mAdapter1);

		btempstop.setVisibility(View.INVISIBLE);
		bpermanentstop.setVisibility(View.INVISIBLE);
	}

	private void showtimer() {
		// TODO Auto-generated method stub
		Time1 = SystemClock.uptimeMillis();
	    myHandler.postDelayed(updateTimerMethod, 0);
	}
	
	Runnable updateTimerMethod = new Runnable() {

		public void run() {
			
			timeInMillies = (SystemClock.uptimeMillis()) - (Time1);
			finalTime = timeSwap + timeInMillies;
			System.err.println(finalTime);
			int seconds = (int) (finalTime / 1000);
			int minutes = seconds / 60;
			int hh = minutes / 60;
			seconds = seconds % 60;
			ttimer.setText("" + hh + ":" + minutes + ":"
					+ String.format("%02d", seconds));
			myHandler.postDelayed(this, 0);
			ttimer.setVisibility(View.VISIBLE);
		}

	};

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

	protected void GetTaskDetails() {
		// TODO Auto-generated method stub
		try {
			httpClient = new DefaultHttpClient();
			System.err.println(muUrl);
			httpPost = new HttpPost(muUrl + "getdetasselingdetails.php");
			nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("taskid", mTaskid));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			mResponse = httpClient.execute(httpPost, responseHandler);
			mResponse = mResponse.trim().toString();
			runOnUiThread(new Runnable() {
				public void run() {
					progressDialog.dismiss();
					if (mResponse.contains("Not Found")) {

					} else {
						try {
							jObject = new JSONObject(mResponse);

							JSONArray menuitemArray = jObject
									.getJSONArray("Taskdetails");
							for (int i = 0; i < menuitemArray.length(); i++) {
								mNumberofWorkers = menuitemArray.getJSONObject(
										i).getString("noofworkers");
								mStartDate = menuitemArray.getJSONObject(i)
										.getString("startdate");
								mID = menuitemArray.getJSONObject(i).getString(
										"id");
								mWorkedtime = menuitemArray.getJSONObject(i).getString("workedtime");
								if (mWorkedtime.contains("null")) {
									time = mStartDate.split(" ");
									String dateStart = time[1];
									Calendar cal = Calendar.getInstance();
									SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
									String dateStop = sdf.format(cal.getTime());
									SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

									java.util.Date d1 = null;
									java.util.Date d2 = null;
									try {
										d1 = format.parse(dateStart);
										d2 = format.parse(dateStop);
									} catch (ParseException e) {
										e.printStackTrace();
									}
									long diff = d2.getTime() - d1.getTime();
									diffSeconds = diff / 1000 % 60;
									diffMinutes = diff / (60 * 1000) % 60;
									diffHours = diff / (60 * 60 * 1000);
									String hours = String.valueOf(diffHours);
									String min = String.valueOf(diffMinutes);
									String sec = String.valueOf(diffSeconds);
							         mWorkedtime = hours + ":" + min + ":" + sec;
							        }
								String[] currenttime = mWorkedtime.split(":");
								int hour = (Integer.parseInt(currenttime[0])) * 3600000;
								int minutes = (Integer.parseInt(currenttime[1])) * 60000;
								int seconds = (Integer.parseInt(currenttime[2])) * 1000;
								long Time = hour + minutes + seconds;
								System.err.println("Work:"+mWorkedtime);
								timeSwap = Time;
								tnumberofworkers
										.setText(getText(R.string.numberofworkers)
												+ " " + mNumberofWorkers);
								tstartdate.setText(getText(R.string.startdate)
										+ " " + mStartDate);
								showtimer();
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
				}
			});

		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == iexit) {
			callHome();
		}
		if (v == bpermanentstop) {
			ChangeStatus("Completed");
			myHandler.removeCallbacks(updateTimerMethod);
		}
		if (v == btempstop) {
			ChangeStatus(getText(R.string.tempstop).toString());
			Time = ttimer.getText().toString();
			database = this.openOrCreateDatabase("RufuTech", MODE_PRIVATE, null);
			database.execSQL("create table if not exists timer ( name varchar(100),time varchar(50))");
			try {
				database.execSQL("insert into timer (name,time) values ('"
						+ "Detasseling"
						+ "','"
						+ Time + "')");
				System.err.println("Success insertf");
				
			} catch (Exception e) {
				System.err.println("error in inserting" + e.toString());
			}
			myHandler.removeCallbacks(updateTimerMethod);
		}
		if (v == bverifylocation) {
			GetCurrentLocation();

		}
	}

	private void GetCurrentLocation() {
		// TODO Auto-generated method stub
		gps = new GPSTracker(InProgressCrewDetasseling.this);

		// check if GPS enabled
		if (gps.canGetLocation()) {
			location = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			final double dmylat1 = location.getLatitude();
			final double dmylon1 = location.getLongitude();

			progressDialog = ProgressDialog.show(InProgressCrewDetasseling.this,
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
								InProgressCrewDetasseling.this).create();
						alertDialog.setTitle("Valid Location");
						alertDialog.setIcon(R.drawable.success);
						alertDialog.setButton("OK",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										dialog.cancel();
										btempstop.setVisibility(View.VISIBLE);
										bpermanentstop
												.setVisibility(View.VISIBLE);
									}
								});
						alertDialog.setCancelable(false);
						alertDialog.setCanceledOnTouchOutside(false);
						alertDialog.show();
					} else {
						AlertDialog alertDialog = new AlertDialog.Builder(
								InProgressCrewDetasseling.this).create();
						alertDialog.setTitle("Location not found");
						alertDialog.setIcon(R.drawable.wrong);
						alertDialog.setButton(getText(R.string.skipthisstep),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										dialog.cancel();
										btempstop.setVisibility(View.VISIBLE);
										bpermanentstop
												.setVisibility(View.VISIBLE);
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
		Intent intent = new Intent(this, CrewHome.class);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Time = ttimer.getText().toString();
		database = this.openOrCreateDatabase("RufuTech", MODE_PRIVATE, null);
		database.execSQL("create table if not exists timer ( name varchar(100),time varchar(50))");
		try {
			database.execSQL("insert into timer (name,time) values ('"
					+ "Detasseling"
					+ "','"
					+ Time + "')");
			System.err.println("Success insertf");
			
		} catch (Exception e) {
			System.err.println("error in inserting" + e.toString());
		}
		myHandler.removeCallbacks(updateTimerMethod);
		Intent intent = new Intent(InProgressCrewDetasseling.this,
				CrewGetTasks.class);
		startActivity(intent);
	}

	@SuppressWarnings("deprecation")
	private void ChangeStatus(final String status) {
		// TODO Auto-generated method stub
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(getText(R.string.confirmation));

		alertDialog.setMessage("Are you sure ?");
		alertDialog.setButton("Yes", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				progressDialog = ProgressDialog.show(
						InProgressCrewDetasseling.this, "In progress",
						"Please wait");
				new Thread(new Runnable() {
					public void run() {

						UpdateTaskStatus(status);

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
			if (mTaskid.contains("RD")) {
				mTaskid = mTaskid.substring(2);
			}
			System.err.println("Taskid" + mTaskid);

			httpPost = new HttpPost(muUrl + "updatedetasseling.php");
			nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("taskid", mTaskid));
			nameValuePairs.add(new BasicNameValuePair("status", status));
			nameValuePairs.add(new BasicNameValuePair("noofworkers",
					mNumberofWorkers));
			nameValuePairs.add(new BasicNameValuePair("comments", mComments));

			nameValuePairs.add(new BasicNameValuePair("detasselingno", "1"));
			nameValuePairs.add(new BasicNameValuePair("id", mID));
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
						// startTime = SystemClock.uptimeMillis();
						// myHandler.postDelayed(updateTimerMethod, 0);
						Intent intent = new Intent(InProgressCrewDetasseling.this,
								CrewGetTasks.class);
						startActivity(intent);
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
				Intent intent = new Intent(InProgressCrewDetasseling.this,
						CrewGetTasks.class);
				startActivity(intent);
			}
		});
		alertDialog.show();
		alertDialog.setCancelable(false);
		alertDialog.setCanceledOnTouchOutside(false);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Spinner spinner = (Spinner) parent;

		if (spinner.getId() == R.id.scomments) {
			mComments = (String) parent.getItemAtPosition(position);

		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}

}
