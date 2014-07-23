package com.example.crookham.fieldmanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.crookham.R;
import com.example.crookham.admin.GPSTracker;

@SuppressLint("SimpleDateFormat")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FMInProgressInspection extends Activity implements OnClickListener {
	String muUid, muUsername, muPassword, muFname, muLname, muEmail, muMobile,
			muUrl, muRole;
	SQLiteDatabase database;
	String mTaskid, mTasktype, mTasklocation, mTaskdate, mTaskstatus,
			mTaskassignedto;
	TextView ttaskid, ttasktype, ttasklocation, ttaskdate, ttaskstatus, tname;
	ImageView iexit;
	HttpPost httpPost;
	HttpClient httpClient;
	List<NameValuePair> nameValuePairs;
	String mResponse;
	ProgressDialog progressDialog;
	JSONObject jObject;
	Button btemporarystop, bpermanentstop, bactionrequired, blevel2pulldown,
			bdataentrybox;
	EditText ecomments;
	String mComments, mPulldownvalues;
	Button bpersonpulldown, bemail, bsms, blevel1pulldown, bnotifyfieldmanager;
	String mPName, mPMobile, mPemail, mLevelpulldownoption,
			mLevel1pulldownSeedrowselection, mLevel1PlantDate,
			mLevel150percentSilkDate, mLevel1Detasseldate, mLevel1Harvestdate,
			mLevel1StandCount, mLevel1pulldownBullrowselection,
			mLevel1TimingPullDownselection,
			mLevel1pulldownBull5opercentTasselDate;
	ArrayList<HashMap<String, String>> mylist;
	TextView twelcome, tdate;

	static final int Level1PlantDATE_DIALOG_ID = 0;
	static final int Level150percentsilkDATE_DIALOG_ID = 1;
	static final int Level1detasselDATE_DIALOG_ID = 2;
	static final int Level1harvestDATE_DIALOG_ID = 3;

	static final int Level1BullPlantDATE_DIALOG_ID = 4;
	static final int Level1Bull50percenttasselDATE_DIALOG_ID = 5;

	private int mYear;
	private int mMonth;
	private int mDay;
	TextView tlevelpulldowntext, ttimer;
	GPSTracker gps;
	double latitude, longitude;
	String lat, lon;
	private Handler myHandler = new Handler();
	long Time1;
	long timeInMillies = 0L;
	long timeSwap = 0L;
	long finalTime = 0L;
	String mNumberofWorkers, mStartDate, mID, Time, mWorkedtime;
	static long diffSeconds;
	static long diffMinutes;
	static long diffHours;
	String[] time;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.inprogressinspection);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);

		twelcome = (TextView) findViewById(R.id.twelcome);
		tdate = (TextView) findViewById(R.id.tdate);

		iexit = (ImageView) findViewById(R.id.iexit);
		ttimer = (TextView) findViewById(R.id.textTimer);
		ttaskid = (TextView) findViewById(R.id.taskid);
		ttasktype = (TextView) findViewById(R.id.tasktype);
		ttasklocation = (TextView) findViewById(R.id.tasklocation);
		ttaskdate = (TextView) findViewById(R.id.taskdate);
		ttaskstatus = (TextView) findViewById(R.id.taskstatus);
		tname = (TextView) findViewById(R.id.tname);
		btemporarystop = (Button) findViewById(R.id.temperarorystop);
		bpermanentstop = (Button) findViewById(R.id.permanentstop);
		ecomments = (EditText) findViewById(R.id.ecomments);
		bpersonpulldown = (Button) findViewById(R.id.bpersonpulldown);
		bemail = (Button) findViewById(R.id.bemail);
		bsms = (Button) findViewById(R.id.bsms);
		bnotifyfieldmanager = (Button) findViewById(R.id.bnotifyfieldmanager);
		blevel1pulldown = (Button) findViewById(R.id.level1pulldown);
		tlevelpulldowntext = (TextView) findViewById(R.id.tlevelpulldowntext);
		blevel2pulldown = (Button) findViewById(R.id.level2pulldown);
		bdataentrybox = (Button) findViewById(R.id.dataentrybox);
		bactionrequired = (Button) findViewById(R.id.actionrequired);

		database = this.openOrCreateDatabase("RufuTech", MODE_PRIVATE, null);
		GetConfigDetails();
		GetSelectedTaskfromSqlite();
		progressDialog = ProgressDialog.show(FMInProgressInspection.this,
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
		tname.setText(getText(R.string.assignedto) + ": " + mTaskassignedto);

		iexit.setOnClickListener(this);
		btemporarystop.setOnClickListener(this);
		bpermanentstop.setOnClickListener(this);
		bpersonpulldown.setOnClickListener(this);
		bemail.setOnClickListener(this);
		bsms.setOnClickListener(this);
		blevel1pulldown.setOnClickListener(this);
		bnotifyfieldmanager.setOnClickListener(this);
		bactionrequired.setOnClickListener(this);

		SimpleDateFormat formatter = new SimpleDateFormat("EEEE MMM-dd-yyyy");
		String now = formatter.format(new Date());
		twelcome.setText("Hi " + muFname + " " + muLname + " !");
		tdate.setText(now);

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
			System.err.println(Time1);
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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == iexit) {
			callHome();
		}
		if (v == bactionrequired) {
			progressDialog = ProgressDialog.show(FMInProgressInspection.this,
					"In progress", "Please wait");
			new Thread(new Runnable() {
				public void run() {

					UpdateInspectionstatus1("Completed");

				}

			}).start();

		}
		if (v == btemporarystop) {
			progressDialog = ProgressDialog.show(FMInProgressInspection.this,
					"In progress", "Please wait");
			new Thread(new Runnable() {
				public void run() {

					UpdateInspectionstatus(getText(R.string.tempstop)
							.toString());

				}

			}).start();
			myHandler.removeCallbacks(updateTimerMethod);
		}
		if (v == bpermanentstop) {
			progressDialog = ProgressDialog.show(FMInProgressInspection.this,
					"In progress", "Please wait");
			new Thread(new Runnable() {
				public void run() {

					UpdateInspectionstatus("Completed");

				}

			}).start();
			myHandler.removeCallbacks(updateTimerMethod);
		}
		if (v == bpersonpulldown) {
			progressDialog = ProgressDialog.show(FMInProgressInspection.this,
					"In progress", "Please wait");
			new Thread(new Runnable() {
				public void run() {

					GetPullDownPerson();
				}

			}).start();

		}
		if (v == bemail) {
			mComments = ecomments.getText().toString();
			if (mPemail != null && !mPemail.isEmpty()) {
				if (mComments != null && !mComments.isEmpty()) {
					Intent intent = new Intent(Intent.ACTION_SENDTO);
					intent.setType("text/plain");
					intent.putExtra(Intent.EXTRA_SUBJECT,
							"Crookham Application");
					intent.putExtra(Intent.EXTRA_TEXT, mComments);
					intent.setData(Uri.parse("mailto:" + mPemail));
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);

				} else {
					Toast("Enter comments to send E-mail");
				}
			} else {
				Toast.makeText(getApplicationContext(), "Select Pull Person",
						Toast.LENGTH_SHORT).show();
			}

		}

		if (v == bsms) {
			mComments = ecomments.getText().toString();
			if (mPMobile != null && !mPMobile.isEmpty()) {
				if (mComments != null && !mComments.isEmpty()) {
					try {
						Uri smsUri = Uri.parse("tel:" + mPMobile);
						Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
						intent.putExtra("sms_body", mComments);
						intent.setType("vnd.android-dir/mms-sms");
						startActivity(intent);
					} catch (Exception e) {
						Toast("Sorry can't send SMS");
					}
				} else {
					Toast("Enter comments to send SMS");
				}

			} else {
				Toast.makeText(getApplicationContext(), "Select Pull Person",
						Toast.LENGTH_SHORT).show();
			}

		}
		if (v == blevel1pulldown) {
			Level1PullDown();
		}
		if (v == bnotifyfieldmanager) {
			// bsms.setVisibility(View.VISIBLE);
			bpersonpulldown.setVisibility(View.VISIBLE);
			// bemail.setVisibility(View.VISIBLE);
		}
	}

	protected void Toast(String string) {
		// TODO Auto-generated method stub
		android.widget.Toast.makeText(getApplicationContext(), string,
				android.widget.Toast.LENGTH_SHORT).show();
	}

	private void Level1PullDown() {
		// TODO Auto-generated method stub
		blevel2pulldown.setVisibility(View.INVISIBLE);
		bdataentrybox.setVisibility(View.INVISIBLE);
		final Dialog dialog = new Dialog(FMInProgressInspection.this);
		WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();

		wmlp.gravity = Gravity.CENTER;
		wmlp.y = 100; // y position
		dialog.setContentView(R.layout.level1pulldownpopup);
		dialog.setTitle("Select Level 1 Pull Down Option");

		Button cancel = (Button) dialog.findViewById(R.id.cancel);
		Button bullrow = (Button) dialog.findViewById(R.id.bullrow);
		Button seedrow = (Button) dialog.findViewById(R.id.seedrow);
		// if button is clicked, close the custom dialog
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				//blevel1pulldown.setText(R.string.level1pulldown);
			}
		});
		bullrow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.cancel();
				mLevelpulldownoption = "Bull Row";
				//blevel1pulldown.setText(mLevelpulldownoption);

				BullRowSelection();
			}
		});
		seedrow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.cancel();
				mLevelpulldownoption = "Seed Row";
				//blevel1pulldown.setText(mLevelpulldownoption);
				SeedRowSelction();
			}
		});

		dialog.show();
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);

	}

	@SuppressWarnings("deprecation")
	protected void BullRowSelection() {
		// TODO Auto-generated method stub

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				FMInProgressInspection.this, android.R.layout.select_dialog_item);
		adapter.add("Plant Date");
		adapter.add("Stand Count");
		adapter.add("50% Tassel Date");
		adapter.add("Timing Pull Down");
		adapter.add("Stand Estimated Pounds");
		adapter.add("Set Estimated Pounds");
		AlertDialog.Builder builder = new AlertDialog.Builder(
				FMInProgressInspection.this);
		builder.setTitle("Select Bull Row option");
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				mLevel1pulldownBullrowselection = adapter.getItem(item)
						.toString();

				if (mLevel1pulldownBullrowselection.equals("Plant Date")) {
					blevel2pulldown.setText(mLevel1pulldownBullrowselection);
					blevel2pulldown.setVisibility(View.VISIBLE);
					final Calendar c = Calendar.getInstance();
					mYear = c.get(Calendar.YEAR);
					mMonth = c.get(Calendar.MONTH);
					mDay = c.get(Calendar.DAY_OF_MONTH);
					showDialog(Level1BullPlantDATE_DIALOG_ID);
				}
				if (mLevel1pulldownBullrowselection.equals("50% Tassel Date")) {
					blevel2pulldown.setText(mLevel1pulldownBullrowselection);
					blevel2pulldown.setVisibility(View.VISIBLE);
					final Calendar c = Calendar.getInstance();
					mYear = c.get(Calendar.YEAR);
					mMonth = c.get(Calendar.MONTH);
					mDay = c.get(Calendar.DAY_OF_MONTH);
					showDialog(Level1Bull50percenttasselDATE_DIALOG_ID);
				}
				if (mLevel1pulldownBullrowselection.equals("Stand Count")) {
					blevel2pulldown.setText(mLevel1pulldownBullrowselection);
					blevel2pulldown.setVisibility(View.VISIBLE);
					ShowStandCoundDialog(mLevel1pulldownBullrowselection, "1",
							"999");
				}
				if (mLevel1pulldownBullrowselection.equals("Timing Pull Down")) {
					blevel2pulldown.setText(mLevel1pulldownBullrowselection);
					blevel2pulldown.setVisibility(View.VISIBLE);
					TimingPullDownPopup(mLevel1pulldownBullrowselection);
				}
				if (mLevel1pulldownBullrowselection
						.equals("Stand Estimated Pounds")) {
					blevel2pulldown.setText(mLevel1pulldownBullrowselection);
					blevel2pulldown.setVisibility(View.VISIBLE);
					ShowStandCoundDialog(mLevel1pulldownBullrowselection, "1",
							"9999");
				}
				if (mLevel1pulldownBullrowselection
						.equals("Set Estimated Pounds")) {
					blevel2pulldown.setText(mLevel1pulldownBullrowselection);
					blevel2pulldown.setVisibility(View.VISIBLE);
					ShowStandCoundDialog(mLevel1pulldownBullrowselection, "1",
							"9999");
				}
			}
		});
		AlertDialog alert = builder.create();
		WindowManager.LayoutParams wmlp = alert.getWindow().getAttributes();

		wmlp.gravity = Gravity.CENTER;
		wmlp.y = 200; // y position
		alert.setButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
				//blevel1pulldown.setText(R.string.level1pulldown);
			}
		});
		alert.show();
		alert.setCancelable(false);
		alert.setCanceledOnTouchOutside(false);

	}

	@SuppressWarnings("deprecation")
	protected void TimingPullDownPopup(
			final String mLevel1pulldownBullrowselection2) {
		// TODO Auto-generated method stub
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				FMInProgressInspection.this, android.R.layout.select_dialog_item);
		adapter.add("Early");
		adapter.add("O.K.");
		adapter.add("Late");
		AlertDialog.Builder builder = new AlertDialog.Builder(
				FMInProgressInspection.this);
		builder.setTitle("Timing Pull Down");
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				mLevel1TimingPullDownselection = mLevel1pulldownBullrowselection2
						+ ": " + adapter.getItem(item).toString();
				if (mLevel1TimingPullDownselection.contains("Early")) {
					ShowStandCoundDialog(mLevel1TimingPullDownselection, "1",
							"20");

				}
				if (mLevel1TimingPullDownselection.contains("Late")) {
					ShowStandCoundDialog(mLevel1TimingPullDownselection, "1",
							"20");
				}
				if (mLevel1TimingPullDownselection.contains("O.K.")) {
					CallLevel1PullDownValues(mLevelpulldownoption,
							mLevel1pulldownBullrowselection2, "O.K.");
				}
			}
		});
		AlertDialog alert = builder.create();

		WindowManager.LayoutParams wmlp = alert.getWindow().getAttributes();

		wmlp.gravity = Gravity.CENTER;
		wmlp.y = 200; // y position

		alert.setButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
		alert.show();
		alert.setCancelable(false);
		alert.setCanceledOnTouchOutside(false);

	}

	@SuppressWarnings("deprecation")
	protected void SeedRowSelction() {
		// TODO Auto-generated method stub
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				FMInProgressInspection.this, android.R.layout.select_dialog_item);
		adapter.add("Plant Date");
		adapter.add("Stand Count");
		adapter.add("50% Silk Date");
		adapter.add("De-Tassel Date");
		adapter.add("Harvest Date");
		AlertDialog.Builder builder = new AlertDialog.Builder(
				FMInProgressInspection.this);
		builder.setTitle("Select Seed Row option");
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				mLevel1pulldownSeedrowselection = adapter.getItem(item)
						.toString();
				if (mLevel1pulldownSeedrowselection.equals("Plant Date")) {
					blevel2pulldown.setText(mLevel1pulldownSeedrowselection);
					blevel2pulldown.setVisibility(View.VISIBLE);
					final Calendar c = Calendar.getInstance();
					mYear = c.get(Calendar.YEAR);
					mMonth = c.get(Calendar.MONTH);
					mDay = c.get(Calendar.DAY_OF_MONTH);
					showDialog(Level1PlantDATE_DIALOG_ID);
				} else if (mLevel1pulldownSeedrowselection
						.equals("50% Silk Date")) {
					blevel2pulldown.setText(mLevel1pulldownSeedrowselection);
					blevel2pulldown.setVisibility(View.VISIBLE);
					final Calendar c = Calendar.getInstance();
					mYear = c.get(Calendar.YEAR);
					mMonth = c.get(Calendar.MONTH);
					mDay = c.get(Calendar.DAY_OF_MONTH);
					showDialog(Level150percentsilkDATE_DIALOG_ID);
				} else if (mLevel1pulldownSeedrowselection
						.equals("De-Tassel Date")) {
					blevel2pulldown.setText(mLevel1pulldownSeedrowselection);
					blevel2pulldown.setVisibility(View.VISIBLE);
					final Calendar c = Calendar.getInstance();
					mYear = c.get(Calendar.YEAR);
					mMonth = c.get(Calendar.MONTH);
					mDay = c.get(Calendar.DAY_OF_MONTH);
					showDialog(Level1detasselDATE_DIALOG_ID);
				} else if (mLevel1pulldownSeedrowselection
						.equals("Harvest Date")) {
					blevel2pulldown.setText(mLevel1pulldownSeedrowselection);
					blevel2pulldown.setVisibility(View.VISIBLE);
					final Calendar c = Calendar.getInstance();
					mYear = c.get(Calendar.YEAR);
					mMonth = c.get(Calendar.MONTH);
					mDay = c.get(Calendar.DAY_OF_MONTH);
					showDialog(Level1harvestDATE_DIALOG_ID);
				} else if (mLevel1pulldownSeedrowselection
						.equals("Stand Count")) {
					blevel2pulldown.setText(mLevel1pulldownSeedrowselection);
					blevel2pulldown.setVisibility(View.VISIBLE);
					ShowStandCoundDialog(mLevel1pulldownSeedrowselection, "1",
							"999");
				}
			}
		});
		AlertDialog alert = builder.create();

		WindowManager.LayoutParams wmlp = alert.getWindow().getAttributes();

		wmlp.gravity = Gravity.CENTER;
		wmlp.y = 200; // y position

		alert.setButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
				//blevel1pulldown.setText(R.string.level1pulldown);
			}
		});
		alert.show();
		alert.setCancelable(false);
		alert.setCanceledOnTouchOutside(false);

	}

	protected void ShowStandCoundDialog(final String mlevel1selectionname,
			String Min, String Max) {
		// TODO Auto-generated method stub
		mLevel1StandCount = "1";
		// Create custom dialog object
		final Dialog dialog = new Dialog(FMInProgressInspection.this);

		WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();

		wmlp.gravity = Gravity.CENTER;
		wmlp.y = 200; // y position

		// Include dialog.xml file
		dialog.setContentView(R.layout.standcoundnumberpicker);
		// Set dialog title
		dialog.setTitle("Select " + mlevel1selectionname + " value");

		// set values for custom dialog components - text, image and
		// button
		final NumberPicker numberPicker = (NumberPicker) dialog
				.findViewById(R.id.numberpicker);
		int minimun = Integer.parseInt(Min);
		int maximum = Integer.parseInt(Max);
		numberPicker.setMaxValue(maximum);
		numberPicker.setMinValue(minimun);
		numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);


		numberPicker.setOnValueChangedListener(new OnValueChangeListener() {

			public void onValueChange(NumberPicker picker, int oldVal,
					int newVal) {
				// TODO Auto-generated method stub
				mLevel1StandCount = String.valueOf(newVal);
			}
		});

		dialog.show();
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);

		Button cancel = (Button) dialog.findViewById(R.id.cancel);
		Button submit = (Button) dialog.findViewById(R.id.submit);
		// if decline button is clicked, close the custom dialog
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Close dialog
				getWindow()
						.setSoftInputMode(
								WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

				dialog.dismiss();
				//blevel1pulldown.setText(R.string.level1pulldown);
				blevel2pulldown.setVisibility(View.INVISIBLE);
			}
		});
		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				CallLevel1PullDownValues(mLevelpulldownoption,
						mlevel1selectionname, mLevel1StandCount);
				getWindow()
						.setSoftInputMode(
								WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

				dialog.dismiss();
			}
		});

	}

	@Override
	protected Dialog onCreateDialog(int id) {

		if (id == Level1PlantDATE_DIALOG_ID) {
			DatePickerDialog _date = new DatePickerDialog(this,
					mDateSetListener, mYear, mMonth, mDay) {
				@Override
				public void onDateChanged(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					if (year < mYear)
						view.updateDate(mYear, mMonth, mDay);

					if (monthOfYear < mMonth && year == mYear)
						view.updateDate(mYear, mMonth, mDay);

					if (dayOfMonth < mDay && year == mYear
							&& monthOfYear == mMonth)
						view.updateDate(mYear, mMonth, mDay);
					if (year > mYear)
						view.updateDate(mYear, mMonth, mDay);

				}
			};
			return _date;

		}
		if (id == Level150percentsilkDATE_DIALOG_ID) {
			DatePickerDialog _date = new DatePickerDialog(this,
					mDateSetListener1, mYear, mMonth, mDay) {
				@Override
				public void onDateChanged(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					if (year < mYear)
						view.updateDate(mYear, mMonth, mDay);

					if (monthOfYear < mMonth && year == mYear)
						view.updateDate(mYear, mMonth, mDay);

					if (dayOfMonth < mDay && year == mYear
							&& monthOfYear == mMonth)
						view.updateDate(mYear, mMonth, mDay);
					if (year > mYear)
						view.updateDate(mYear, mMonth, mDay);

				}
			};
			return _date;
		}
		if (id == Level1detasselDATE_DIALOG_ID) {
			DatePickerDialog _date = new DatePickerDialog(this,
					mDateSetListener2, mYear, mMonth, mDay) {
				@Override
				public void onDateChanged(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					if (year < mYear)
						view.updateDate(mYear, mMonth, mDay);

					if (monthOfYear < mMonth && year == mYear)
						view.updateDate(mYear, mMonth, mDay);

					if (dayOfMonth < mDay && year == mYear
							&& monthOfYear == mMonth)
						view.updateDate(mYear, mMonth, mDay);
					if (year > mYear)
						view.updateDate(mYear, mMonth, mDay);

				}
			};
			return _date;
		}
		if (id == Level1harvestDATE_DIALOG_ID) {
			DatePickerDialog _date = new DatePickerDialog(this,
					mDateSetListener3, mYear, mMonth, mDay) {
				@Override
				public void onDateChanged(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					if (year < mYear)
						view.updateDate(mYear, mMonth, mDay);

					if (monthOfYear < mMonth && year == mYear)
						view.updateDate(mYear, mMonth, mDay);

					if (dayOfMonth < mDay && year == mYear
							&& monthOfYear == mMonth)
						view.updateDate(mYear, mMonth, mDay);
					if (year > mYear)
						view.updateDate(mYear, mMonth, mDay);

				}
			};
			return _date;
		}
		if (id == Level1BullPlantDATE_DIALOG_ID) {
			DatePickerDialog _date = new DatePickerDialog(this,
					mDateSetListener4, mYear, mMonth, mDay) {
				@Override
				public void onDateChanged(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					if (year < mYear)
						view.updateDate(mYear, mMonth, mDay);

					if (monthOfYear < mMonth && year == mYear)
						view.updateDate(mYear, mMonth, mDay);

					if (dayOfMonth < mDay && year == mYear
							&& monthOfYear == mMonth)
						view.updateDate(mYear, mMonth, mDay);
					if (year > mYear)
						view.updateDate(mYear, mMonth, mDay);

				}
			};
			return _date;
		}
		if (id == Level1Bull50percenttasselDATE_DIALOG_ID) {
			DatePickerDialog _date = new DatePickerDialog(this,
					mDateSetListener5, mYear, mMonth, mDay) {
				@Override
				public void onDateChanged(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					if (year < mYear)
						view.updateDate(mYear, mMonth, mDay);

					if (monthOfYear < mMonth && year == mYear)
						view.updateDate(mYear, mMonth, mDay);

					if (dayOfMonth < mDay && year == mYear
							&& monthOfYear == mMonth)
						view.updateDate(mYear, mMonth, mDay);
					if (year > mYear)
						view.updateDate(mYear, mMonth, mDay);

				}
			};
			return _date;
		}
		return null;
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear + 1;
			mDay = dayOfMonth;
			mLevel1PlantDate = mYear + "-" + mMonth + "-" + mDay;
			CallLevel1PullDownValues(mLevelpulldownoption,
					mLevel1pulldownSeedrowselection, mLevel1PlantDate);

		}
	};

	private DatePickerDialog.OnDateSetListener mDateSetListener1 = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear + 1;
			mDay = dayOfMonth;
			mLevel150percentSilkDate = mYear + "-" + mMonth + "-" + mDay;
			CallLevel1PullDownValues(mLevelpulldownoption,
					mLevel1pulldownSeedrowselection, mLevel150percentSilkDate);

		}
	};
	private DatePickerDialog.OnDateSetListener mDateSetListener2 = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear + 1;
			mDay = dayOfMonth;
			mLevel1Detasseldate = mYear + "-" + mMonth + "-" + mDay;
			CallLevel1PullDownValues(mLevelpulldownoption,
					mLevel1pulldownSeedrowselection, mLevel1Detasseldate);
		}
	};
	private DatePickerDialog.OnDateSetListener mDateSetListener3 = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear + 1;
			mDay = dayOfMonth;
			mLevel1Harvestdate = mYear + "-" + mMonth + "-" + mDay;
			CallLevel1PullDownValues(mLevelpulldownoption,
					mLevel1pulldownSeedrowselection, mLevel1Harvestdate);

		}
	};
	private DatePickerDialog.OnDateSetListener mDateSetListener4 = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear + 1;
			mDay = dayOfMonth;
			mLevel1PlantDate = mYear + "-" + mMonth + "-" + mDay;
			CallLevel1PullDownValues(mLevelpulldownoption,
					mLevel1pulldownBullrowselection, mLevel1PlantDate);

		}

	};
	private DatePickerDialog.OnDateSetListener mDateSetListener5 = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear + 1;
			mDay = dayOfMonth;
			mLevel150percentSilkDate = mYear + "-" + mMonth + "-" + mDay;
			CallLevel1PullDownValues(mLevelpulldownoption,
					mLevel1pulldownBullrowselection, mLevel150percentSilkDate);

		}
	};

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
				Intent intent = new Intent(FMInProgressInspection.this,
						FMGetTasks.class);
				startActivity(intent);
			}
		});
		alertDialog.show();
		alertDialog.setCancelable(false);
		alertDialog.setCanceledOnTouchOutside(false);
	}

	protected void CallLevel1PullDownValues(String mLevel1Type,
			String mLevel1Section, String mlevel1value) {
		// TODO Auto-generated method stub
		bdataentrybox.setText(mlevel1value);
		bdataentrybox.setVisibility(View.VISIBLE);

		mPulldownvalues = mLevel1Type + " > " + mLevel1Section + " > "
				+ mlevel1value;
		tlevelpulldowntext.setText(mPulldownvalues);

	}

	protected void callHome() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, FMHome.class);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		myHandler.removeCallbacks(updateTimerMethod);
		Intent intent = new Intent(FMInProgressInspection.this,
				FMGetTasks.class);
		startActivity(intent);
	}

	private void UpdateInspectionstatus(String status) {
		// TODO Auto-generated method stub
		mComments = ecomments.getText().toString();
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String DateTime = formatter.format(new Date());
			httpClient = new DefaultHttpClient();
			httpPost = new HttpPost(muUrl + "updateinspectionstatus.php");
			nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("taskid", mTaskid));
			nameValuePairs.add(new BasicNameValuePair("status", status));
			nameValuePairs.add(new BasicNameValuePair("comments", mComments));
			nameValuePairs.add(new BasicNameValuePair("datetime", DateTime));
			nameValuePairs.add(new BasicNameValuePair("pulldown",
					mPulldownvalues));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			mResponse = httpClient.execute(httpPost, responseHandler);
			System.err.println(mResponse);
			mResponse = mResponse.trim().toString();
			runOnUiThread(new Runnable() {
				public void run() {
					progressDialog.cancel();
					if (mResponse.contains("Success")) {
						Intent intent = new Intent(FMInProgressInspection.this,
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

	private void UpdateInspectionstatus1(String status) {
		// TODO Auto-generated method stub
		mComments = ecomments.getText().toString();
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String DateTime = formatter.format(new Date());
			httpClient = new DefaultHttpClient();
			httpPost = new HttpPost(muUrl + "updateinspectionstatus.php");
			nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("taskid", mTaskid));
			nameValuePairs.add(new BasicNameValuePair("status", status));
			nameValuePairs.add(new BasicNameValuePair("comments", mComments));
			nameValuePairs.add(new BasicNameValuePair("pulldown",
					mPulldownvalues));
			nameValuePairs.add(new BasicNameValuePair("rework", "Yes"));

			nameValuePairs.add(new BasicNameValuePair("datetime", DateTime));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			mResponse = httpClient.execute(httpPost, responseHandler);
			System.err.println("mresponse" + mResponse);
			mResponse = mResponse.trim().toString();
			runOnUiThread(new Runnable() {
				public void run() {
					progressDialog.cancel();
					if (mResponse.contains("Success")) {
						Intent intent = new Intent(FMInProgressInspection.this,
								FMReWork.class);
						intent.putExtra("locationname", mTasklocation);
						intent.putExtra("taskid", mTaskid);
						intent.putExtra("tasktype", mTasktype);
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

	private void GetPullDownPerson() {
		// TODO Auto-generated method stub
		try {

			httpClient = new DefaultHttpClient();
			httpPost = new HttpPost(muUrl + "getpulldownperson.php");

			nameValuePairs = new ArrayList<NameValuePair>(2);
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			mResponse = httpClient.execute(httpPost, responseHandler);
			mResponse = mResponse.trim().toString();
			runOnUiThread(new Runnable() {
				@SuppressWarnings("deprecation")
				public void run() {
					progressDialog.dismiss();
					if (mResponse.contains("Not Found")) {
						AlertDialog alertDialog = new AlertDialog.Builder(
								FMInProgressInspection.this).create();
						alertDialog.setTitle("No Field Manager Found");

						alertDialog.setButton("OK",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										dialog.cancel();
									}
								});

						alertDialog.show();
						alertDialog.setCancelable(false);
						alertDialog.setCanceledOnTouchOutside(false);
					} else {
						try {
							mylist = new ArrayList<HashMap<String, String>>();
							final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
									FMInProgressInspection.this,
									android.R.layout.select_dialog_item);
							jObject = new JSONObject(mResponse);

							JSONArray menuitemArray = jObject
									.getJSONArray("PullDown");
							for (int i = 0; i < menuitemArray.length(); i++) {
								HashMap<String, String> pulldownmap = new HashMap<String, String>();

								String name = menuitemArray.getJSONObject(i)
										.getString("name").toString();
								String mobile = menuitemArray.getJSONObject(i)
										.getString("mobile").toString();

								String email = menuitemArray.getJSONObject(i)
										.getString("email").toString();
								adapter.add(name);
								pulldownmap.put("name", name);
								pulldownmap.put("mobile", mobile);
								pulldownmap.put("email", email);
								mylist.add(pulldownmap);
							}

							AlertDialog.Builder builder = new AlertDialog.Builder(
									FMInProgressInspection.this);
							builder.setTitle("Select Field Manager");
							builder.setAdapter(adapter,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int item) {
											mPName = mylist.get(item)
													.get("name").toString();
											mPMobile = mylist.get(item)
													.get("mobile").toString();
											mPemail = mylist.get(item)
													.get("email").toString();
											bpersonpulldown.setText(mPName);
											bemail.setVisibility(View.VISIBLE);
											bsms.setVisibility(View.VISIBLE);
										}
									});

							AlertDialog alert = builder.create();

							alert.setButton("Cancel",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											dialog.cancel();
										}
									});
							alert.show();
							alert.setCancelable(false);
							alert.setCanceledOnTouchOutside(false);

						} catch (Exception e) {
							// TODO: handle exception
							System.err.println("Error in Parsing json"
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
					Toast.makeText(getApplicationContext(),
							"Error in connectin", Toast.LENGTH_LONG).show();
				}
			});

		}

	}
	
	protected void GetTaskDetails() {
		// TODO Auto-generated method stub
		try {
			httpClient = new DefaultHttpClient();
			System.err.println(muUrl);
			httpPost = new HttpPost(muUrl + "getinspectiondetails.php");
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

}
