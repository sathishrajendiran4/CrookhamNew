package com.example.crookham.admin;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.crookham.Configure;
import com.example.crookham.R;

@SuppressLint({ "NewApi", "SimpleDateFormat" })
public class AdminHome extends Activity implements OnClickListener {
	SQLiteDatabase database;
	String muUid, muUsername, muPassword, muFname, muLname, muEmail, muMobile,
			muUrl, muRole;

	ImageView iexit, ilogo;
	TextView twelcome, tdate;
	Button badmin, bgettasks, bnewtask, bmapfield, bbuildcrew, breschedule;
	String mNewTaskType;
	GPSTracker gps;
	double latitude, longitude;
	String lat, lon;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.adminhome);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		database = this.openOrCreateDatabase("RufuTech", MODE_PRIVATE, null);
		// database.execSQL("Drop table locations");
		GetConfigDetails();
		if (muUrl != null && !muUrl.isEmpty()) {

		} else {
			Intent intent = new Intent(AdminHome.this, Configure.class);
			startActivity(intent);
		}
		ilogo = (ImageView) findViewById(R.id.ilist);
		iexit = (ImageView) findViewById(R.id.iexit);
		twelcome = (TextView) findViewById(R.id.twelcome);
		tdate = (TextView) findViewById(R.id.tdate);
		badmin = (Button) findViewById(R.id.badmin);
		bgettasks = (Button) findViewById(R.id.bgettasks);
		bnewtask = (Button) findViewById(R.id.bnewtask);
		bmapfield = (Button) findViewById(R.id.bmapfield);
		bbuildcrew = (Button) findViewById(R.id.bbuildcrew);
		breschedule = (Button) findViewById(R.id.breschedule);
		iexit.setImageDrawable(getResources().getDrawable(R.drawable.exit));

		SimpleDateFormat formatter = new SimpleDateFormat("EEEE MMM-dd-yyyy");
		String now = formatter.format(new Date());
		twelcome.setText("Hi " + muFname + " " + muLname + " !");
		tdate.setText(now);
		ilogo.setOnClickListener(this);
		iexit.setOnClickListener(this);
		badmin.setOnClickListener(this);
		bgettasks.setOnClickListener(this);
		bnewtask.setOnClickListener(this);
		bmapfield.setOnClickListener(this);
		bbuildcrew.setOnClickListener(this);
		breschedule.setOnClickListener(this);

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
		if (v == badmin) {
			Intent intent = new Intent(AdminHome.this, Configure.class);
			startActivity(intent);
		}
		if (v == bgettasks) {
			Intent intent = new Intent(AdminHome.this, AdminGetTasks.class);
			startActivity(intent);
		}
		if (v == bmapfield) {
			Intent intent = new Intent(AdminHome.this,
					Admincheckcoordinates.class);
			startActivity(intent);
		}
		if (v == bnewtask) {
			NewTaskPopup();
		}
		if (v == bbuildcrew) {
			BuildCrewPopup();
		}
		if(v == breschedule)
		{
			Intent i = new Intent(AdminHome.this,AdminReschedule.class);
			startActivity(i);
		}
		if (v == ilogo) {
			try {
				String mVersionname = (this.getPackageManager().getPackageInfo(
						this.getPackageName(), 0).versionName).toString();
				Toast.makeText(getApplicationContext(),
						"App version: " + mVersionname, Toast.LENGTH_LONG)
						.show();
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	@SuppressWarnings("deprecation")
	private void BuildCrewPopup() {
		// TODO Auto-generated method stub
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				AdminHome.this, android.R.layout.select_dialog_item);
		adapter.add("Rougeing");
		adapter.add("De-Tasseling");
		AlertDialog.Builder builder = new AlertDialog.Builder(AdminHome.this);
		builder.setTitle("Select Build Crew");

		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				mNewTaskType = adapter.getItem(item).toString();
				if (mNewTaskType.contains("Cancel")) {
					dialog.cancel();
				} else if (mNewTaskType.contains("De-Tasseling")) {
					Intent intent = new Intent(AdminHome.this,
							AddDeTasseling.class);
					startActivity(intent);

				} else if (mNewTaskType.contains("Rougeing")) {
					Intent intent = new Intent(AdminHome.this,
							AddRogueing.class);
					startActivity(intent);

				} else if (mNewTaskType.contains("Inspection")) {
					Intent intent = new Intent(AdminHome.this,
							AddInspection.class);
					startActivity(intent);

				} else {
					Toast.makeText(getApplicationContext(), "In - Progress",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		AlertDialog alert = builder.create();

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
	private void NewTaskPopup() {
		// TODO Auto-generated method stub

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				AdminHome.this, android.R.layout.select_dialog_item);
		adapter.add("Growing");
		adapter.add("Planting");
		adapter.add("Rougeing");
		adapter.add("De-Tasseling");
		adapter.add("Inspection");
		AlertDialog.Builder builder = new AlertDialog.Builder(AdminHome.this);
		builder.setTitle("Select New Task Type");

		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				mNewTaskType = adapter.getItem(item).toString();
				if (mNewTaskType.contains("Cancel")) {
					dialog.cancel();
				} else if (mNewTaskType.contains("De-Tasseling")) {
					Intent intent = new Intent(AdminHome.this,
							AddDeTasseling.class);
					startActivity(intent);
				} else if (mNewTaskType.contains("Inspection")) {
					Intent intent = new Intent(AdminHome.this,
							AddInspection.class);
					startActivity(intent);
				} else if (mNewTaskType.contains("Rougeing")) {
					Intent intent = new Intent(AdminHome.this,
							AddRogueing.class);
					startActivity(intent);
				} else {
					Toast.makeText(getApplicationContext(), "In - Progress",
							Toast.LENGTH_SHORT).show();
				}

			}
		});
		AlertDialog alert = builder.create();

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

}
