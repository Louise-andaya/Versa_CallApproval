package lamcomis.landaya.versa_callapproval;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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

import java.util.HashMap;
import java.util.Map;

public class AdminDashboard extends AppCompatActivity {
    public static Activity context;
    DatabaseHelper myDb;
    SessionManager sessionManager;
    private TabsAdapter tabadapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    String user_id, user_type;
    ImageView logout, changePass;
    EditText newPassword, confirmPassword;
    TextView employee;
    String EMP_ID = "employee_id";
    String EMP_PASSWORD = "password";
    String CHANGE_PASSWORD = "http://"+Variable.link+"changePass.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        myDb = new DatabaseHelper(this);
        sessionManager = new SessionManager(getApplicationContext());
        HashMap<String, String> user = sessionManager.getUserDetails();
        user_id = user.get(SessionManager.KEY_USERID);
        user_type = user.get(SessionManager.RESPONSE);

        viewPager = (ViewPager) findViewById(R.id.admin_view_pager);
        tabLayout = (TabLayout) findViewById(R.id.admin_tab_layout);
        tabadapter = new TabsAdapter(getSupportFragmentManager());
        tabadapter.addFragment(new CaForToday(), "CA For Today");
        tabadapter.addFragment(new CaForApproval(), "For Approval CA");
        tabadapter.addFragment(new CaPosted(), "Posted CA");
        viewPager.setAdapter(tabadapter);
        tabLayout.setupWithViewPager(viewPager);

        logout = (ImageView)findViewById(R.id.admin_logout);
        logout.setClickable(true);
        logout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    logout.setClickable(false);
                    PromptActivity.showAlert(AdminDashboard.this, "logout");
                }

                return false;
            }
        });

        changePass = (ImageView) findViewById(R.id.changePass);
        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertChangePassword();
            }
        });
    }

    //ALERT CHANGE PASS
    private void AlertChangePassword() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.change_password, null);
        dialogBuilder.setView(dialogView);
        employee = (TextView) dialogView.findViewById(R.id.employee_id);
        employee.setText(user_id);
        newPassword = (EditText) dialogView.findViewById(R.id.newPass);
        confirmPassword = (EditText) dialogView.findViewById(R.id.confirmPass);


        dialogBuilder.setTitle("Change Password");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                if(!newPassword.getText().toString().equals("")&&!confirmPassword.getText().toString().equals("")){

                    if(newPassword.getText().toString().equals(confirmPassword.getText().toString()) ){
                        ChangePassword();
                    }
                    else{
                        PromptActivity.showAlert(AdminDashboard.this, "dont_match");
                    }
                }
                else{
                    PromptActivity.showAlert(AdminDashboard.this, "empty");
                }

            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    //CHANGE PASSWORD
    private void ChangePassword() {
        final String newpass = newPassword.getText().toString();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, CHANGE_PASSWORD, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if (response.trim().contains("success")) {
                    PromptActivity.showAlert(AdminDashboard.this, "success_changepass");

                }

                else {
                    Toast.makeText(AdminDashboard.this, response, Toast.LENGTH_LONG).show();

                }
            }

        },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        PromptActivity.showAlert(AdminDashboard.this, "error");

                    }
                }) {


            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();

                map.put(EMP_ID, user_id);
                map.put(EMP_PASSWORD, newpass);

                return map;


            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);



    }


    @Override
    public void onBackPressed() {
        PromptActivity.showAlert(AdminDashboard.this, "back");
    }
}
