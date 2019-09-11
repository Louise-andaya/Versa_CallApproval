package lamcomis.landaya.versa_callapproval;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    static String url = Variable.link;
    String LOGIN_URL = "http://"+url+"login.php";
    String KEY_USERID = "user_id";
    String KEY_PASSWORD = "password";
    ProgressBar loading;
    Button login;
    SessionManager sessionManager;
    DatabaseHelper db;

    EditText user_id, password;
    String user_name, user_password;
    PromptActivity promptActivity = new PromptActivity();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sessionManager = new SessionManager(this);
        db = new DatabaseHelper(this);

        loading = (ProgressBar)findViewById(R.id.loading);
        loading.setVisibility(View.GONE);

        user_id = (EditText)findViewById(R.id.userID);
        password = (EditText)findViewById(R.id.password);

        login = (Button)findViewById(R.id.login);
        login.setClickable(true);


        if(sessionManager.isLoggedIn()){
            HashMap<String, String> user = sessionManager.getUserDetails();
            String user_type = user.get("user_type");
            if(user_type.equals("user")){
                Intent i = new Intent(Login.this, UserDashBoard.class);
                startActivity(i);
            }
            else{
                Intent i = new Intent(Login.this, AdminDashboard.class);
                startActivity(i);
            }
        }
        else {
            login.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        loading.setVisibility(View.VISIBLE);
                        login.setClickable(false);
                        if (InternetConnection.checkConnection(getApplicationContext())) {
                            ValidateLogin();
                        } else {
                            PromptActivity.showAlert(Login.this, "internet");
                            loading.setVisibility(View.GONE);
                            login.setClickable(true);
                        }

                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private void ValidateLogin() {
        user_name = user_id.getText().toString().toUpperCase().trim();
        user_password = password.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response.trim().contains("user")) {
                            String user_type = "user";
                            sessionManager.createLoginSession(user_name, user_password, user_type);
                            Intent i = new Intent(Login.this, UserDashBoard.class);
                            startActivity(i);
                            finish();
                        }
                        else if (response.trim().contains("admin")) {
                            String user_type = "admin";
                            sessionManager.createLoginSession(user_name, user_password, user_type);
                            Intent i = new Intent(Login.this, AdminDashboard.class);
                            startActivity(i);

                        }
                        else if (response.trim().contains("invalid_password")) {
                            loading.setVisibility(View.GONE);
                            login.setClickable(true);
                            PromptActivity.showAlert(Login.this, "invalid_password");
                        } else if (response.trim().contains("user_empty")) {
                            loading.setVisibility(View.GONE);
                            login.setClickable(true);
                            PromptActivity.showAlert(Login.this, "empty");
                        } else if (response.trim().contains("not_found")) {
                            loading.setVisibility(View.GONE);
                            login.setClickable(true);
                            PromptActivity.showAlert(Login.this, "not_found");
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.setVisibility(View.GONE);
                        login.setClickable(true);
                        PromptActivity.showAlert(Login.this, "error");
                        Log.d("error", error.toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(KEY_USERID, user_name);
                map.put(KEY_PASSWORD, user_password);
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);


    }

    @Override
    public void onBackPressed() {
        PromptActivity.showAlert(Login.this, "back");
    }
}
