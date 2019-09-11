package lamcomis.landaya.versa_callapproval;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import butterknife.ButterKnife;

import static android.media.MediaRecorder.VideoSource.CAMERA;

public class CallApprovalActivity extends AppCompatActivity {
    static String url = Variable.link;
    public static final String INSERT_URL = "http://"+ url +"insert.php";
    public static final String CA_NO = "ca_no";
    public static final String CUSTOMER = "customer";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "long";
    public static final String ADDRESS = "address";
    public static final String CAPTURE = "capture";
    public static final String REMARKS = "minutes";
    public static final String EMP_NAME = "employee_name";
    public static final String EMP_ID = "employee_id";
    public static final String JOB_DESC = "job_desc";
    public static final String PURPOSE = "purpose";
    public static final String STATUS = "status";
    public static final String DATE = "dates_ca";

    private String mLastUpdateTime;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private Boolean mRequestingLocationUpdates;


    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    public static final String ALLOW_KEY = "ALLOWED";
    public static final String CAMERA_PREF = "camera_pref";
    ImageView ImageCapture;
    byte[] CaImage = null;
    Button capture;
    String img_sign;

    ImageView back;
    FloatingActionButton save;
    TextView ca_date, ca_no, customer;
    EditText purpose,minutes;

    String TAG = "Location Status";
    String employee_id;
    SessionManager sessionManager;

