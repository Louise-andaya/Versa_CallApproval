package lamcomis.landaya.versa_callapproval;

import android.app.ProgressDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class SendCallApproval extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    String url = Variable.link;
    String SAVE_CA = "http://" + url + "save_callapproval.php";
    String UPDATE_CA = "http://" + url + "update_ca.php";
    public static final String SENDFORAP = "https://hris.versatech.com.ph/api/mobileSendApprovalCA";
    String CA_DATA = "http://" + url + "alldata.php";
    String KEY_USERID = "employee_id";
    String KEY_STATUS = "status";
    String CA_NO = "ca_no";
    String EMPLOYEE_ID = "employee_id";
    SessionManager sessionManager;
    SwipeRefreshLayout mSwipeRefreshLayout;
    String employee_id;
    public static FloatingActionButton btn_send;
    List<SendCallApprovalList>sendCAList;
    RecyclerView rec;
    SendCallApprovalAdapter adapter;
    handleSSLHandshakeActivity hsh = new handleSSLHandshakeActivity();
    ProgressDialog pd;

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_send_call_approval, viewGroup, false);
        hsh.enable();
        pd = new ProgressDialog(getActivity());
        pd.setMessage("Sending, Please Wait...");
        pd.setCancelable(false);
        sessionManager = new SessionManager(getActivity());
        HashMap<String, String> user = sessionManager.getUserDetails();
        employee_id = user.get(SessionManager.KEY_USERID);
        rec = view.findViewById(R.id.recycleViewContainer);
        sendCAList = new ArrayList<>();
        mSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(SendCallApproval.this).attach(SendCallApproval.this).commit();
            }
        });
        btn_send = (FloatingActionButton)view.findViewById(R.id.btn_send);
        btn_send.setVisibility(View.GONE);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendCAList.isEmpty()){
                    Toast.makeText(getActivity(), "No Pending Call Approval", Toast.LENGTH_SHORT).show();
                }
                else {
                        getSelectedCA();
                }
            }
        });


        if(InternetConnection.checkConnection(getActivity())){
            getSendForApproval();
            getCallApproval();
        }
        else{
            PromptActivity.showAlert(getActivity(), "internet");
            mSwipeRefreshLayout.setRefreshing(false);
        }
        return view;
    }

    private void getCallApproval() {
        mSwipeRefreshLayout.setRefreshing(true);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                CA_DATA, new Response.Listener<String>() {


            public void onResponse(String response) {
                if (response.trim().contains("[null]")) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    btn_send.setVisibility(View.VISIBLE);

                }
                else {
                    btn_send.setVisibility(View.GONE);
                }
            }


        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        PromptActivity.showAlert(getActivity(), "error");
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(KEY_USERID, employee_id);
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);
    }

    private void getSendForApproval() {
        mSwipeRefreshLayout.setRefreshing(true);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                SAVE_CA, new Response.Listener<String>() {

            public void onResponse(String response) {
                Log.d("string", response);
                if(response.contains("[null]")){
                    mSwipeRefreshLayout.setRefreshing(false);


                }else {
                    try {
                    JSONArray jsonarray = new JSONArray(response);
                    for (int i = 0; i < jsonarray.length(); i++) {

                        JSONObject jsonObject = jsonarray.getJSONObject(i);
                        SendCallApprovalList scalist = new SendCallApprovalList();
                        scalist.setSend_ca_date(jsonObject.getString("dates_ca"));
                        scalist.setSend_ca_no(jsonObject.getString("ca_no"));
                        scalist.setSend_customer(jsonObject.getString("customer_name"));
                        scalist.setSend_purpose(jsonObject.getString("purpose"));
                        sendCAList.add(scalist);
                    }
                    Log.d("string", String.valueOf(jsonarray.length()));

                    SetUpAdapter();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSwipeRefreshLayout.setRefreshing(false);
                }
            }



        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        PromptActivity.showAlert(getActivity(), "error");

                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(EMPLOYEE_ID, employee_id);
                //map.put(KEY_DATE, thisDate);
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);

    }

    private void SetUpAdapter() {
        adapter = new SendCallApprovalAdapter(getActivity(), sendCAList);
        rec.setLayoutManager(new LinearLayoutManager(getActivity()));
        rec.setAdapter(adapter);
    }

    private void getSelectedCA() {
        if (adapter.getSelected().size() > 0) {
            ArrayList<String> selected_ca = new ArrayList<>();
            for (int i = 0; i < adapter.getSelected().size(); i++) {
                selected_ca.add(adapter.getSelected().get(i).getSend_ca_no());
            }
            HashSet hs = new HashSet();
            hs.addAll(selected_ca);
            selected_ca.clear();
            selected_ca.addAll(hs);
            sendCA(hs);

        } else {
            showToast("Please Select Call Approval to be Send");
        }
    }

    private void sendCA(HashSet selected) {
        pd.show();

        final String ca_no = selected.toString().replace("[", "").replace("]", "");

            Log.d("Selected", ca_no);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, SENDFORAP, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response", response);
                if (response.contains("success")) {
                    UpdateStatus(ca_no);
                    pd.dismiss();
                }
                else{
                    PromptActivity.showAlert(getActivity(), "error");
                    pd.dismiss();
                }

            }


        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        PromptActivity.showAlert(getActivity(), "error");
                        pd.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(CA_NO, ca_no);
                map.put(EMPLOYEE_ID, employee_id);

                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        int socketTimeout = Variable.global_timeout;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        requestQueue.add(stringRequest);

    }

    private void UpdateStatus(final String ca_no) {
        final String status = "FA";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPDATE_CA, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if (response.trim().contains("success")) {
                    PromptActivity.showAlert(getActivity(), "success");
                    pd.dismiss();

                }
                else{
                    PromptActivity.showAlert(getActivity(), "error");
                    pd.dismiss();
                }

            }

        },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        PromptActivity.showAlert(getActivity(), "error");
                        pd.dismiss();
                    }
                }) {


            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();

                map.put(CA_NO, ca_no);
                map.put(KEY_STATUS, status);

                return map;


            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        int socketTimeout = Variable.global_timeout;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        requestQueue.add(stringRequest);
    }

    private void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();

    }
    @Override
    public void onRefresh() {

    }
}
