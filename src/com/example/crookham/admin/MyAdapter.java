package com.example.crookham.admin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.crookham.R;

public class MyAdapter extends ArrayAdapter<Item> {

	private final Context context;
	private final ArrayList<Item> itemsArrayList;
	HttpPost httpPost;
	HttpClient httpClient;
	List<NameValuePair> nameValuePairs;
	String mResponse, maTitle, maMessage;
	SQLiteDatabase database;

	public MyAdapter(Context context, ArrayList<Item> itemsArrayList) {

		super(context, R.layout.row, itemsArrayList);

		this.context = context;
		this.itemsArrayList = itemsArrayList;
		database = context.openOrCreateDatabase("RufuTech", 0, null);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		// 1. Create inflater
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// 2. Get rowView from inflater
		View rowView = inflater.inflate(R.layout.row, parent, false);

		// 3. Get the two text view from the rowView
		TextView snoView = (TextView) rowView.findViewById(R.id.textView1);
		TextView fieldView = (TextView) rowView.findViewById(R.id.textView2);
		TextView varietyView = (TextView) rowView.findViewById(R.id.textView3);
		ImageButton bupload = (ImageButton) rowView.findViewById(R.id.button1);
		ImageButton bdelete = (ImageButton) rowView.findViewById(R.id.button2);
		ImageButton bedit = (ImageButton) rowView.findViewById(R.id.button3);

		// 4. Set the text for textView
		snoView.setText("1");
		fieldView.setText(itemsArrayList.get(position).getField());
		varietyView.setText(itemsArrayList.get(position).getVariety());

		bupload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String mlocation = (itemsArrayList.get(position).getLocation());
				String mvariety = (itemsArrayList.get(position).getVariety());
				String mfield = (itemsArrayList.get(position).getField());
				String url = (itemsArrayList.get(position).getUrl());
				String uid=(itemsArrayList.get(position).getUid());
				UploadFieldtoServer(mfield, mvariety,mlocation,url,uid);
			}

			private void UploadFieldtoServer(String mfield, String mvariety,
					String mlocation, String url, String uid) {
				// TODO Auto-generated method stub
				httpClient = new DefaultHttpClient();
				httpPost = new HttpPost(url + "addfield.php");
				nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs
						.add(new BasicNameValuePair("field_name", mfield));
				nameValuePairs.add(new BasicNameValuePair("userid",uid));
				nameValuePairs.add(new BasicNameValuePair("variety", mvariety));
				nameValuePairs.add(new BasicNameValuePair("polygon_data",
						mlocation));
				try {
					httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				try {
					mResponse = httpClient.execute(httpPost, responseHandler);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mResponse = mResponse.trim().toString();
				System.err.println(mResponse);
				if (mResponse.contains("Success")) {
					// Success alert
					Toast.makeText(context, "Upload Succesfull", Toast.LENGTH_LONG).show();
					//database = this.openOrCreateDatabase("RufuTech", 0, null);
					database.execSQL("Delete from locations where name = '"+mfield+"'");
					Intent i = new Intent(context,Admincheckcoordinates.class);
					context.startActivity(i);
				} else if (mResponse.contains("Field Name already exists")) {
					Toast.makeText(context, "Field Name Already Exist", Toast.LENGTH_LONG).show();
				} else { // Failure Alert maTitle = "Failure";
					Toast.makeText(context, "Error while uploading", Toast.LENGTH_LONG).show();
				}
			}
		});
		bdelete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String mfeild = (itemsArrayList.get(position).getField());
				database.execSQL("Delete from locations where name = '"+mfeild+"'");
				Toast.makeText(context, "Delete Successfull", Toast.LENGTH_LONG).show();
				Intent i = new Intent(context,Admincheckcoordinates.class);
				context.startActivity(i);
			}
		});
		bedit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String mlocation = (itemsArrayList.get(position).getLocation());
				String mvariety = (itemsArrayList.get(position).getVariety());
				String mfeild = (itemsArrayList.get(position).getField());
				System.err.println(mfeild + ":" + mvariety + ": "
						+ mlocation);
			}
		});
		// 5. retrn rowView
		return rowView;
	}
}
