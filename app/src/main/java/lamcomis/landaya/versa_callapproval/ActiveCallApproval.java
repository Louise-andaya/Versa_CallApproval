package lamcomis.landaya.versa_callapproval;

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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActiveCallApproval extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    static String url = Variable.link;
    String CA_DATA = "http://" + url + "alldata.php";
    String KEY_USERID = "employee_id";
    SwipeRefreshLayout mSwipeRefreshLayout;
    List<ActiveCAList>callApprovalList;
    private RecyclerView.Adapter adapter;
    SessionManager sessionManager;
    String employee_id;
    RecyclerView rec;

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_active_call_approval, viewGroup, false);
        sessionManager = new SessionManager(getActivity());
        HashMap<String, String> user = sessionManager.getUserDetails();
        employee_id = user.get(SessionManager.KEY_USERID);
        rec = view.findViewById(R.id.recycleViewContainer);
        callApprovalList = new ArrayList<>();
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(ActiveCallApproval.this).attach(ActiveCallApproval.this).commit();
            }
        });

        if(InternetConnection.checkConnection(getActivity())){
             GetCallApproval();
        }
        else {
            PromptActivity.showAlert(getActivity(), "internet");
        }

        return view;

    }

    private void GetCallApproval() {
        mSwipeRefreshLayout.setRefreshing(true);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                CA_DATA, new Response.Listener<String>() {


            public void onResponse(String response) {
                if (response.trim().contains("[null]")) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getActivity(),"You Don't Have A Call Approval", Toast.LENGTH_SHORT).show();

                } else {
                    try {
                        JSONArray jsonarray = new JSONArray(response);
                        for (int i = 0; i < jsonarray.length(); i++) {

                            JSONObject jsonObject = jsonarray.getJSONObject(i);
                            ActiveCAList caList = new ActiveCAList();
                            caList.setCa_date(jsonObject.getString("ca_date"));
                            caList.setCa_no(jsonObject.getString("ca_no"));
                            caList.setCustomer(jsonObject.getString("customer_name"));
                            caList.setPurpose(jsonObject.getString("purpose"));
                            caList.setEmployee_name(jsonObject.getString("employee_name"));
                            caList.setJob_des(jsonObject.getString("job_desc"));
                            callApprovalList.add(caList);
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

    //ADAPTER
    private void SetUpAdapter() {
        adapter = new ActiveCAAdapter(getActivity(), callApprovalList);
        rec.setLayoutManager(new LinearLayoutManager(getActivity()));
        rec.setAdapter(adapter);
    }

    @Override
    public void onRefresh() {

    }
}
