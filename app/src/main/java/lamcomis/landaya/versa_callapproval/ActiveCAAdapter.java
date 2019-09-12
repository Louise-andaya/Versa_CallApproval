package lamcomis.landaya.versa_callapproval;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActiveCAAdapter extends  RecyclerView.Adapter<ActiveCAAdapter.ViewHolder>{
    SQLiteDatabase db;
    public static Context context;
    private List<ActiveCAList> list;
    static String DELETE = "http://"+Variable.link+"delete.php";
    static String CA_NO = "ca_no";
    static String CUSTOMER = "customer";


    public ActiveCAAdapter(Context context, List<ActiveCAList> list) {
        this.context = context;
        this.list = list;

    }

    @Override
    public ActiveCAAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.ca_active_list, parent, false);
        return new ActiveCAAdapter.ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final ActiveCAList dlist = list.get(position);

        holder.ca_date.setText(dlist.getCa_date());
        holder.ca_no.setText(dlist.getCa_no());
        holder.customer.setText(StringUtils.capitalize(dlist.getCustomer().toLowerCase().trim()));
        holder.purpose.setText(StringUtils.capitalize(dlist.getPurpose().toLowerCase().trim()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ca_no = list.get(position).getCa_no().toString();
                String customer_name = list.get(position).getCustomer().toString();
                String purpose = list.get(position).getPurpose().toString();
                String ca_date = list.get(position).getCa_date();
                String employee_name = list.get(position).getEmployee_name();
                String job_desc = list.get(position).getJob_des();
                Intent intent = new Intent(holder.itemView.getContext(), CallApprovalActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                intent.putExtra("ca_no", ca_no);
                intent.putExtra("customer_name", customer_name);
                intent.putExtra("purpose", purpose);
                intent.putExtra("ca_date", ca_date);
                intent.putExtra("employee_name", employee_name);
                intent.putExtra("job_desc", job_desc);
                holder.itemView.getContext().startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView ca_date, ca_no, customer, purpose;
        Button delete;
        public ViewHolder(View itemView) {
            super(itemView);
            ca_date = (TextView) itemView.findViewById(R.id.active_ca_date);
            ca_no = (TextView) itemView.findViewById(R.id.active_ca_no);
            customer = (TextView) itemView.findViewById(R.id.active_customer);
            purpose = (TextView) itemView.findViewById(R.id.active_purpose);
            delete = (Button) itemView.findViewById(R.id.delete);
            delete.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                        delete.setClickable(false);
                        AlertDelete();
                        return true;
                    }
                    return false;
                }
            });
        }

        private void AlertDelete() {
            delete.setClickable(true);
            final String ca_number = ca_no.getText().toString();
            final String customer_name = customer.getText().toString();
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle("Alert");
            alertDialog.setCancelable(false);
            alertDialog.setMessage("Are you sure you want to delete this data? ");
            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(context, ca_number, Toast.LENGTH_SHORT).show();
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, DELETE, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (response.trim().contains("success")) {
                                //PromptActivity.showAlert(co, "success_changepass");
                                Toast.makeText(context, "deleted success", Toast.LENGTH_SHORT).show();

                            }

                            else {
                                Toast.makeText(context, response, Toast.LENGTH_LONG).show();

                            }
                        }

                    },

                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    PromptActivity.showAlert(context, "error");
                                    Log.d("error", String.valueOf(error));

                                }
                            }) {


                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> map = new HashMap<String, String>();

                            map.put(CA_NO, ca_number);
                            map.put(CUSTOMER, customer_name);

                            return map;


                        }
                    };


                    RequestQueue requestQueue = Volley.newRequestQueue(context);
                    requestQueue.add(stringRequest);

                }
            });
            alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    // DO SOMETHING HERE
                }
            });

            AlertDialog dialog = alertDialog.create();
            dialog.show();
        }

    }

}