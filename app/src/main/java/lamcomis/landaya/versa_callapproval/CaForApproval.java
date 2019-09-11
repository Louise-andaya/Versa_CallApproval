package lamcomis.landaya.versa_callapproval;

import android.app.ProgressDialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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
import java.util.List;

public class CaForApproval extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    String url = Variable.link;
    String FA_DATA = "http://"+url+"fa_data.php";
    SessionManager sessionManager;
    List<CaForApprovalList>ca_faList;
    List<CaForApprovalList>filter;
    RecyclerView recyclerView;
    EditText txt_search;
    ProgressDialog pd;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView.Adapter adapter;
    int textlength = 0;


    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_ca_for_approval, viewGroup, false);
        pd = new ProgressDialog(getActivity());
        pd.setMessage("Please Wait...");
        sessionManager = new SessionManager(getActivity());
        recyclerView = view.findViewById(R.id.ca_recycleViewContainer);
        txt_search = view.findViewById(R.id.ca_fa_searchTrans);
        ca_faList = new ArrayList<>();
        filter = new ArrayList<>();
        swipeRefreshLayout = view.findViewById(R.id.ca_swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(CaForApproval.this).attach(CaForApproval.this).commit();
            }
        });
        
        if(InternetConnection.checkConnection(getActivity())){
            getForApproval();
        }
        else{
            PromptActivity.showAlert(getActivity(), "internet");

        }
        txt_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textlength = txt_search.getText().length();
                filter.clear();

                for (int i = 0; i < ca_faList.size(); i++) {
                    if (textlength <= ca_faList.get(i).getFa_caEmployee().length()) {
//                            Log.d("ertyyy", list_date.get(i).getD_name().toLowerCase().trim());
                        if (ca_faList.get(i).getFa_caEmployee().toLowerCase().trim().contains(
                                txt_search.getText().toString().toLowerCase().trim())) {
                            filter.add(ca_faList.get(i));
                        }
                    }
                }
                adapter = new CaForApprovalAdapter(getActivity(), filter);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        return view;
    }

    private void getForApproval() {
        swipeRefreshLayout.setRefreshing(true);
        pd.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                FA_DATA, new Response.Listener<String>() {


            public void onResponse(String response) {
                try {
                    JSONArray jsonarray = new JSONArray(response);
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonObject = jsonarray.getJSONObject(i);
                        CaForApprovalList list = new CaForApprovalList();
                        list.setFa_caDate(jsonObject.getString("ca_date"));
                        list.setFa_caEmployee(jsonObject.getString("employee_name"));
                        list.setFa_caCustomer(jsonObject.getString("customer"));
                        list.setFa_caNo(jsonObject.getString("ca_no"));
                        list.setFa_caPurpose(jsonObject.getString("purpose"));
                        ca_faList.add(list);
                    }

                    SetUpAdapter();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                swipeRefreshLayout.setRefreshing(false);
                pd.dismiss();
            }




        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        swipeRefreshLayout.setRefreshing(false);
                        PromptActivity.showAlert(getActivity(), "error");
                        pd.dismiss();
                    }
                }) {

        };

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);
    }

    private void SetUpAdapter() {
        adapter = new CaForApprovalAdapter(getActivity(), ca_faList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onRefresh() {

    }
}
