package com.example.crookham.crew;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.crookham.Configure;
import com.example.crookham.R;

@SuppressLint("SimpleDateFormat")
public class CrewHome extends Activity implements OnClickListener {
	SQLiteDatabase database;
	String muUid, muUsername, muPassword, muFname, muLname, muEmail, muMobile,
			muUrl, muRole;

	ImageView iexit;
	TextView twelcome, tdate;
	Button badmin, bgettasks;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.crewhome);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		database = this.openOrCreateDatabase("RufuTech", MODE_PRIVATE, null);

		GetConfigDetails();

		iexit = (ImageView) findViewById(R.id.iexit);
		twelcome = (TextView) findViewById(R.id.twelcome);
		tdate = (TextView) findViewById(R.id.tdate);
		badmin = (Button) findViewById(R.id.badmin);
		bgettasks = (Button) findViewById(R.id.bgettasks);
		iexit.setImageDrawable(getResources().getDrawable(R.drawable.exit));

		SimpleDateFormat formatter = new SimpleDateFormat("EEEE MMM-dd-yyyy");
		String now = formatter.format(new Date());
		twelcome.setText("Hi " + muFname + " " + muLname + " !");
		tdate.setText(now);
		iexit.setOnClickListener(this);
		badmin.setOnClickListener(this);
		bgettasks.setOnClickListener(this);

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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == iexit) {
			callHome();
		}
		if (v == badmin) {
			Intent intent = new Intent(CrewHome.this, Configure.class);
			startActivity(intent);
		}

		if (v == bgettasks) {
			Intent intent = new Intent(CrewHome.this, CrewGetTasks.class);
			startActivity(intent);
		}
	}

	protected void callHome() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
}
