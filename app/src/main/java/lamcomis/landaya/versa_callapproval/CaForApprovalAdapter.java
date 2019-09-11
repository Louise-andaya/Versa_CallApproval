package lamcomis.landaya.versa_callapproval;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
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
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CaForApprovalAdapter extends  RecyclerView.Adapter<CaForApprovalAdapter.ViewHolder> implements OnMapReadyCallback {
    public Context context;
    private List<CaForApprovalList> list;
    public static final String GETLOCATION = "http://"+Variable.link+"ca_loc.php";
    public static final String GETIMAGE = "http://"+Variable.link+"ca_image.php";
    public static final String CUSTOMER = "customer";
    public static final String EMPLOYEENAME = "employee_name";

    public CaForApprovalAdapter(Context context, List<CaForApprovalList> list) {
        this.context = context;
        this.list = list;



    }

    @Override
    public CaForApprovalAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.fa_list_item, parent, false);
        return new CaForApprovalAdapter.ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull final CaForApprovalAdapter.ViewHolder holder, final int position) {
        final CaForApprovalList dlist = list.get(position);

        holder.ca_faDate.setText(dlist.getFa_caDate());
        holder.ca_faEmployee.setText(dlist.getFa_caEmployee());
        holder.ca_faCustomer.setText(dlist.getFa_caCustomer());
        holder.ca_faCano.setText(dlist.getFa_caNo());
        holder.purpose = dlist.getFa_caPurpose();

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView toggle;
        private TextView ca_faDate, ca_faEmployee, ca_faCustomer, ca_faCano;
        CardView card;
        Button btn_loc, btn_image;
        ProgressDialog pd;
        String purpose;


        public ViewHolder(View itemView) {
            super(itemView);
            pd = new ProgressDialog(context);
            pd.setMessage("Please Wait...");
            toggle = itemView.findViewById(R.id.ca_fa_imageViewExpand);
            card = itemView.findViewById(R.id.fa_cv);
            card.setVisibility(View.GONE);
            ca_faDate = (TextView) itemView.findViewById(R.id.ca_fa_date);
            ca_faEmployee = (TextView) itemView.findViewById(R.id.ca_fa_employee_name);
            ca_faCustomer = (TextView) itemView.findViewById(R.id.ca_fa_customer);
            ca_faCano = (TextView) itemView.findViewById(R.id.ca_fa_no);
            btn_loc = (Button) itemView.findViewById(R.id.ca_fa_loc);
            btn_loc.setClickable(true);
            btn_image = (Button) itemView.findViewById(R.id.ca_fa_image);
            btn_image.setClickable(true);
            toggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (card.getVisibility() == View.GONE) {
                        expand();

                    } else {
                        collapse();

                    }
                }

            });
            btn_loc.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        pd.show();
                        btn_loc.setClickable(false);
                        final String customer = ca_faCustomer.getText().toString();
                        final String employee_name = ca_faEmployee.getText().toString();
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

                                            OpenDialog(latitude, longitude);
                                            btn_loc.setClickable(true);
                                        }

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
                                map.put(CUSTOMER, customer);
                                return map;
                            }
                        };

                        RequestQueue requestQueue = Volley.newRequestQueue(context);
                        requestQueue.add(stringRequest);

                        return true;
                    }
                    return false;
                }

            });
            btn_image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        pd.show();
                        btn_image.setClickable(false);
                        final String customer = ca_faCustomer.getText().toString();
                        final String employee_name = ca_faEmployee.getText().toString();
                        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                                GETIMAGE, new Response.Listener<String>() {


                            public void onResponse(final String response) {

                                Log.d("urlresponse", response);
                                if (response.contains("null")) {
                                    Toast.makeText(context, "Image Not Available", Toast.LENGTH_SHORT).show();
                                    btn_image.setClickable(true);
                                    pd.dismiss();
                                } else {
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        JSONArray array = jsonObject.getJSONArray("image");
                                        for (int i = 0; i < array.length(); i++) {

                                            JSONObject ob = array.getJSONObject(i);
                                            final String image = ob.getString("capture");

                                            OpenImage(image);
                                            btn_image.setClickable(true);

                                        }

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
                                        btn_image.setClickable(true);
                                        pd.dismiss();
                                    }
                                }) {

                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> map = new HashMap<String, String>();
                                map.put(EMPLOYEENAME, employee_name);
                                map.put(CUSTOMER, customer);
                                return map;
                            }
                        };

                        RequestQueue requestQueue = Volley.newRequestQueue(context);
                        requestQueue.add(stringRequest);

                        return true;
                    }
                    return false;
                }

            });


        }


        private void expand() {
            // Set visible
            card.setVisibility(View.VISIBLE);

            final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            card.measure(widthSpec, heightSpec);

            ValueAnimator animator = slideAnimator(0, card.getMeasuredHeight());
            animator.start();
            // toggle.setImageResource(R.drawable.less);
            rotate(-180.0f);
        }

        private void collapse() {
            int finalHeight = card.getHeight();
            ValueAnimator animator = slideAnimator(finalHeight, 0);

            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    //
                    //  toggle.setImageResource(R.drawable.more);
                    rotate(360.0f);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    //Height=0, but it set visibility to GONE
                    card.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            animator.start();
        }

        private ValueAnimator slideAnimator(int start, int end) {
            ValueAnimator animator = ValueAnimator.ofInt(start, end);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    // Update height
                    int value = (Integer) animation.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = card.getLayoutParams();
                    layoutParams.height = value;
                    card.setLayoutParams(layoutParams);
                }
            });

            return animator;
        }

        private void rotate(float angle) {
            Animation animation = new RotateAnimation(0.0f, angle, Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setFillAfter(true);
            animation.setDuration(250);
            toggle.startAnimation(animation);

        }

        private void OpenDialog(final String latitude, final String longitude) {
            pd.dismiss();
            final Dialog dialog = new Dialog(context);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            /////make map clear
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            dialog.setContentView(R.layout.layout_map);
            dialog.setCancelable(false);
            dialog.show();
            MapView mMapView = (MapView) dialog.findViewById(R.id.mapView);
            MapsInitializer.initialize(context);

            mMapView.onCreate(dialog.onSaveInstanceState());
            mMapView.onResume();
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(Double.valueOf(latitude), Double.valueOf(longitude), 1);
                        String address = addresses.get(0).getAddressLine(0);
                        String city = addresses.get(0).getLocality();
                        String state = addresses.get(0).getAdminArea();
                        String country = addresses.get(0).getCountryName();
                        String postalCode = addresses.get(0).getPostalCode();
                        String knownName = addresses.get(0).getFeatureName();
                        LatLng posisiabsen = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude)); ////your lat lng
                        googleMap.addMarker(new MarkerOptions().position(posisiabsen).title(ca_faCustomer.getText().toString()).snippet("\nPurpose\n" + purpose + "\n"+ "\nCA Date\n" + ca_faDate.getText().toString() + "\n" +  "\nLocation\n" + city + ", " + country));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(posisiabsen));
                        googleMap.getUiSettings().setZoomControlsEnabled(true);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(posisiabsen, 12), 3000, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
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
                            info.addView(snippet);

                            return info;
                        }
                    });
                }

            });
            TextView txt_employee = (TextView) dialog.findViewById(R.id.employee_name);
            txt_employee.setText(ca_faEmployee.getText().toString());
            Button cancel_button = (Button) dialog.findViewById(R.id.button_cancel);

            cancel_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    btn_loc.setClickable(true);
                }
            });

        }

        private void OpenImage(String image) {
            pd.dismiss();

            //String image1 = "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEB AQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/2wBDAQEBAQEBAQEBAQEBAQEBAQEBAQEB AQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/wAARCADAAJADASIA AhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQA AAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3 ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWm p6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEA AwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSEx BhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElK U1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3 uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD+izRd Bjv0F1cSmZG2eSqspiDKZYzvy4DNGy7QgTMb4dwrjA6628NwnJMYfBQMQpHAaViHJL5BVQRhwwRW O7AJGXpcskLm/tI4l0VpDaW9hGIf7Qy80k8upuEdHulhMkkd3Myeabb7IkZk+yHPq+nxx+SpjPLq A2do+dTISOFB5z+73knYYxkDJP0sbLRcqTjB6rmb5bqWspS5dlazurtbRkfGqL95NuyScrtXu7tr lvJb6tz5pLa/Na3J23hS0ZtzwRoARk4ZgSCQw2M7BypXB3qFwGXJwca9v4V0sKwNsj5xk+XGB/Ed uWRiwJQEdAG7ANXYW9kXyXOApGenoT8v3g3DKQxO0grgOAa34LFQNrKqBduMCPLEbsMSMgEHJJI7 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 ";
            final Dialog dialog = new Dialog(context);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            /////make map clear
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            dialog.setContentView(R.layout.layout_image);
            dialog.setCancelable(false);
            dialog.show();
            ImageView capture = (ImageView) dialog.findViewById(R.id.ca_imageCapture);


            byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            capture.setImageBitmap(decodedByte);

            TextView txt_employee = (TextView) dialog.findViewById(R.id.image_name);
            txt_employee.setText(ca_faEmployee.getText().toString());
            Button cancel_button = (Button) dialog.findViewById(R.id.btn_close);

            cancel_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    btn_image.setClickable(true);
                }
            });


        }
    }



    }
