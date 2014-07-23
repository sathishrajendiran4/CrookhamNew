package com.example.crookham.fieldmanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.crookham.R;
import com.example.crookham.admin.AdminGetTasks;
import com.example.crookham.admin.AdminHome;
import com.example.crookham.admin.GPSTracker;

@SuppressLint("SimpleDateFormat")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FMInProgressRogueing extends Activity implements OnClickListener {
	String muUid, muUsername, muPassword, muFname, muLname, muEmail, muMobile,
			muUrl, muRole;
	SQLiteDatabase database;
	String mTaskid, mTasktype, mTasklocation, mTaskdate, mTaskstatus,
			mTaskassignedto;
	TextView ttaskid, ttasktype, ttasklocation, ttaskdate, ttaskstatus, tname,
			tnumberofworkers, tstartdate, ttimer;
	TextView tstartinfo;
	ImageView iexit;
	HttpPost httpPost;
	HttpClient httpClient;
	List<NameValuePair> nameValuePairs;
	String mResponse;
	ProgressDialog progressDialog;
	JSONObject jObject;
	Button btemporarystop, bpermanentstop;

	Button bverifylocation;
	Location location;
	LocationManager locationManager;
	String mNumberofWorkers, mStartDate, mID, mWorkedtime;
	GPSTracker gps;
	double latitude, longitude;
	String lat, lon;
	static long diffSeconds;
	static long diffMinutes;
	static long diffHours;
	String[] time;
	long Time1;
	long timeInMillies = 0L;
	long timeSwap = 0L;
	long finalTime = 0L;
	private Handler myHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.inprogressrogueing);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		locationManager = (LocationManager) FMInProgressRogueing.this
				.getSystemService(Context.LOCATION_SERVICE);

		iexit = (ImageView) findViewById(R.id.iexit);
		ttaskid = (TextView) findViewById(R.id.taskid);
		ttasktype = (TextView) findViewById(R.id.tasktype);
		ttasklocation = (TextView) findViewById(R.id.tasklocation);
		ttaskdate = (TextView) findViewById(R.id.taskdate);
		ttaskstatus = (TextView) findViewById(R.id.taskstatus);
		tname = (TextView) findViewById(R.id.tname);
		bverifylocation = (Button) findViewById(R.id.bverifylocation);
		tnumberofworkers = (TextView) findViewById(R.id.tnumberofworkers);
		tstartdate = (TextView) findViewById(R.id.tstartdate);
		ttimer = (TextView) findViewById(R.id.textTimer);
		bpermanentstop = (Button) findViewById(R.id.permanentstop);
		btemporarystop = (Button) findViewById(R.id.temperarorystop);

		bverifylocation.setOnClickListener(this);
		btemporarystop.setOnClickListener(this);
		bpermanentstop.setOnClickListener(this);

		btemporarystop.setVisibility(View.INVISIBLE);
		bpermanentstop.setVisibility(View.INVISIBLE);
		database = this.openOrCreateDatabase("RufuTech", MODE_PRIVATE, null);
		GetConfigDetails();
		GetSelectedTaskfromSqlite();
		progressDialog = ProgressDialog.show(FMInProgressRogueing.this,
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
			httpPost = new HttpPost(muUrl + "getrogueingdetails.php");
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

		if (v == bverifylocation) {
			GetCurrentLocation();

		}
		if (v == bpermanentstop) {
			StopAlert("Completed");
		}
		if (v == btemporarystop) {
			StopAlert(getText(R.string.tempstop).toString());
		}
	}

	private void GetCurrentLocation() {
		// TODO Auto-generated method stub
		gps = new GPSTracker(FMInProgressRogueing.this);

		// check if GPS enabled
		if (gps.canGetLocation()) {
			location = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			final double dmylat1 = location.getLatitude();
			final double dmylon1 = location.getLongitude();

			progressDialog = ProgressDialog.show(FMInProgressRogueing.this,
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
								FMInProgressRogueing.this).create();
						alertDialog.setTitle("Valid Location");
						alertDialog.setIcon(R.drawable.success);
						alertDialog.setButton("OK",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										dialog.cancel();

										btemporarystop
												.setVisibility(View.VISIBLE);
										bpermanentstop
												.setVisibility(View.VISIBLE);
									}
								});
						alertDialog.setCancelable(false);
						alertDialog.setCanceledOnTouchOutside(false);
						alertDialog.show();
					} else {
						AlertDialog alertDialog = new AlertDialog.Builder(
								FMInProgressRogueing.this).create();
						alertDialog.setTitle("Location not found");
						alertDialog.setIcon(R.drawable.wrong);
						alertDialog.setButton(getText(R.string.skipthisstep),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										dialog.cancel();

										btemporarystop
												.setVisibility(View.VISIBLE);
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
		Intent intent = new Intent(this, FMHome.class);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(FMInProgressRogueing.this, FMGetTasks.class);
		startActivity(intent);
	}

	@SuppressWarnings("deprecation")
	private void StopAlert(final String status) {
		// TODO Auto-generated method stub
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(getText(R.string.confirmation));

		alertDialog.setMessage("Are you sure ?");
		alertDialog.setButton("Yes", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				progressDialog = ProgressDialog.show(FMInProgressRogueing.this,
						"In progress", "Please wait");
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
			httpPost = new HttpPost(muUrl + "updaterogueing.php");
			nameValuePairs = new ArrayList<NameValuePair>(2);
			if (mTaskid.contains("RR")) {
				mTaskid = mTaskid.substring(2);
			}
			nameValuePairs.add(new BasicNameValuePair("taskid", mTaskid));
			nameValuePairs.add(new BasicNameValuePair("status", status));
			nameValuePairs.add(new BasicNameValuePair("noofworkers",
					mNumberofWorkers));

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
						Intent intent = new Intent(FMInProgressRogueing.this,
								FMGetTasks.class);
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
				Intent intent = new Intent(FMInProgressRogueing.this,
						FMGetTasks.class);
				startActivity(intent);
			}
		});
		alertDialog.show();
		alertDialog.setCancelable(false);
		alertDialog.setCanceledOnTouchOutside(false);
	}

}