    String inst_ca, inst_customer, inst_porpose,  inst_remarks, inst_date, inst_status, inst_employee_name, inst_job_desc, inst_lat, inst_longi;
    String cur_address;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_approval);
        ButterKnife.bind(this);
        init();
        restoreValuesFromBundle(savedInstanceState);
        sessionManager = new SessionManager(this);
        HashMap<String, String> user = sessionManager.getUserDetails();
        employee_id = user.get(SessionManager.KEY_USERID);
        pd = new ProgressDialog(this);
        pd.setMessage("Saving, Please Wait....");
        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy hh:mm a");
        inst_date = format.format(today);
        inst_status = "SVD";
        inst_employee_name = getIntent().getExtras().getString("employee_name");
        inst_job_desc = getIntent().getExtras().getString("job_desc");
        ca_date = (TextView)findViewById(R.id.ca_date);
        ca_date.setText(getIntent().getExtras().getString("ca_date"));
        ca_no = (TextView)findViewById(R.id.ca_no);
        ca_no.setText(getIntent().getExtras().getString("ca_no"));
        customer = (TextView)findViewById(R.id.customer_name);
        customer.setText(StringUtils.capitalize(getIntent().getExtras().getString("customer_name").toLowerCase().trim()));
        purpose = (EditText) findViewById(R.id.purpose);
        purpose.setText(getIntent().getExtras().getString("purpose"));
        minutes = (EditText) findViewById(R.id.remarks);
        back = (ImageView) findViewById(R.id.btn_back);
        startLocation();
        back.setClickable(true);
        back.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_UP){
                    back.setClickable(false);
                    finish();
                    return true;
                }
                return false;
            }
        });
        ImageCapture = (ImageView)findViewById(R.id.imageCapture);
        capture = (Button)findViewById(R.id.btn_capture);
        capture.setClickable(true);
        capture.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_UP) {
                    capture.setClickable(false);
                    if (ContextCompat.checkSelfPermission(CallApprovalActivity.this,
                            Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {

                        if (getFromPref(CallApprovalActivity.this, ALLOW_KEY)) {

                            showAlert();

                        } else if (ContextCompat.checkSelfPermission(CallApprovalActivity.this,
                                Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            // Should we show an explanation?
                            if (ActivityCompat.shouldShowRequestPermissionRationale(CallApprovalActivity.this,
                                    Manifest.permission.CAMERA)) {
                                showAlert();
                            } else {
                                // No explanation needed, we can request the permission.
                                ActivityCompat.requestPermissions(CallApprovalActivity.this,
                                        new String[]{Manifest.permission.CAMERA},
                                        MY_PERMISSIONS_REQUEST_CAMERA);
                            }
                        }

                    } else {
                        OpenCamera();
                    }
                    return true;
                }
                return false;
            }
        });
        save = (FloatingActionButton) findViewById(R.id.btn_save);
        save.setClickable(true);
        save.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_UP){
                    save.setClickable(false);
                    pd.show();
                    if(InternetConnection.checkConnection(getApplicationContext())){
                        SaveFunction();
                    }
                    else{
                        pd.dismiss();
                        PromptActivity.showAlert(CallApprovalActivity.this, "internet");
                        save.setClickable(true);

                    }
                    return true;
                }
                return false;
            }
        });

    }

    private void SaveFunction() {
        if(mCurrentLocation != null){
            inst_lat = String.valueOf(mCurrentLocation.getLatitude());
            inst_longi = String.valueOf(mCurrentLocation.getLongitude());
            try {
                Geocoder geocoder = new Geocoder(CallApprovalActivity.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
                if (addresses != null && addresses.size() > 0) {
                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    Log.d(TAG, "getAddress:  address" + address);
                    Log.d(TAG, "getAddress:  city" + city);
                    cur_address = address;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            InsertData();
        }
        else{
            pd.dismiss();
            startLocation();
            save.setClickable(true);
        }
    }


    private void InsertData() {
        inst_remarks = minutes.getText().toString();
        if (inst_remarks.equals("")) {
            Toast.makeText(getApplicationContext(), "Minutes are Required", Toast.LENGTH_LONG).show();
            save.setClickable(true);
            pd.dismiss();
        }
        else {
            inst_ca = ca_no.getText().toString();
            inst_customer = customer.getText().toString();
            inst_porpose = purpose.getText().toString();
            final String final_address = Objects.toString(cur_address, "None").toString();
            final String signature = Objects.toString(img_sign, "").toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, INSERT_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("response", response);
                    if (response.contains("success")) {
                        save.setClickable(true);
                        pd.dismiss();
                        PromptActivity.showAlert(CallApprovalActivity.this, "success");

                    } else {
                        Toast.makeText(getApplicationContext(), "Call Approval Not Inserted", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                        save.setClickable(true);
                    }

                }


            },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            PromptActivity.showAlert(CallApprovalActivity.this, "error");
                            pd.dismiss();


                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put(CA_NO, inst_ca);
                    map.put(CUSTOMER, inst_customer);
                    map.put(LATITUDE, inst_lat);
                    map.put(LONGITUDE, inst_longi);
                    map.put(ADDRESS, final_address);
                    map.put(CAPTURE, signature);
                    map.put(REMARKS, inst_remarks);
                    map.put(EMP_NAME, inst_employee_name);
                    map.put(EMP_ID, employee_id);
                    map.put(JOB_DESC, inst_job_desc);
                    map.put(PURPOSE, inst_porpose);
                    map.put(STATUS, inst_status);
                    map.put(DATE, inst_date);

                    return map;
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
        }
    }


    //LOCATION
    private void startLocation() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mRequestingLocationUpdates = true;
                        startLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            // open device settings when the permission is
                            // denied permanently
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }
    private void openSettings() {
        Intent intent = new Intent(this, UserDashBoard.class);
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    private void init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

            }
        };

        mRequestingLocationUpdates = false;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }
    private void restoreValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("is_requesting_updates")) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean("is_requesting_updates");
            }

            if (savedInstanceState.containsKey("last_known_location")) {
                mCurrentLocation = savedInstanceState.getParcelable("last_known_location");
            }

            if (savedInstanceState.containsKey("last_updated_on")) {
                mLastUpdateTime = savedInstanceState.getString("last_updated_on");
            }
        }

    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("is_requesting_updates", mRequestingLocationUpdates);
        outState.putParcelable("last_known_location", mCurrentLocation);
        outState.putString("last_updated_on", mLastUpdateTime);

    }
    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());
                        SaveFunction();

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(CallApprovalActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);

                                Toast.makeText(CallApprovalActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    //CAMERA
    private void OpenCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }
    public void onActivityResult(int requestcode,int resultcode,Intent intent) {
        super.onActivityResult(requestcode, resultcode, intent);
        if(resultcode==RESULT_OK&&requestcode==CAMERA)
        {
            Bitmap bitmap = (Bitmap) intent.getExtras().get("data");
            ImageCapture.setImageBitmap(bitmap);
            capture.setClickable(true);
            save.setClickable(true);
            Bitmap bit = ((BitmapDrawable)ImageCapture.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bit.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            CaImage = baos.toByteArray();
            img_sign = Base64.encodeToString(CaImage, Base64.NO_WRAP);
            Log.d("Image",img_sign);
        }
        else{
            capture.setClickable(true);
        }
    }
    public static void saveToPreferences(Context context, String key, Boolean allowed) {
        SharedPreferences myPrefs = context.getSharedPreferences
                (CAMERA_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean(key, allowed);
        prefsEditor.commit();
    }
    public static Boolean getFromPref(Context context, String key) {
        SharedPreferences myPrefs = context.getSharedPreferences
                (CAMERA_PREF, Context.MODE_PRIVATE);
        return (myPrefs.getBoolean(key, false));
    }
    private void showAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        capture.setClickable(true);
                        dialog.dismiss();
                        finish();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        capture.setClickable(true);
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(CallApprovalActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);

                    }
                });
        alertDialog.show();
    }
    @Override
    public void onRequestPermissionsResult
            (int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                for (int i = 0, len = permissions.length; i < len; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale =
                                ActivityCompat.shouldShowRequestPermissionRationale
                                        (this, permission);
                        if (showRationale) {
                            showAlert();
                        } else if (!showRationale) {
                            // user denied flagging NEVER ASK AGAIN
                            // you can either enable some fall back,
                            // disable features of your app
                            // or open another dialog explaining
                            // again the permission and directing to
                            // the app setting
                            saveToPreferences(CallApprovalActivity.this, ALLOW_KEY, true);

                        }
                    }
                    else{
                        capture.setClickable(true);
                    }
                }
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mRequestingLocationUpdates) {
            // pausing location updates
            stopLocationUpdates();
        }
    }

    public void stopLocationUpdates() {
        // Removing location updates
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //Toast.makeText(getApplicationContext(), "Location updates stopped!", Toast.LENGTH_SHORT).show();

                    }
                });
    }

}
