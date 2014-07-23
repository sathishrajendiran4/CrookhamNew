package com.example.crookham.admin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
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
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.crookham.R;

@SuppressLint("SimpleDateFormat")
public class AddInspection extends Activity implements OnItemSelectedListener,
		OnClickListener {
	SQLiteDatabase database;
	String muUid, muUsername, muPassword, muFname, muLname, muEmail, muMobile,
			muUrl, muRole;
	Spinner screw, slocation, sinspectionfor;
	ProgressDialog progressDialog;
	String mResponse, mResponse1;
	HttpPost httpPost, httpPost1;
	HttpClient httpClient, httpClient1;
	List<NameValuePair> nameValuePairs;
	JSONObject jObject;
	AlertDialog alertDialog;
	HashMap<String, String> map;
	HashMap<String, String> map1;

	String mCrewid, mCrewname, mCrewFname, mCrewLname, mCrewEmail, mCrewMobile,
			mLocationid, mLocationname, mDate, mTaskid, mInspectionfor;
	ArrayList<HashMap<String, String>> crewmylist = new ArrayList<HashMap<String, String>>();

	ArrayList<HashMap<String, String>> locationmylist = new ArrayList<HashMap<String, String>>();
	ImageView iexit, idate;
	EditText edate;
	static final int DATE_PICKER_ID = 1111;

	private int myear;
	private int mmonth;
	private int mday;
	CheckBox caddtocalendar, csmscrew, cemailcrew;
	Button baddtask, bback, bcallcrew, bverifylocation;
	GPSTracker gps;
	Location location;
	LocationManager locationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.addinspection);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		locationManager = (LocationManager) AddInspection.this
				.getSystemService(Context.LOCATION_SERVICE);
		database = this.openOrCreateDatabase("RufuTech", MODE_PRIVATE, null);
		screw = (Spinner) findViewById(R.id.spinselectcrew);
		slocation = (Spinner) findViewById(R.id.spinselectlocation);
		iexit = (ImageView) findViewById(R.id.iexit);
		idate = (ImageView) findViewById(R.id.idate);
		edate = (EditText) findViewById(R.id.edate);
		caddtocalendar = (CheckBox) findViewById(R.id.caddtocalendar);
		csmscrew = (CheckBox) findViewById(R.id.csmscrew);
		cemailcrew = (CheckBox) findViewById(R.id.cemailcrew);
		baddtask = (Button) findViewById(R.id.baddtask);
		bback = (Button) findViewById(R.id.bback);
		bcallcrew = (Button) findViewById(R.id.bcallcrew);
		bverifylocation = (Button) findViewById(R.id.verifylocation1);
		sinspectionfor = (Spinner) findViewById(R.id.spinselectinspectionfor);

		screw.setOnItemSelectedListener(this);
		sinspectionfor.setOnItemSelectedListener(this);
		slocation.setOnItemSelectedListener(this);
		idate.setOnClickListener(this);
		iexit.setOnClickListener(this);
		edate.setOnClickListener(this);
		baddtask.setOnClickListener(this);
		baddtask.setVisibility(View.INVISIBLE);
		bback.setOnClickListener(this);
		bback.setVisibility(View.INVISIBLE);
		bcallcrew.setOnClickListener(this);
		bverifylocation.setOnClickListener(this);

		GetConfigDetails();
		progressDialog = ProgressDialog.show(AddInspection.this, "In progress",
				"Please wait");
		new Thread(new Runnable() {
			public void run() {

				GetFieldMangerfromServer();
			}
		}).start();
		new Thread(new Runnable() {
			public void run() {

				GetLocationfromServer();
			}
		}).start();
		final Calendar c = Calendar.getInstance();
		myear = c.get(Calendar.YEAR);
		int mon = c.get(Calendar.MONTH) + 1;
		mday = c.get(Calendar.DAY_OF_MONTH);
		edate.setText(mon + "-" + mday + "-" + myear);

		List<String> list = new ArrayList<String>();
		list.add("De-Tasseling");
		list.add("Rogueing");
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sinspectionfor.setAdapter(dataAdapter);

	}

	protected void GetLocationfromServer() {
		// TODO Auto-generated method stub

		try {
			System.err.println("url" + muUrl);
			httpClient = new DefaultHttpClient();
			httpPost = new HttpPost(muUrl + "getlocation.php");

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			mResponse = httpClient.execute(httpPost, responseHandler);
			mResponse = mResponse.trim().toString();
			HttpResponse response = httpClient.execute(httpPost);
			response.getEntity().consumeContent();
			runOnUiThread(new Runnable() {
				@SuppressWarnings("unchecked")
				public void run() {
					progressDialog.dismiss();

					if (mResponse.contains("Not Found")) {
						CrewNotFound("Location not found");
					} else {
						try {
							jObject = new JSONObject(mResponse);

							JSONArray menuitemArray = jObject
									.getJSONArray("Locations");
							@SuppressWarnings("rawtypes")
							List list = new ArrayList();
							for (int i = 0; i < menuitemArray.length(); i++) {
								map1 = new HashMap<String, String>();
								mLocationid = menuitemArray.getJSONObject(i)
										.getString("id").toString();
								mLocationname = menuitemArray.getJSONObject(i)
										.getString("name").toString();
								map1.put("locationid", mLocationid);
								map1.put("locationname", mLocationname);
								locationmylist.add(map1);
								list.add(mLocationname);

							}
							@SuppressWarnings("rawtypes")
							ArrayAdapter dataAdapter = new ArrayAdapter(
									AddInspection.this,
									android.R.layout.simple_dropdown_item_1line,
									list);
							slocation.setAdapter(dataAdapter);

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

	private void GetFieldMangerfromServer() {
		// TODO Auto-generated method stub

		try {
			httpClient1 = new DefaultHttpClient();
			httpPost1 = new HttpPost(muUrl + "getfieldmanager.php");

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			mResponse1 = httpClient1.execute(httpPost1, responseHandler);
			mResponse1 = mResponse1.trim().toString();
			HttpResponse response = httpClient1.execute(httpPost1);
			response.getEntity().consumeContent();
			runOnUiThread(new Runnable() {
				@SuppressWarnings("unchecked")
				public void run() {
					progressDialog.dismiss();

					if (mResponse1.contains("Not Found")) {
						CrewNotFound("Field Manger not found");
					} else {
						try {
							jObject = new JSONObject(mResponse1);

							JSONArray menuitemArray = jObject
									.getJSONArray("FieldManger");
							@SuppressWarnings("rawtypes")
							List list = new ArrayList();
							for (int i = 0; i < menuitemArray.length(); i++) {
								map = new HashMap<String, String>();
								mCrewid = menuitemArray.getJSONObject(i)
										.getString("id").toString();
								mCrewname = menuitemArray.getJSONObject(i)
										.getString("name").toString();
								map.put("crewid", mCrewid);
								map.put("crewname", mCrewname);
								crewmylist.add(map);
								list.add(mCrewname);

							}
							@SuppressWarnings("rawtypes")
							ArrayAdapter dataAdapter = new ArrayAdapter(
									AddInspection.this,
									android.R.layout.simple_dropdown_item_1line,
									list);
							screw.setAdapter(dataAdapter);

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

	@SuppressWarnings("deprecation")
	protected void CrewNotFound(String message) {
		// TODO Auto-generated method stub
		alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("Sorry");
		alertDialog.setIcon(R.drawable.warning);
		alertDialog.setMessage(message);

		alertDialog.setButton2("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(AddInspection.this, AdminHome.class);
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

	protected void callHome() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, AdminHome.class);

		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Spinner spinner = (Spinner) parent;
		if (spinner.getId() == R.id.spinselectlocation) {
			mLocationid = (locationmylist.get(position).get("locationid"));
			mLocationname = (locationmylist.get(position).get("locationname"));
			baddtask.setVisibility(View.INVISIBLE);
			bback.setVisibility(View.INVISIBLE);
			bverifylocation.setVisibility(View.VISIBLE);
		}
		if (spinner.getId() == R.id.spinselectinspectionfor) {
			mInspectionfor = sinspectionfor.getItemAtPosition(position)
					.toString();
			if (mInspectionfor.contains("De-Tasseling")) {
				mInspectionfor = "DT";
			} else {
				mInspectionfor = "RO";
			}
		}

		if (spinner.getId() == R.id.spinselectcrew) {
			mCrewid = (crewmylist.get(position).get("crewid"));
			progressDialog = ProgressDialog.show(AddInspection.this,
					"In progress", "Please wait");
			new Thread(new Runnable() {
				public void run() {

					GetCrewDetailsfromServer();
				}
			}).start();

		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
	}

	private void GetCrewDetailsfromServer() {
		// TODO Auto-generated method stub
		try {
			httpClient = new DefaultHttpClient();
			httpPost = new HttpPost(muUrl + "getcrewdetails.php");

			nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("id", mCrewid));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			mResponse = httpClient.execute(httpPost, responseHandler);
			mResponse = mResponse.trim().toString();
			runOnUiThread(new Runnable() {
				public void run() {
					progressDialog.dismiss();

					if (mResponse.contains("Not Found")) {
						CrewNotFound("Crew not found");
					} else {
						try {
							jObject = new JSONObject(mResponse);

							JSONArray menuitemArray = jObject
									.getJSONArray("Crewdetails");

							for (int i = 0; i < menuitemArray.length(); i++) {

								mCrewFname = menuitemArray.getJSONObject(i)
										.getString("fname").toString();
								mCrewLname = menuitemArray.getJSONObject(i)
										.getString("lname").toString();
								mCrewEmail = menuitemArray.getJSONObject(i)
										.getString("email").toString();
								mCrewMobile = menuitemArray.getJSONObject(i)
										.getString("mobile").toString();

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

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == iexit) {
			callHome();

		}
		if (v == idate || v == edate) {
			final Calendar c = Calendar.getInstance();
			myear = c.get(Calendar.YEAR);
			mmonth = c.get(Calendar.MONTH);
			mday = c.get(Calendar.DAY_OF_MONTH);
			showDialog(DATE_PICKER_ID);
		}
		if (v == bback) {
			callHome();
		}
		if (v == baddtask) {
			mDate = edate.getText().toString();
			if (mCrewid == null) {
				Toast("Select Field Manager before adding task");
			} else if (mLocationid == null) {
				Toast("Select Lot before adding task");
			} else if (mDate != null && !mDate.isEmpty()) {

				progressDialog = ProgressDialog.show(AddInspection.this,
						"In progress", "Please wait");
				new Thread(new Runnable() {
					public void run() {

						AddTasktoServer();
					}
				}).start();

			} else {
				Toast("Select Date before adding task");
			}
		}
		if (v == bcallcrew) {
			try {
				Intent callIntent = new Intent(Intent.ACTION_CALL);
				callIntent.setData(Uri.parse("tel:" + mCrewMobile));
				startActivity(callIntent);
			} catch (Exception e) {
				System.err.println(e.toString());
				Toast("Cannot make call");
			}

		}
		if (v == bverifylocation) {
			GetCurrentLocation();

		}
	}

	private void GetCurrentLocation() {
		// TODO Auto-generated method stub
		gps = new GPSTracker(AddInspection.this);

		// check if GPS enabled
		if (gps.canGetLocation()) {
			location = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			final double dmylat1 = location.getLatitude();
			final double dmylon1 = location.getLongitude();

			progressDialog = ProgressDialog.show(AddInspection.this,
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
			httpPost = new HttpPost(muUrl + "newtaskchecklocation.php");
			nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("locationid", mLocationid));
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
								AddInspection.this).create();
						alertDialog.setTitle("Valid Location");
						alertDialog.setIcon(R.drawable.success);
						alertDialog.setButton("OK",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										dialog.cancel();
										baddtask.setVisibility(View.VISIBLE);
										bback.setVisibility(View.VISIBLE);
										bverifylocation.setVisibility(View.INVISIBLE);
									}
								});
						alertDialog.setCancelable(false);
						alertDialog.setCanceledOnTouchOutside(false);
						alertDialog.show();
					} else {
						AlertDialog alertDialog = new AlertDialog.Builder(
								AddInspection.this).create();
						alertDialog.setTitle("Location not found");
						alertDialog.setIcon(R.drawable.wrong);
						alertDialog.setButton(getText(R.string.skipthisstep),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										dialog.cancel();
										baddtask.setVisibility(View.VISIBLE);
										bback.setVisibility(View.VISIBLE);
										bverifylocation.setVisibility(View.INVISIBLE);
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

	protected void AddTasktoServer() {
		// TODO Auto-generated method stub
		try {
			mDate = edate.getText().toString();
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-dd-MM");
			String DateTime = formatter.format(new Date());
			SimpleDateFormat newDateFormat = new SimpleDateFormat("MM-dd-yyyy");
			Date MyDate = newDateFormat.parse(mDate);
			newDateFormat.applyPattern("yyyy-MM-dd");
			mDate = newDateFormat.format(MyDate);
			System.err.println(mDate);
			httpClient = new DefaultHttpClient();
			httpPost = new HttpPost(muUrl + "addnewtask.php");

			nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs
					.add(new BasicNameValuePair("locationid", mLocationid));
			nameValuePairs.add(new BasicNameValuePair("assignedto", mCrewid));
			nameValuePairs.add(new BasicNameValuePair("taskdate", mDate));
			nameValuePairs.add(new BasicNameValuePair("createdby", muUid));
			nameValuePairs.add(new BasicNameValuePair("createddate", DateTime));
			nameValuePairs.add(new BasicNameValuePair("tasktype",
					mInspectionfor + " - Inspection"));
			nameValuePairs.add(new BasicNameValuePair("detasselingno", "0"));
			
			nameValuePairs.add(new BasicNameValuePair("status", getText(
					R.string.activestatus).toString()));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			mResponse = httpClient.execute(httpPost, responseHandler);
			mResponse = mResponse.trim().toString();
			runOnUiThread(new Runnable() {
				public void run() {
					progressDialog.dismiss();   

					if (mResponse.contains("Not Found")) {
						ErrorAddingTask();
					} else {
						try {
							jObject = new JSONObject(mResponse);

							JSONArray menuitemArray = jObject
									.getJSONArray("Task");

							for (int i = 0; i < menuitemArray.length(); i++) {

								mTaskid = menuitemArray.getJSONObject(i)
										.getString("taskid").toString();

							}
							SuccessAlert();
							if (caddtocalendar.isChecked()) {
								AddtoCalendar();
							}
							if (csmscrew.isChecked() == true) {
								SendSMStoCrew();
							}
							if (cemailcrew.isChecked() == true) {
								SendEmailtoCrew();
							}

						} catch (Exception e) {
							ErrorAddingTask();
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

	protected void SendEmailtoCrew() {
		// TODO Auto-generated method stub
		try {
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("text/html");
			i.putExtra(Intent.EXTRA_EMAIL, new String[] { mCrewEmail });
			i.putExtra(Intent.EXTRA_SUBJECT, "Inspection");
			i.putExtra(Intent.EXTRA_TEXT, "Hi " + mCrewFname
					+ "\n\nThe De-Tesseling (" + mTaskid
					+ ") for the Location: " + mLocationname + "("
					+ mLocationid + ") is created and the job date is: "
					+ mDate);

			startActivity(Intent.createChooser(i, "Send mail..."));

		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(AddInspection.this,
					"There are no email clients installed.", Toast.LENGTH_SHORT)
					.show();
		}
	}

	protected void SendSMStoCrew() {
		// TODO Auto-generated method stub
		try {
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(mCrewMobile, null, "Hi " + mCrewFname
					+ "\n\nThe De-Tesseling (" + mTaskid
					+ ") for the Location: " + mLocationname + "("
					+ mLocationid + ") is created and the job date is: "
					+ mDate, null, null);
		} catch (Exception e) {
			Toast("Cannot send SMS");
		}
	}

	@SuppressLint("SimpleDateFormat")
	protected void AddtoCalendar() {
		// TODO Auto-generated method stub
		try {
			java.util.Date date = new SimpleDateFormat("dd-MM-yyyy")
					.parse(mDate);
			long lOpendate = date.getTime() + (1000 * 60 * 60 * 24);

			Intent intent = new Intent(Intent.ACTION_EDIT);
			intent.setType("vnd.android.cursor.item/event");
			intent.putExtra("beginTime", lOpendate);
			intent.putExtra("allDay", true);
			intent.putExtra("rrule", "FREQ=YEARLY");
			intent.putExtra("title", "Inspection");
			intent.putExtra("description", "Inspection (" + mTaskid
					+ ") is added for the crew " + mCrewname);
			intent.putExtra("eventLocation", mLocationname);
			startActivity(intent);
		} catch (Exception e) {
			System.err.println("Error in Adding caldendar" + e.toString());
		}
	}

	@SuppressWarnings("deprecation")
	protected void SuccessAlert() {
		// TODO Auto-generated method stub
		alertDialog = new AlertDialog.Builder(AddInspection.this).create();
		alertDialog.setIcon(R.drawable.success);
		alertDialog.setMessage("Success");
		alertDialog
				.setMessage("Added Inspection successfully and the Task Id is:"
						+ mTaskid);
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

				alertDialog.dismiss();
				Intent intent = new Intent(AddInspection.this, AdminHome.class);
				startActivity(intent);
			}
		});
		alertDialog.show();
		alertDialog.setCancelable(false);
		alertDialog.setCanceledOnTouchOutside(false);
	}

	@SuppressWarnings("deprecation")
	protected void ErrorAddingTask() {
		// TODO Auto-generated method stub
		alertDialog = new AlertDialog.Builder(AddInspection.this).create();
		alertDialog.setIcon(R.drawable.warning);
		alertDialog.setMessage("Error");
		alertDialog.setMessage("Error in adding Inspection");
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

				alertDialog.dismiss();
			}
		});
		alertDialog.show();
		alertDialog.setCancelable(false);
		alertDialog.setCanceledOnTouchOutside(false);
	}

	private void Toast(String string) {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_PICKER_ID:
			// set date picker as current date
			DatePickerDialog _date = new DatePickerDialog(this,
					datePickerListener, myear, mmonth, mday) {
				@Override
				public void onDateChanged(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					if (year < myear)
						view.updateDate(myear, mmonth, mday);

					if (monthOfYear < mmonth && year == myear)
						view.updateDate(myear, mmonth, mday);

					if (dayOfMonth < mday && year == myear
							&& monthOfYear == mmonth)
						view.updateDate(myear, mmonth, mday);
					if (year > myear)
						view.updateDate(myear, mmonth, mday);

				}
			};
			return _date;
		}
		return null;
	}

	private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

		// when dialog box is closed, below method will be called.
		public void onDateSet(DatePicker view, int selectedYear,
				int selectedMonth, int selectedDay) {
			myear = selectedYear;
			mmonth = selectedMonth;
			mday = selectedDay;

			// set selected date into textview
			mDate = (new StringBuilder().append(mmonth + 1).append("-")
					.append(mday).append("-").append(myear)).toString();
			edate.setText(mDate);
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		}
	};

}
