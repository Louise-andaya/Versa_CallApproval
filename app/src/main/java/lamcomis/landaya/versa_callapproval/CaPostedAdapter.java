package lamcomis.landaya.versa_callapproval;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CaPostedAdapter extends RecyclerView.Adapter<CaPostedAdapter.ViewHolder>{
    private List<CaPostedList> postedca_list;
    public static final String GETLOCATION = "http://"+Variable.link+"posted_ca_loc.php";
    public static final String GETIMAGE = "http://"+Variable.link+"posted_image.php";
    public static final String EMPLOYEENAME = "employee_name";
    public static final String DATE = "dates_ca";
    Context context;
    public CaPostedAdapter(Context context, List<CaPostedList> postedca_list) {
        this.context = context;
        this.postedca_list = postedca_list;
    }

    @Override
    public CaPostedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.postedca_list,parent,false);
        return new CaPostedAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CaPostedAdapter.ViewHolder holder, final int position) {
        final CaPostedList postedca = postedca_list.get(position);
        holder.posted_ca_date.setText(postedca.getPosted_ca_date());
        holder.posted_ca_employee.setText(postedca.getPosted_ca_empNamen());




    }

    @Override
    public int getItemCount() {
        return postedca_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView posted_ca_date, posted_ca_employee;
        Button btn_loc;
        ProgressDialog pd;
        private ArrayList<LatLng> points; //added
        Polyline line;
        LatLng sydney;
        private GoogleMap mMap;
        private HashMap<String, Bitmap> markers;
        public ViewHolder(View itemView) {
            super(itemView);
            pd = new ProgressDialog(context);
            pd.setMessage("Please Wait...");
            points = new ArrayList<>();
            markers = new HashMap<String, Bitmap>();
            posted_ca_date=(TextView)itemView.findViewById(R.id.posted_ca_date);
            posted_ca_employee=(TextView)itemView.findViewById(R.id.posted_ca_employee);
            btn_loc = (Button) itemView.findViewById(R.id.btn_posted_loc);
            btn_loc.setClickable(true);

            btn_loc.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        btn_loc.setClickable(false);
                        if(InternetConnection.checkConnection(context)){
                            OpenDialog();
                        }
                        else{
                            PromptActivity.showAlert(context, "internet");
                            pd.dismiss();
                            btn_loc.setClickable(true);
                        }


                        return true;
                    }
                    return false;
                }

            });


        }


        private void OpenDialog() {
            final Dialog dialog = new Dialog(context);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            /////make map clear
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            dialog.setContentView(R.layout.layout_map);
            dialog.show();
            MapView mMapView = (MapView) dialog.findViewById(R.id.mapView);
            MapsInitializer.initialize(context);

            mMapView.onCreate(dialog.onSaveInstanceState());
            mMapView.onResume();
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {
                    mMap = googleMap;
                    getLocation();
                    pd.show();
                }

            });
            TextView txt_employee = (TextView) dialog.findViewById(R.id.employee_name);
            txt_employee.setText(posted_ca_employee.getText().toString()+"\n"+ posted_ca_date.getText().toString());
            Button cancel_button = (Button) dialog.findViewById(R.id.button_cancel);

            cancel_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    btn_loc.setClickable(true);
                }
            });

        }

        private void getLocation() {
            final ArrayList<Bitmap> bitmaps= new ArrayList<>();
            final String employee_name = posted_ca_employee.getText().toString();
            final String date = posted_ca_date.getText().toString();
            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    GETLOCATION, new Response.Listener<String>() {


                public void onResponse(final String response) {

                    Log.d("urlresponse", response);
                    if (response.contains("null")) {
                        Toast.makeText(context, "Location Not Available", Toast.LENGTH_SHORT).show();
                        btn_loc.setClickable(true);
                        pd.dismiss();
                    } else {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray array = jsonObject.getJSONArray("location");
                            for (int i = 0; i < array.length(); i++) {

                                JSONObject ob = array.getJSONObject(i);
                                final String latitude = ob.getString("latitude");
                                final String longitude = ob.getString("longitude");
                                final String customer = ob.getString("customer");
                                final String minutes = ob.getString("minutes");
                                final String purpose = ob.getString("purpose");
                                final String time = ob.getString("time");
                                final String capture = ob.getString("capture");
                                byte[] decodedString = Base64.decode(capture, Base64.DEFAULT);
                                final Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                bitmaps.add(decodedByte);

                                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                                if(latitude !=null && latitude.length()>0) {
                                    try {
                                        List<Address> addresses = geocoder.getFromLocation(Double.valueOf(latitude), Double.valueOf(longitude), 1);
                                        String address = addresses.get(0).getAddressLine(0);
                                        String city = addresses.get(0).getLocality();
                                        String state = addresses.get(0).getAdminArea();
                                        String country = addresses.get(0).getCountryName();
                                        String postalCode = addresses.get(0).getPostalCode();
                                        String knownName = addresses.get(0).getFeatureName();

                                        sydney = new LatLng(Double.parseDouble(latitude), (Double.parseDouble(longitude)));
                                        //LatLng sydney1 = new LatLng(Double.parseDouble(latitude.get(j).toString()), (Double.parseDouble(longitude.get(j).toString())));
                                        points.add(sydney);
                                        Log.d("mark", points.toString());
                                        Marker myMarker =  mMap.addMarker(new MarkerOptions().position(sydney).title(customer + "\n" + time).snippet("\nMinutes\n" + minutes  + "\n\nPurpose\n" + purpose + "\n" + "\nLocation\n" + city + ", " + country));

                                        markers.put(myMarker.getId(), decodedByte);

                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12), 3000, null);



                                        //redrawLine();
                                    } catch (IOException e) {
                                        e.printStackTrace();

                                    }
                                }
                                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                                    @Override
                                    public View getInfoWindow(Marker marker) {
                                        return null;
                                    }

                                    @Override
                                    public View getInfoContents(Marker marker) {
                                        LinearLayout info = new LinearLayout(context);
                                        info.setOrientation(LinearLayout.VERTICAL);

                                        TextView title = new TextView(context);
                                        title.setTextColor(Color.BLACK);
                                        title.setGravity(Gravity.CENTER);
                                        title.setTypeface(null, Typeface.BOLD);
                                        title.setText(marker.getTitle());

                                        TextView snippet = new TextView(context);
                                        snippet.setTextColor(Color.GRAY);
                                        snippet.setText(marker.getSnippet());
                                        info.addView(title);
                                        final Bitmap image = markers.get(marker.getId());
                                        if(image == null){
                                            Toast.makeText(context, "Image Not Available", Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(500, 500);
                                            ImageView imgMarkerImage = new ImageView(context);
                                            imgMarkerImage.setLayoutParams(lp);
                                            imgMarkerImage.setImageBitmap(image);

                                            info.addView(imgMarkerImage);
                                        }

                                        info.addView(snippet);

                                        return info;
                                    }
                                });

                            }
                            btn_loc.setClickable(true);
                            pd.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            pd.dismiss();
                        }
                    }
                }

            },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            PromptActivity.showAlert(context, "error");
                            btn_loc.setClickable(true);
                            pd.dismiss();
                        }
                    }) {

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put(EMPLOYEENAME, employee_name);
                    map.put(DATE, date);
                    return map;
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(context);
            requestQueue.add(stringRequest);
        }
        private void redrawLine(){
            PolylineOptions options = new PolylineOptions().width(10).color(Color.GREEN).geodesic(true);
            for (int i = 0; i < points.size(); i++) {
                LatLng point = points.get(i);
                options.add(point);
            }

            line = mMap.addPolyline(options); //add Polyline
        }




    }

}