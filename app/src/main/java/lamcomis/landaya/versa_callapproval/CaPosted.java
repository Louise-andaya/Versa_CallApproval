package lamcomis.landaya.versa_callapproval;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;



public class CaPosted extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    String url = Variable.link;
    String POSTED_CA = "http://"+url+"posted_ca.php";
    private static final String DATE_FROM ="date_from";
    private static final String DATE_TO ="date_to";
    private List<CaPostedList> postedca_list;
    private RecyclerView recyclerView;
    private CaPostedAdapter adapter;

    private EditText startDateDisplay;
    private EditText endDateDisplay;


    Button retrieve;
    ProgressDialog pd;
    SwipeRefreshLayout swipeRefreshLayout;

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_ca_posted, viewGroup, false);
        pd = new ProgressDialog(getActivity());
        pd.setMessage("Please Wait...");
        startDateDisplay = (EditText) view.findViewById(R.id.date_from);
        startDateDisplay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    final Calendar myCalendar = Calendar.getInstance();
                    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            // TODO Auto-generated method stub
                            myCalendar.set(Calendar.YEAR, year);
                            myCalendar.set(Calendar.MONTH, monthOfYear);
                            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            String myFormat = "MM/dd/yyyy"; // your format
                            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
                            startDateDisplay.setText(sdf.format(myCalendar.getTime()));
                        }

                    };
                    new DatePickerDialog(getActivity(), date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();

                    return true;
                }
                return false;
            }
        });

        endDateDisplay = (EditText) view.findViewById(R.id.date_to);
        endDateDisplay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    final Calendar myCalendar = Calendar.getInstance();
                    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            // TODO Auto-generated method stub
                            myCalendar.set(Calendar.YEAR, year);
                            myCalendar.set(Calendar.MONTH, monthOfYear);
                            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            String myFormat = "MM/dd/yyyy"; // your format
                            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
                            endDateDisplay.setText(sdf.format(myCalendar.getTime()));
                        }

                    };
                    new DatePickerDialog(getActivity(), date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();

                    return true;
                }
                return false;
            }
        });
        postedca_list = new ArrayList<>();
        recyclerView = view.findViewById(R.id.posted_recycleViewContainer);
        swipeRefreshLayout = view.findViewById(R.id.posted_swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(CaPosted.this).attach(CaPosted.this).commit();
            }
        });

        retrieve = (Button)view.findViewById(R.id.retrieve);
        retrieve.setClickable(true);
        retrieve.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    retrieve.setClickable(false);
                    if(!startDateDisplay.getText().toString().equals("") && !endDateDisplay.getText().toString().equals("")){
                        pd.show();
                        postedca_list.clear();
                        getPostedCa();
                    }

                    else{
                        pd.dismiss();
                        Toast.makeText(getActivity(), "Enter Date Range", Toast.LENGTH_LONG).show();
                        retrieve.setClickable(true);
                    }
                    return true;
                }
                return false;
            }
        });



        return view;

    }

    private void getPostedCa() {
        final String firstdate = startDateDisplay.getText().toString();
        final String lastdate = endDateDisplay.getText().toString();
        StringRequest stringRequest=new StringRequest(Request.Method.POST, POSTED_CA, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response", response);
                if (response.trim().contains("[null]")){
                    pd.dismiss();
                    retrieve.setClickable(true);
                    Toast.makeText(getActivity(), "No Data Available for this Date Range", Toast.LENGTH_LONG).show();
                    SetUpAdapter();
                    postedca_list.clear();
                }
                else {
                    try {
                        JSONArray jsonarray = new JSONArray(response);
                        for (int i = 0; i < jsonarray.length(); i++) {
                            JSONObject jsonObject = jsonarray.getJSONObject(i);
                            CaPostedList list = new CaPostedList();
                            list.setPosted_ca_empNamen(jsonObject.getString("employee_name"));
                            list.setPosted_ca_date(jsonObject.getString("dates_ca"));
                            postedca_list.add(list);
                        }
                        SetUpAdapter();
                        pd.dismiss();
                        retrieve.setClickable(true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        pd.dismiss();
                        retrieve.setClickable(true);
                    }
                }

            }
        },  new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                PromptActivity.showAlert(getActivity(), "error");
                retrieve.setClickable(true);
                pd.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(DATE_FROM, firstdate);
                map.put(DATE_TO, lastdate);
                return map;
            }
        };

        RequestQueue requestQueue= Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);
    }
    private void SetUpAdapter() {
        adapter = new CaPostedAdapter(getActivity(), postedca_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onRefresh() {

    }
}
