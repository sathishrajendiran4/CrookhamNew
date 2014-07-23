package com.example.crookham.crew;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.crookham.R;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
@SuppressLint("ResourceAsColor")
public class CrewGetTaskList extends BaseAdapter {
	protected static Context Context = null;
	private LayoutInflater inflater;
	String[] tasktype, taskid, taskdate, tasklocation, taskstatus,
			taskassignedto;
	String mTasktype, mTaskid, mTaskdate, mTasklocation, mTaskstatus,
			mTaskassignedto;
	private ProgressDialog progressDialog;
	AlertDialog alertDialog;
	View vi;
	SQLiteDatabase database;

	public CrewGetTaskList(Context context, JSONArray imageArrayJson) {
		Context = context;
		alertDialog = new AlertDialog.Builder(Context).create();
		database = Context.openOrCreateDatabase("RufuTech", 0, null);

		inflater = LayoutInflater.from(context);
		progressDialog = new ProgressDialog(Context);
		progressDialog.setMessage("Loading...");
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
		this.tasktype = new String[imageArrayJson.length()];
		this.taskid = new String[imageArrayJson.length()];
		this.tasklocation = new String[imageArrayJson.length()];
		this.taskdate = new String[imageArrayJson.length()];
		this.taskstatus = new String[imageArrayJson.length()];
		this.taskassignedto = new String[imageArrayJson.length()];

		try {
			for (int i = 0; i < imageArrayJson.length(); i++) {
				JSONObject image = imageArrayJson.getJSONObject(i);
				mTasktype = image.getString("tasktype");
				mTaskid = image.getString("taskid");
				mTasklocation = image.getString("locationid");
				mTaskstatus = image.getString("status");
				mTaskdate = image.getString("date");
				mTaskassignedto = image.getString("assignedto");

				taskid[i] = mTaskid;
				tasktype[i] = mTasktype;

				taskstatus[i] = mTaskstatus;
				taskdate[i] = mTaskdate;
				taskassignedto[i] = mTaskassignedto;

				tasklocation[i] = mTasklocation;
			}
		} catch (Exception e) {
		}
	}

	public int getCount() {
		return tasktype.length;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	@SuppressWarnings("deprecation")
	public View getView(final int position, View convertView, ViewGroup parent) {
		vi = convertView;
		vi = inflater.inflate(R.layout.tasklist, null);
		TextView ttasktye = (TextView) vi.findViewById(R.id.tasktype);
		TextView ttaskid = (TextView) vi.findViewById(R.id.taskid);
		TextView ttasklocation = (TextView) vi.findViewById(R.id.tasklocation);
		TextView ttaskstatus = (TextView) vi.findViewById(R.id.taskstatus);
		TextView ttaskdate = (TextView) vi.findViewById(R.id.taskdate);

		TextView ttaskassignedto = (TextView) vi
				.findViewById(R.id.taskassignedto);

		mTasktype = tasktype[position].toString();
		mTaskid = taskid[position].toString();
		mTasklocation = tasklocation[position].toString();
		mTaskstatus = taskstatus[position].toString();
		mTaskdate = taskdate[position].toString();
		mTaskassignedto = taskassignedto[position].toString();

		ttasktye.setText(mTasktype);
		ttaskid.setText(mTaskid);
		ttasklocation.setText(mTasklocation);
		ttaskstatus.setText(mTaskstatus);
		ttaskdate.setText(mTaskdate);
		ttaskassignedto.setText(mTaskassignedto);

		if (mTaskstatus.contains("Completed")) {
			vi.setBackgroundDrawable(Context.getResources().getDrawable(
					R.drawable.red));
		}
		if (mTaskstatus.contains("In Progress")
				|| (mTaskstatus.contains(Context.getText(R.string.tempstop).toString()))) {
			vi.setBackgroundDrawable(Context.getResources().getDrawable(
					R.drawable.orange));
		}
		if (mTaskstatus.contains(Context.getText(R.string.activestatus)
				.toString())) {
			vi.setBackgroundDrawable(Context.getResources().getDrawable(
					R.drawable.green));
		}

		vi.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String mTasktype = tasktype[position].toString();
				String mTaskid = taskid[position].toString();
				String mTasklocation = tasklocation[position].toString();
				String mTaskdate = taskdate[position].toString();
				String mTaskstatus = taskstatus[position].toString();
				String mTaskassignedto = taskassignedto[position].toString();
				try {
					database.execSQL("delete from selectedtask");
					database.execSQL("insert into selectedtask(id ,type,location ,date,status,assignedto) values"
							+ " ('"
							+ taskid[position]
							+ "','"
							+ tasktype[position]
							+ "','"
							+ tasklocation[position]
							+ "','"
							+ taskdate[position]
							+ "','"
							+ taskstatus[position]
							+ "','" + taskassignedto[position] + "')");
					System.err.println("Success select");

				} catch (Exception e) {
					System.err.println("Insert error selectedtask:"
							+ e.toString());
				}
				if (mTaskstatus.contains("Done")) {
				} else {

					if ((mTaskstatus.contains(Context.getText(
							R.string.activestatus).toString()))
							&& (mTasktype.contains("Detasseling"))) {
						Intent intent = new Intent(Context,
								StartCrewDetasseling.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						Context.startActivity(intent);
					} else if ((mTaskstatus.contains("In Progress"))
							&& (mTasktype.contains("Detasseling"))) {
						Intent intent = new Intent(Context,
								InProgressCrewDetasseling.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						Context.startActivity(intent);
					} else if ((mTaskstatus.contains(Context.getText(R.string.tempstop).toString()))
							&& (mTasktype.contains("Detasseling"))) {
						Intent intent = new Intent(Context,
								CrewFreezeDetasseling.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						Context.startActivity(intent);
					} else if ((mTaskstatus.contains(Context.getText(
							R.string.activestatus).toString()))
							&& (mTasktype.contains("Rogueing"))) {
						Intent intent = new Intent(Context,
								CrewStartRogueing.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						Context.startActivity(intent);
					} else if ((mTaskstatus.contains("In Progress"))
							&& (mTasktype.contains("Rogueing"))) {
						Intent intent = new Intent(Context,
								CrewInProgressRogueing.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						Context.startActivity(intent);
					} else if ((mTaskstatus.contains(Context.getText(R.string.tempstop).toString()))
							&& (mTasktype.contains("Rogueing"))) {
						Intent intent = new Intent(Context,
								CrewFreezeRogueing.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						Context.startActivity(intent);
					}

					System.err.println(mTaskstatus);
					System.err.println(mTasktype);
					System.err.println(mTaskid);
					System.out.println(mTaskdate);
					System.err.println(mTasklocation);
					System.err.println(mTaskassignedto);

				}
			}

		});
		return vi;
	}
}
