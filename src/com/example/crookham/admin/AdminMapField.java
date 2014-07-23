package com.example.crookham.admin;

import java.io.UnsupportedEncodingException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.crookham.Configure;
import com.example.crookham.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class AdminMapField extends FragmentActivity implements OnClickListener,
		OnItemSelectedListener {

	LatLng currentlocation;
	Timer mTimer;
	private GoogleMap googleMap;  
	Context context;
	Button bsave, bupload;
	ToggleButton tbutton;
	ArrayList<LatLng> myList;
	LatLng msourcelatlng, mdestlatlng;
	ArrayList<String> uploadarraylist;
	Marker msmarker, mdmarker;
	HttpPost httpPost;
	HttpClient httpClient;
	List<NameValuePair> nameValuePairs;
	String mResponse;
	AlertDialog alertDialog;
	String maTitle, maMessage;
	SQLiteDatabase database;
	String muUid, muUsername, muPassword, muFname, muLname, muEmail, muMobile,
			muUrl, muRole;
	ImageView iexit, ilist;
	LocationManager manager;
	EditText editfield;
	Spinner editvariety;
	Button cancel, upload, save, tryagain;
	JSONObject jObject;
	ArrayList<HashMap<String, String>> mtasklist;
	String variety, Variety;
	HashMap<String, String> map;
	@SuppressWarnings("rawtypes")
	List list;
	String[] spinnerlist;
	ArrayList<String> crewmylist = new ArrayList<String>();
	ProgressDialog progressDialog;

	@SuppressWarnings("rawtypes")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		list = new ArrayList();
		setContentView(R.layout.adminmapfield);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		database = this.openOrCreateDatabase("RufuTech", MODE_PRIVATE, null);
		try {
			database.execSQL("create table if not exists locations ( name varchar(100),variety varchar(50),polygon blob)");
			System.err.println("Success");
		} catch (Exception e) {
			System.err.println("Error" + e.toString());
		}
		manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		myList = new ArrayList<LatLng>();
		uploadarraylist = new ArrayList<String>();
		alertDialog = new AlertDialog.Builder(AdminMapField.this).create();

		context = this.getApplicationContext();
		tbutton = (ToggleButton) findViewById(R.id.toggle);
		bupload = (Button) findViewById(R.id.bupload);
		bsave = (Button) findViewById(R.id.bsave);

		tbutton.setOnClickListener(this);
		bupload.setOnClickListener(this);
		bsave.setOnClickListener(this);
		bupload.setEnabled(false);
		bsave.setEnabled(false);
		iexit = (ImageView) findViewById(R.id.iexit);
		ilist = (ImageView) findViewById(R.id.ilist);
		iexit.setOnClickListener(this);
		ilist.setOnClickListener(this);
		GetConfigDetails();
		GetSavedLocation();
		if (muUrl != null && !muUrl.isEmpty()) {
			System.err.println(muUrl);// Getting Google Play availability status
			int status = GooglePlayServicesUtil
					.isGooglePlayServicesAvailable(getBaseContext());

			// Showing status
			if (status != ConnectionResult.SUCCESS) { // Google Play Services
														// are
														// not available

				int requestCode = 10;
				Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status,
						this, requestCode);
				dialog.show();

			} else { // Google Play Services are available

				// Getting reference to the SupportMapFragment of
				// activity_main.xml
				SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
						.findFragmentById(R.id.map);

				// Getting GoogleMap object from the fragment
				googleMap = fm.getMap();

				// Enabling MyLocation Layer of Google Map
				googleMap.setMyLocationEnabled(true);

			}
			/*
			 * mTimer = new Timer(); CallFunction();
			 */
		} else {
			Intent intent = new Intent(AdminMapField.this, Configure.class);
			startActivity(intent);
		}
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			public void uncaughtException(Thread t, Throwable e) {
				// TODO implement this

			}
		});

	}

	private void GetSavedLocation() {
		// TODO Auto-generated method stub
		Cursor c = database.rawQuery("SELECT * from locations ", null);
		@SuppressWarnings("unused")
		String mName = null, mPolygon = null;
		byte[] blob;

		if (c != null) {
			if (c.moveToFirst()) {
				do {
					mName = c.getString(c.getColumnIndex("name"));
					blob = c.getBlob(c.getColumnIndex("polygon"));
					try {
						mPolygon = new String(blob, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.err.println("Location NAme:" + mName);
				} while (c.moveToNext());
			}
		}
	}

	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"Your GPS seems to be disabled, do you want to enable it?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								startActivity(new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int id) {
						dialog.cancel();
						callHome();
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
		alert.setCancelable(false);
		alert.setCanceledOnTouchOutside(false);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
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

	private void CallFunction() {
		// TODO Auto-generated method stub
		mTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				// What you want to do goes here
				runOnUiThread(new Runnable() {
					@SuppressWarnings("unused")
					public void run() {
						Location myLocation = googleMap.getMyLocation();
						if (myLocation != null) {
							
							 GPSTracker gpsTracker = new GPSTracker(context); 
							 if(gpsTracker.canGetLocation()) 
							 { 
								 String stringLatitude = String.valueOf(gpsTracker.latitude); 
								 String stringLongitude = String.valueOf(gpsTracker.longitude); 
								 currentlocation = new LatLng(gpsTracker.latitude,gpsTracker.longitude);
							  
							 }
							 
							/*double dclat = googleMap.getMyLocation()
									.getLatitude();
							double dclon = googleMap.getMyLocation()
									.getLongitude();
							currentlocation = new LatLng(dclat, dclon);*/

							if (!myList.contains(currentlocation)
									&& (currentlocation != null)) {
								myList.add(currentlocation);

								double lat = currentlocation.latitude;
								double lon = currentlocation.longitude;
								uploadarraylist.add(String.valueOf(lon) + ","
										+ String.valueOf(lat));

							}
							if (myList.size() == 1) {
								System.err.println("First zoom");
								LatLng CurrentLocation = new LatLng(googleMap
										.getMyLocation().getLatitude(),
										googleMap.getMyLocation()
												.getLongitude());
								CameraPosition cameraPosition = new CameraPosition.Builder()
										.target(CurrentLocation).zoom(18)
										.bearing(70).tilt(25).build();
								googleMap.animateCamera(CameraUpdateFactory
										.newCameraPosition(cameraPosition));

							}
							if (myList.size() >= 2) {
								double slat = myList.get(myList.size() - 2).latitude;
								double slon = myList.get(myList.size() - 2).longitude;

								double dlat = myList.get(myList.size() - 1).latitude;
								double dlon = myList.get(myList.size() - 1).latitude;

								LatLng mmarkersourcelatlng = new LatLng(myList
										.get(1).latitude,
										myList.get(1).longitude);
								msourcelatlng = new LatLng(slat, slon);
								mdestlatlng = new LatLng(dlat, dlon);
								System.err.println("myLocation:"
										+ currentlocation);
								for (int i = 1; i < myList.size() - 1; i++) {
									LatLng src = myList.get(i);
									LatLng dest = myList.get(i + 1);
									Polyline line = googleMap
											.addPolyline(new PolylineOptions()
													// mMap is the Map Object
													.add(new LatLng(
															src.latitude,
															src.longitude),
															new LatLng(
																	dest.latitude,
																	dest.longitude))
													.width(10)
													.color(R.color.pink)
													.geodesic(true));
									msmarker = googleMap
											.addMarker(new MarkerOptions()
													.position(
															mmarkersourcelatlng)
													.title("Source")
													.icon(BitmapDescriptorFactory
															.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

									if (mdmarker != null) {
										mdmarker.remove();
									}
									mdmarker = googleMap
											.addMarker(new MarkerOptions()
													.position(msourcelatlng)
													.title("Destination")
													.icon(BitmapDescriptorFactory
															.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
								}
							}

						} else {
							System.err.println("Mylocation Empty");
						}
					}

				});
			}
		}, 200, 5000);

	}

	@Override
	public void onClick(View v) {
		String mtbtext = tbutton.getText().toString();
		if (v == tbutton) {
			if (mtbtext.contains("Start")) {
				mTimer.cancel();
				bupload.setEnabled(true);
				bsave.setEnabled(true);
			}
			if (mtbtext.contains("Stop")) {
				mTimer = new Timer();
				bupload.setEnabled(false);
				bsave.setEnabled(false);
				CallFunction();
			}

		}
		if (v == bupload) {
			if (mtbtext.contains("Start") && (myList.size() > 3)) {
				bupload.setEnabled(true);
				UploadtoServer();
			} else {
				bupload.setEnabled(false);
			}

		}
		if (v == ilist) {
			PopupMenu popupMenu = new PopupMenu(AdminMapField.this, ilist);
			// Inflating the Popup using xml file
			popupMenu.getMenuInflater().inflate(R.menu.popup,
					popupMenu.getMenu());
			// registering popup with OnMenuItemClickListener
			popupMenu
					.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
						public boolean onMenuItemClick(MenuItem item) {
							String mMaptype = item.getTitle().toString();
							if (mMaptype.equals(getText(R.string.normal))) {
								googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
							} else if (mMaptype
									.equals(getText(R.string.satellite))) {
								googleMap
										.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
							} else if (mMaptype
									.equals(getText(R.string.terrain))) {
								googleMap
										.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
							} else if (mMaptype
									.equals(getText(R.string.hybrid))) {
								googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
							} else if (mMaptype.equals(getText(R.string.none))) {
								googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
							} else if (mMaptype.equals(getText(R.string.clear))) {
								Intent intent = new Intent(AdminMapField.this,
										AdminMapField.class);
								startActivity(intent);
							}
							return true;
						}
					});
			popupMenu.show();// showing popup menu
		}
		if (v == bsave) {
			if (mtbtext.contains("Start") && myList.size() > 3) {
				bsave.setEnabled(true);
				SaveLocation();

			} else {
				bsave.setEnabled(false);
			}

		}
		if (v == iexit) {
			callHome();
		}
	}

	@SuppressWarnings("unchecked")
	private void SaveLocation() {
		// TODO Auto-generated method stub
		final Dialog dialog = new Dialog(AdminMapField.this);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setTitle("Save Data");
		dialog.setContentView(R.layout.saveserver);
		editfield = (EditText) dialog.findViewById(R.id.fieldname);
		editvariety = (Spinner) dialog.findViewById(R.id.varietyname);
		try {

			httpClient = new DefaultHttpClient();
			httpPost = new HttpPost(muUrl + "getvariety.php");
			nameValuePairs = new ArrayList<NameValuePair>(2);
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			mResponse = httpClient.execute(httpPost, responseHandler);
			mResponse = mResponse.trim().toString();
			try {
				jObject = new JSONObject(mResponse);

				JSONArray menuitemArray = jObject.getJSONArray("Variety");
				list.add("Variety");
				for (int i = 0; i < menuitemArray.length(); i++) {
					String mVname = menuitemArray.getJSONObject(i)
							.getString("name").toString();
					list.add(mVname);
					crewmylist.add(mVname);

				}
				@SuppressWarnings("rawtypes")
				ArrayAdapter dataAdapter = new ArrayAdapter(AdminMapField.this,
						android.R.layout.simple_dropdown_item_1line, list);
				editvariety.setAdapter(dataAdapter);
			} catch (Exception e) {
				System.err.println("Error in parsing" + e.toString());
			}
		} catch (final Exception e) {
			System.err.println("Error" + e.toString());
		}
		editvariety.setOnItemSelectedListener(this);
		cancel = (Button) dialog.findViewById(R.id.btn1);
		upload = (Button) dialog.findViewById(R.id.btn2);
		upload.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String mFieldname = editfield.getText().toString();
				String mVarietyname = Variety;
				String mUploadList = String.valueOf(uploadarraylist);
				mUploadList = (mUploadList.substring(1,
						mUploadList.length() - 1));

				// Do something with value!
				if (mFieldname.length() >= 1) {
					if (mVarietyname != null && !mVarietyname.isEmpty()) {
						try {
							database.execSQL("insert into locations (name,variety,polygon) values ('"
									+ mFieldname
									+ "','"
									+ mVarietyname
									+ "','"
									+ mUploadList + "')");
							System.err.println("Success insertf");
							Intent intent = new Intent(AdminMapField.this,
									AdminMapField.class);
							startActivity(intent);
						} catch (Exception e) {
							System.err.println("error in inserting"
									+ e.toString());
						}
					} else {
						try {
							database.execSQL("insert into locations (name,variety,polygon) values ('"
									+ mFieldname
									+ "','"
									+ ""
									+ "','"
									+ mUploadList + "')");
							System.err.println("Success insertf");
							Intent intent = new Intent(AdminMapField.this,
									AdminMapField.class);
							startActivity(intent);
						} catch (Exception e) {
							System.err.println("error in inserting"
									+ e.toString());
						}
					}

				} else {
					Toast.makeText(getApplicationContext(),
							"Enter Field name to upload", Toast.LENGTH_SHORT)
							.show();
				}
			}
		});
		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	@SuppressWarnings("unchecked")
	private void UploadtoServer() {
		// TODO Auto-generated method stub
		final Dialog dialog = new Dialog(AdminMapField.this);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setTitle("Upload Data");
		dialog.setContentView(R.layout.uploadserver);
		editfield = (EditText) dialog.findViewById(R.id.fieldname);
		editvariety = (Spinner) dialog.findViewById(R.id.varietyname);
		try {

			httpClient = new DefaultHttpClient();
			httpPost = new HttpPost(muUrl + "getvariety.php");
			nameValuePairs = new ArrayList<NameValuePair>(2);
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			mResponse = httpClient.execute(httpPost, responseHandler);
			mResponse = mResponse.trim().toString();
			try {
				jObject = new JSONObject(mResponse);

				JSONArray menuitemArray = jObject.getJSONArray("Variety");
				list.add("Variety");
				for (int i = 0; i < menuitemArray.length(); i++) {
					String mVname = menuitemArray.getJSONObject(i)
							.getString("name").toString();
					list.add(mVname);
					crewmylist.add(mVname);

				}
				@SuppressWarnings("rawtypes")
				ArrayAdapter dataAdapter = new ArrayAdapter(AdminMapField.this,
						android.R.layout.simple_dropdown_item_1line, list);
				editvariety.setAdapter(dataAdapter);
				// progressDialog.dismiss();
				// System.err.println(crewmylist);
			} catch (Exception e) {
				System.err.println("Error in parsing" + e.toString());
			}
		} catch (final Exception e) {
			System.err.println("Error" + e.toString());
		}
		editvariety.setOnItemSelectedListener(this);
		cancel = (Button) dialog.findViewById(R.id.btn1);
		upload = (Button) dialog.findViewById(R.id.btn2);

		upload.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method
				final String mFieldname = editfield.getText().toString();
				final String mVarietyname = Variety;
				// Do something with value!
				if (mFieldname.length() >= 1) {
					/*
					 * progressDialog = ProgressDialog.show(AdminMapField.this,
					 * "In progress", "Please wait"); new Thread(new Runnable()
					 * { public void run() {
					 */
					UploadFieldtoServer(mFieldname, mVarietyname);
					/*
					 * } });
					 */
				} else {
					Toast.makeText(getApplicationContext(),
							"Enter Field name to upload", Toast.LENGTH_SHORT)
							.show();
				}
			}
		});
		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) { // TODO Auto-generated method
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	@SuppressWarnings("deprecation")
	protected void UploadFieldtoServer(String mFieldname, String mVarietyname) {
		// TODO Auto-generated method stub
		String mUploadList = String.valueOf(uploadarraylist);
		mUploadList = (mUploadList.substring(1, mUploadList.length() - 1));

		// mUploadList = mUploadList.substring(1, mUploadList.length() - 1);
		try {
			System.err.println("Test" + muUrl);

			httpClient = new DefaultHttpClient();
			httpPost = new HttpPost(muUrl + "addfield.php");
			nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs
					.add(new BasicNameValuePair("field_name", mFieldname));
			nameValuePairs.add(new BasicNameValuePair("userid", muUid));
			nameValuePairs.add(new BasicNameValuePair("variety", mVarietyname));
			nameValuePairs.add(new BasicNameValuePair("polygon_data",
					mUploadList));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			mResponse = httpClient.execute(httpPost, responseHandler);
			mResponse = mResponse.trim().toString();
			System.err.println(mResponse);
				try {

						if (mResponse.contains("Success")) {
							// Success alert
							maTitle = "Success";
							maMessage = "Field added successfully";
							ShowAlert(maTitle, maMessage);
							uploadarraylist = new ArrayList<String>();
							msmarker.remove();
							mdmarker.remove();
							myList = new ArrayList<LatLng>();
						} else if (mResponse
								.contains("Field Name already exists")) {
							alertDialog = new AlertDialog.Builder(
									AdminMapField.this).create();
							alertDialog.setTitle("Information");
							alertDialog.setMessage("Field Name already exists");
							alertDialog.setButton("Cancel",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											Intent intent = new Intent(
													AdminMapField.this,
													AdminMapField.class);
											startActivity(intent);
										}
									});
							alertDialog.setButton2("Try Again",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											dialog.cancel();
										}
									});
							alertDialog.show();
							alertDialog.setCancelable(false);
						} else { // Failure Alert maTitle = "Failure";
							maMessage = "Error in adding field\nTry again";
							ShowAlert1(maTitle, maMessage);
						}
						//Thread.sleep(10000);
					} catch (Exception e) {
					}
				
		} catch (final Exception e) {
			/*
			 * new Thread(new Runnable() { public void run() {
			 * progressDialog.dismiss(); System.err.println("Error" +
			 * e.toString()); } });
			 */
			Toast.makeText(getApplicationContext(), "Error in connection",
					Toast.LENGTH_LONG).show();
		}

	}

	@SuppressWarnings("deprecation")
	private void ShowAlert(String maTitle2, String maMessage2) {
		// TODO Auto-generated method stub
		alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(maTitle2);
		alertDialog.setMessage(maMessage2);
		alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent i = new Intent(AdminMapField.this, AdminMapField.class);
				startActivity(i);
			}
		});
		alertDialog.show();
		alertDialog.setCancelable(false);

	}

	@SuppressWarnings("deprecation")
	private void ShowAlert1(String maTitle2, String maMessage2) {
		// TODO Auto-generated method stub
		alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(maTitle2);
		alertDialog.setMessage(maMessage2);
		alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				alertDialog.dismiss();
			}
		});
		alertDialog.show();
		alertDialog.setCancelable(false);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.upload:
			UploadtoServerSavedLocaiton();

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void UploadtoServerSavedLocaiton() {
		// TODO Auto-generated method stub
		Cursor c = database.rawQuery("SELECT * from locations ", null);
		String mName = null, mPolygon = null;
		byte[] blob;

		if (c != null) {
			if (c.moveToFirst()) {
				do {
					mName = c.getString(c.getColumnIndex("name"));
					blob = c.getBlob(c.getColumnIndex("polygon"));
					try {
						mPolygon = new String(blob, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					UploadSavedLocationtoServer(mName, mPolygon);
				} while (c.moveToNext());
			}
			if (mResponse.contains("Success")) {
				// Success alert
				maTitle = "Success";
				maMessage = "Field added successfully";
				ShowAlert(maTitle, maMessage);
				uploadarraylist = new ArrayList<String>();
				myList = new ArrayList<LatLng>();
				database.execSQL("delete from locations");
				System.err.println("database empty");

			} else {
				// Failure Alert
				maTitle = "Failure";
				maMessage = "Error in adding field\nTry again";
				ShowAlert(maTitle, maMessage);
			}
		}
		c.close();

	}

	private void UploadSavedLocationtoServer(String mName, String mPolygon) {
		// TODO Auto-generated method stub
		try {
			httpClient = new DefaultHttpClient();
			System.err.println("Test" + muUrl);
			httpPost = new HttpPost(muUrl + "addfield.php");
			nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("field_name", mName));
			nameValuePairs.add(new BasicNameValuePair("userid", muUid));
			nameValuePairs
					.add(new BasicNameValuePair("polygon_data", mPolygon));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			mResponse = httpClient.execute(httpPost, responseHandler);
			mResponse = mResponse.trim().toString();

		} catch (Exception e) {
			System.err.println(e.toString());
			Toast.makeText(getApplicationContext(), "Error in connection",
					Toast.LENGTH_LONG).show();
		}

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

	}

	protected void callHome() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, AdminHome.class);
		startActivity(intent);
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		variety = editvariety.getItemAtPosition(arg2).toString();
		if (variety.equals("Select Variety")) {
			Variety = null;
		} else {
			Variety = variety;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}
}
