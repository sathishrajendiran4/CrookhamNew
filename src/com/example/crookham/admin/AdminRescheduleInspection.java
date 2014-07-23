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
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.SmsManager;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.crookham.R;

@SuppressLint("SimpleDateFormat")
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class AdminRescheduleInspection extends Activity implements
		OnClickListener, OnItemSelectedListener {
	String muUid, muUsername, muPassword, muFname, muLname, muEmail, muMobile,
			muUrl, muRole, mCrewid, mCrewname, mDate, mCrewFname, mCrewLname,
			mCrewEmail, mCrewMobile;
	SQLiteDatabase database;
	String mTaskid, mTasktype, mTasklocation, mTaskdate, mTaskstatus,
			mTaskassignedto;
	TextView ttaskid, ttasktype, ttasklocation, ttaskdate, ttaskstatus, tname;
	ImageView iexit, idate;
	HttpPost httpPost, httpPost1;
	HttpClient httpClient, httpClient1;
	List<NameValuePair> nameValuePairs;
	String mResponse, mResponse1;
	ProgressDialog progressDialog;
	JSONObject jObject;
	Spinner spinassigned;
	Button brescheduleTask, bcanceltask;
	HashMap<String, String> map;
	ArrayList<HashMap<String, String>> crewmylist = new ArrayList<HashMap<String, String>>();
	static final int DATE_PICKER_ID = 1111;
	private int myear;
	private int mmonth;
	private int mday;
	EditText edate;
	CheckBox caddtocalendar, csmscrew, cemailcrew;
	AlertDialog alertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.startrescheduling);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		iexit = (ImageView) findViewById(R.id.iexit);
		idate = (ImageView) findViewById(R.id.idate);
		ttaskid = (TextView) findViewById(R.id.taskid);
		ttasktype = (TextView) findViewById(R.id.tasktype);
		ttasklocation = (TextView) findViewById(R.id.tasklocation);
		ttaskdate = (TextView) findViewById(R.id.taskdate);
		ttaskstatus = (TextView) findViewById(R.id.taskstatus);
		tname = (TextView) findViewById(R.id.tname);
		edate = (EditText) findViewById(R.id.editdatepicker);
		caddtocalendar = (CheckBox) findViewById(R.id.caddtocalendar);
		csmscrew = (CheckBox) findViewById(R.id.csmscrew);
		cemailcrew = (CheckBox) findViewById(R.id.cemailcrew);
		spinassigned = (Spinner) findViewById(R.id.spinassignedto);
		brescheduleTask = (Button) findViewById(R.id.bverifylocation);
		bcanceltask = (Button) findViewById(R.id.bcancel);
		bcanceltask.setOnClickListener(this);
		brescheduleTask.setOnClickListener(this);
		spinassigned.setOnItemSelectedListener(this);
		edate.setOnClickListener(this);

		database = this.openOrCreateDatabase("RufuTech", MODE_PRIVATE, null);
		GetConfigDetails();
		GetSelectedTaskfromSqlite();
		if(mTaskid.contains("RD") || mTaskid.contains("RO"))
		{
			mTaskid = mTaskid.substring(2);
		}
		ttaskid.setText(getText(R.string.taskid) + ": " + mTaskid);
		ttasktype.setText(getText(R.string.tasktype) + ": " + mTasktype);
		ttasklocation.setText(getText(R.string.tasklocation) + ": "
				+ mTasklocation);
		ttaskdate.setText(getText(R.string.taskdate) + ": " + mTaskdate);
		ttaskstatus.setText(getText(R.string.status) + ": " + mTaskstatus);
		tname.setText(getText(R.string.inspector) + ": " + mTaskassignedto);

		iexit.setOnClickListener(this);

		progressDialog = ProgressDialog.show(AdminRescheduleInspection.this,
				"In progress", "Please wait");
		new Thread(new Runnable() {
			public void run() {

				GetFieldMangerfromServer();
			}
		}).start();
		final Calendar c = Calendar.getInstance();
		myear = c.get(Calendar.YEAR);
		int mon = c.get(Calendar.MONTH) + 1;
		mday = c.get(Calendar.DAY_OF_MONTH);
		edate.setText(mon + "-" + mday + "-" + myear);

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
		if (v == brescheduleTask) {
			mDate = edate.getText().toString();
			if (mCrewid == null) {
				Toast("Select Field Manager before adding task");
			} else if (mDate != null && !mDate.isEmpty()) {

				progressDialog = ProgressDialog.show(AdminRescheduleInspection.this,
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
		if(v == bcanceltask)
		{
			callHome();
		}
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
									AdminRescheduleInspection.this,
									android.R.layout.simple_dropdown_item_1line,
									list);
							spinassigned.setAdapter(dataAdapter);

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

	protected void callHome() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, AdminHome.class);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(AdminRescheduleInspection.this,
				AdminReschedule.class);
		startActivity(intent);
	}

	protected void AddTasktoServer() {
		// TODO Auto-generated method stub
		try {
			mDate = edate.getText().toString();
			SimpleDateFormat newDateFormat = new SimpleDateFormat("MM-dd-yyyy");
			Date MyDate = newDateFormat.parse(mDate);
			newDateFormat.applyPattern("yyyy-MM-dd");
			mDate = newDateFormat.format(MyDate);
			System.err.println(mDate);
			httpClient = new DefaultHttpClient();
			httpPost = new HttpPost(muUrl + "rescheduledb.php");
			System.err.println(muUrl);
			nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("userid", mCrewid));
			nameValuePairs.add(new BasicNameValuePair("taskid", mTaskid));
			nameValuePairs.add(new BasicNameValuePair("date", mDate));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			mResponse = httpClient.execute(httpPost, responseHandler);
			mResponse = mResponse.trim().toString();
			System.err.println(mResponse);
			runOnUiThread(new Runnable() {
				public void run() {
					progressDialog.dismiss();

					if (mResponse.contains("Not Found")) {
						ErrorAddingTask();
					} else {
						try {
							/*jObject = new JSONObject(mResponse);

							JSONArray menuitemArray = jObject
									.getJSONArray("Task");

							for (int i = 0; i < menuitemArray.length(); i++) {

								mTaskid = menuitemArray.getJSONObject(i)
										.getString("taskid").toString();

							}*/
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
					+ ") for the Location: " + mTasklocation + "is created and the job date is: "
					+ mDate);

			startActivity(Intent.createChooser(i, "Send mail..."));

		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(AdminRescheduleInspection.this,
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
					+ ") for the Location: " + mTasklocation + " is created and the job date is: "
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
			intent.putExtra("eventLocation", mTasklocation);
			startActivity(intent);
		} catch (Exception e) {
			System.err.println("Error in Adding caldendar" + e.toString());
		}
	}

	@SuppressWarnings("deprecation")
	protected void SuccessAlert() {
		// TODO Auto-generated method stub
		alertDialog = new AlertDialog.Builder(AdminRescheduleInspection.this)
				.create();
		alertDialog.setIcon(R.drawable.success);
		alertDialog.setMessage("Success");
		alertDialog
				.setMessage("Rescheduling the Task Id "
						+ mTaskid+ " is succeed");
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

				alertDialog.dismiss();
				Intent intent = new Intent(AdminRescheduleInspection.this,
						AdminHome.class);
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
		alertDialog = new AlertDialog.Builder(AdminRescheduleInspection.this)
				.create();
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
				Intent intent = new Intent(AdminRescheduleInspection.this,
						AdminGetTasks.class);
				startActivity(intent);
			}
		});
		alertDialog.show();
		alertDialog.setCancelable(false);
		alertDialog.setCanceledOnTouchOutside(false);
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		Spinner spinner = (Spinner) arg0;
		if (spinner.getId() == R.id.spinassignedto) {
			mCrewid = (crewmylist.get(arg2).get("crewid"));
			
		}
		//Toast.makeText(getApplicationContext(), "Crew:"+mCrewid, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

}
