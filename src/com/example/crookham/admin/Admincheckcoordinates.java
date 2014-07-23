package com.example.crookham.admin;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.example.crookham.R;

public class Admincheckcoordinates extends ListActivity {

	SQLiteDatabase database;
	int sno = 1, i = 0;
	String name, variety, data, location, muUrl, muUid;
	Cursor cursor;
	Context context;
	Button btn;
	ArrayList<Item> items;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.displaycoordinate);
		context = this.getApplicationContext();
		database = context.openOrCreateDatabase("RufuTech", MODE_PRIVATE, null);
		// list = (ListView) findViewById(R.id.tasklistview);
		btn = (Button) findViewById(R.id.btn);
		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(Admincheckcoordinates.this,
						AdminMapField.class);
				startActivity(i);
			}
		});
		geturl();
		getcoordinates();

	}

	private void geturl() {
		// TODO Auto-generated method stub
		Cursor c = database.rawQuery("select * from config", null);
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					muUrl = c.getString(c.getColumnIndex("url"));
					muUid = c.getString(c.getColumnIndex("id"));

				} while (c.moveToNext());
			}
		}
		c.close();
	}

	private void getcoordinates() {
		// TODO Auto-generated method stub
		cursor = database.rawQuery("select * from locations", null);
		items = new ArrayList<Item>();

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					try {
						name = cursor.getString(cursor.getColumnIndex("name"));
						variety = cursor.getString(cursor
								.getColumnIndex("variety"));
						location = cursor.getString(cursor
								.getColumnIndex("polygon"));
						sno = sno + 1;
						// Toast.makeText(getApplicationContext(), name,
						// Toast.LENGTH_LONG).show();
						if (variety.isEmpty()) {
							variety = "Not Available";
							MyAdapter adapter = new MyAdapter(this,
									generateData());
							setListAdapter(adapter);
						} else {
							MyAdapter adapter = new MyAdapter(this,
									generateData());
							setListAdapter(adapter);
						}

					} catch (Exception e) {
						System.err.println(e.toString());
					}

				} while (cursor.moveToNext());
			}
		}
		if (name != null && !name.isEmpty()) {
			System.err.println("Contains");
		} else {
			Intent i = new Intent(Admincheckcoordinates.this,
					AdminMapField.class);
			startActivity(i);
		}
	}

	private ArrayList<Item> generateData() {
		items.add(new Item(name, variety, location, muUrl, muUid));
		return items;
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, AdminHome.class);
		startActivity(intent);
	}

}
