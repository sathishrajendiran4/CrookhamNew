package com.example.crookham.fieldmanager;

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
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.crookham.R;

public class FMRogueingRework extends Activity implements OnClickListener,
		OnItemSelectedListener {
	SQLiteDatabase database;
	String muUid, muUsername, muPassword, muFname, muLname, muEmail, muMobile,
			muUrl, muRole, mLocationid;
	ImageView iexit, idate;
	String mLocaiton, mRework;
	Spinner screw, sreasonforrework;
	TextView tlocation, trework;
	String mCrewid, mCrewname, mCrewFname, mCrewLname, mCrewEmail, mCrewMobile,
			mDate, mTaskid;
	String mResponse, mResponse1, mComments, mReasonforrework, Response;
	HttpPost httpPost, httpPost1;
	HttpClient httpClient, httpClient1;
	List<NameValuePair> nameValuePairs;
	JSONObject jObject;
	AlertDialog alertDialog;
	ProgressDialog progressDialog;
	HashMap<String, String> map;
	HashMap<String, String> map1;
	ArrayList<HashMap<String, String>> crewmylist = new ArrayList<HashMap<String, String>>();
	EditText edate, ecomments;
	CheckBox caddtocalendar, csmscrew, cemailcrew;

	Button bcallcrew, baddtask, bback;
	static final int DATE_PICKER_ID = 1111;

	private int year;
	private int month;
	private int day;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.admindetasselingrework);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		database = this.openOrCreateDatabase("RufuTech", MODE_PRIVATE, null);
		edate = (EditText) findViewById(R.id.edate);
		ecomments = (EditText) findViewById(R.id.ecomments);
		caddtocalendar = (CheckBox) findViewById(R.id.caddtocalendar);
		csmscrew = (CheckBox) findViewById(R.id.csmscrew);
		cemailcrew = (CheckBox) findViewById(R.id.cemailcrew);
		bcallcrew = (Button) findViewById(R.id.bcallcrew);
		baddtask = (Button) findViewById(R.id.baddtask);
		bback = (Button) findViewById(R.id.bback);
		idate = (ImageView) findViewById(R.id.idate);

		tlocation = (TextView) findViewById(R.id.tlocation);
		trework = (TextView) findViewById(R.id.trework);
		iexit = (ImageView) findViewById(R.id.iexit);
		screw = (Spinner) findViewById(R.id.spinselectcrew);
		sreasonforrework = (Spinner) findViewById(R.id.sreasonforrework);

		iexit.setOnClickListener(this);
		baddtask.setOnClickListener(this);
		bcallcrew.setOnClickListener(this);
		bback.setOnClickListener(this);
		edate.setOnClickListener(this);
		idate.setOnClickListener(this);

		mLocaiton = getIntent().getStringExtra("locationname");
		mRework = getIntent().getStringExtra("rework");

		mTaskid = getIntent().getStringExtra("taskid");
		GetConfigDetails();
		new Thread(new Runnable() {
			public void run() {

				GetLocationid();
			}
		}).start();
		System.err.println("Location ID" + mLocationid);
		tlocation.setText(getText(R.string.tasklocation) + ": " + mLocaiton);
		trework.setText(getText(R.string.rework) + ": " + mRework);
		screw.setOnItemSelectedListener(this);
		progressDialog = ProgressDialog.show(FMRogueingRework.this,
				"In progress", "Please wait");
		new Thread(new Runnable() {
			public void run() {

				GetCrewfromServer();
			}
		}).start();

		List<String> list = new ArrayList<String>();
		list.add("Open Hot Spot");
		list.add("Pulled Too Deep");
		list.add("Bad Pull");
		list.add("Good De-Tassel Quality");
		list.add("Fair De-Tassel Quality");
		list.add("Poor De-Tassel Quality");
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sreasonforrework.setAdapter(dataAdapter);
		sreasonforrework.setOnItemSelectedListener(this);

		final Calendar c = Calendar.getInstance();
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);

	}

	private void GetLocationid() {
		// TODO Auto-generated method stub
		try {
			httpClient = new DefaultHttpClient();
			httpPost = new HttpPost(muUrl + "getlocationid.php");

			nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs
					.add(new BasicNameValuePair("locationname", mLocaiton));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			Response = httpClient.execute(httpPost, responseHandler);
			runOnUiThread(new Runnable() {
				public void run() {
					progressDialog.dismiss();
					Response = Response.trim().toString();
					try {
						jObject = new JSONObject(Response);

						JSONArray menuitemArray = jObject
								.getJSONArray("locationid");

						for (int i = 0; i < menuitemArray.length(); i++) {
							mLocationid = menuitemArray.getJSONObject(i)
									.getString("id").toString();
							System.err.println("Location ID" + mLocationid);
						}

					} catch (Exception e) {
						System.err.println("Error in parsing3" + e.toString());
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

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == iexit) {
			callHome();
		}
		if (v == idate || v == edate) {
			showDialog(DATE_PICKER_ID);
		}
		if (v == bback) {
			callHome();
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
		if (v == baddtask) {
			mDate = edate.getText().toString();
			if (mDate != null && !mDate.isEmpty()) {

				progressDialog = ProgressDialog.show(FMRogueingRework.this,
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
	}

	private void Toast(String string) {
		// TODO Auto-generated method stub
		android.widget.Toast.makeText(getApplicationContext(), string,
				android.widget.Toast.LENGTH_SHORT).show();
	}

	protected void callHome() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, FMHome.class);

		startActivity(intent);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Spinner spinner = (Spinner) parent;
		if (spinner.getId() == R.id.sreasonforrework) {
			mReasonforrework = sreasonforrework.getSelectedItem().toString();

		}

		if (spinner.getId() == R.id.spinselectcrew) {
			mCrewid = (crewmylist.get(position).get("crewid"));
			progressDialog = ProgressDialog.show(FMRogueingRework.this,
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

	@SuppressLint("SimpleDateFormat")
	protected void AddTasktoServer() {
		// TODO Auto-generated method stub
		try {
			mComments = ecomments.getText().toString();
			mDate = edate.getText().toString();
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String DateTime = formatter.format(new Date());
			httpClient = new DefaultHttpClient();
			httpPost = new HttpPost(muUrl + "addreworkdetasseling.php");

			nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs
					.add(new BasicNameValuePair("locationid", mLocationid));
			nameValuePairs.add(new BasicNameValuePair("assignedto", mCrewid));
			nameValuePairs.add(new BasicNameValuePair("taskdate", mDate));
			nameValuePairs.add(new BasicNameValuePair("createdby", muUid));
			nameValuePairs.add(new BasicNameValuePair("detasselingno", "0"));
			nameValuePairs.add(new BasicNameValuePair("createddate", DateTime));
			nameValuePairs.add(new BasicNameValuePair("prefix", "RR"));
			nameValuePairs.add(new BasicNameValuePair("tasktype", "Rogueing"));
			nameValuePairs.add(new BasicNameValuePair("status", getText(
					R.string.activestatus).toString()));
			nameValuePairs.add(new BasicNameValuePair("reasonforrework",
					mReasonforrework));
			nameValuePairs.add(new BasicNameValuePair("comments", mComments));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			mResponse = httpClient.execute(httpPost, responseHandler);
			mResponse = mResponse.trim().toString();
			runOnUiThread(new Runnable() {
				public void run() {
					progressDialog.dismiss();

					if (mResponse.contains("Error")) {
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
							System.err.println("Error in parsing4"
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
			i.putExtra(Intent.EXTRA_SUBJECT, "De-Tasseling");
			i.putExtra(Intent.EXTRA_TEXT, "Hi " + mCrewFname
					+ "\n\nThe De-Tesseling (" + mTaskid
					+ ") for the Location: " + mLocaiton + "(" + mLocationid
					+ ") is created and the job date is: " + mDate);

			startActivity(Intent.createChooser(i, "Send mail..."));

		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(FMRogueingRework.this,
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
					+ ") for the Location: " + mLocaiton + "(" + mLocationid
					+ ") is created and the job date is: " + mDate, null, null);
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
			intent.putExtra("title", "De-Tasseling");
			intent.putExtra("description", "De-Tasseling (" + mTaskid
					+ ") is added for the crew " + mCrewname);
			intent.putExtra("eventLocation", mLocaiton);
			startActivity(intent);
		} catch (Exception e) {
			System.err.println("Error in Adding caldendar" + e.toString());
		}
	}

	@SuppressWarnings("deprecation")
	protected void SuccessAlert() {
		// TODO Auto-generated method stub
		alertDialog = new AlertDialog.Builder(FMRogueingRework.this)
				.create();
		alertDialog.setIcon(R.drawable.success);
		alertDialog.setMessage("Success");
		alertDialog
				.setMessage("Added De-Tasseling successfully and the Task Id is:"
						+ mTaskid);
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

				alertDialog.dismiss();
				Intent intent = new Intent(FMRogueingRework.this,
						FMHome.class);
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
		alertDialog = new AlertDialog.Builder(FMRogueingRework.this)
				.create();
		alertDialog.setIcon(R.drawable.warning);
		alertDialog.setMessage("Error");
		alertDialog.setMessage("Error in adding De-Tasselling");
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

	private void GetCrewfromServer() {
		// TODO Auto-generated method stub

		try {
			httpClient1 = new DefaultHttpClient();
			httpPost1 = new HttpPost(muUrl + "getcrew.php");

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
						CrewNotFound("Crew not found");
					} else {
						try {
							jObject = new JSONObject(mResponse1);

							JSONArray menuitemArray = jObject
									.getJSONArray("Crew");
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
									FMRogueingRework.this,
									android.R.layout.simple_dropdown_item_1line,
									list);
							screw.setAdapter(dataAdapter);

						} catch (Exception e) {
							System.err.println("Error in parsing1"
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
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_PICKER_ID:

			// open datepicker dialog.
			// set date picker for current date
			// add pickerListener listner to date picker
			return new DatePickerDialog(this, pickerListener, year, month, day);
		}
		return null;
	}

	private DatePickerDialog.OnDateSetListener pickerListener = new DatePickerDialog.OnDateSetListener() {

		// when dialog box is closed, below method will be called.
		@Override
		public void onDateSet(DatePicker view, int selectedYear,
				int selectedMonth, int selectedDay) {

			year = selectedYear;
			month = selectedMonth + 1;
			day = selectedDay;

			mDate = year + "-" + month + "-" + day;
			edate.setText(mDate);
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		}
	};

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
				Intent intent = new Intent(FMRogueingRework.this,
						FMHome.class);
				startActivity(intent);

			}
		});
		alertDialog.show();
		alertDialog.setCancelable(false);
		alertDialog.setCanceledOnTouchOutside(false);
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
								System.err.println(mCrewEmail);
								System.err.println(mCrewMobile);
							}

						} catch (Exception e) {
							System.err.println("Error in parsing2"
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

}
