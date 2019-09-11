package lamcomis.landaya.versa_callapproval;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
//Louise

public class ActiveCAAdapter extends  RecyclerView.Adapter<ActiveCAAdapter.ViewHolder>{
    SQLiteDatabase db;
    public Context context;
    private List<ActiveCAList> list;
;


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

        public ViewHolder(View itemView) {
            super(itemView);
            ca_date = (TextView) itemView.findViewById(R.id.active_ca_date);
            ca_no = (TextView) itemView.findViewById(R.id.active_ca_no);
            customer = (TextView) itemView.findViewById(R.id.active_customer);
            purpose = (TextView) itemView.findViewById(R.id.active_purpose);

        }

    }

}