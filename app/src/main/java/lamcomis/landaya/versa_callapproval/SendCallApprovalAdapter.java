package lamcomis.landaya.versa_callapproval;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SendCallApprovalAdapter extends RecyclerView.Adapter<SendCallApprovalAdapter.ViewHolder> {

    public Context context;
    List<SendCallApprovalList>sendCAList;

    public SendCallApprovalAdapter(Context context, List<SendCallApprovalList> sendCAList){
        this.context = context;
        this.sendCAList = sendCAList;

    }
    @Override
    public SendCallApprovalAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.ca_send_list, parent, false);
        return new SendCallApprovalAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SendCallApprovalAdapter.ViewHolder holder, int position) {
        SendCallApprovalList scaList = sendCAList.get(position);
        holder.send_ca_date.setText(scaList.getSend_ca_date());
        holder.send_ca_no.setText(scaList.getSend_ca_no());
        holder.send_customer.setText(StringUtils.capitalize(scaList.getSend_customer().toLowerCase().trim()));
        holder.send_purpose.setText(StringUtils.capitalize(scaList.getSend_purpose().toLowerCase().trim()));
        holder.bind(sendCAList.get(position));

    }

    @Override
    public int getItemCount() {
        return sendCAList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView send_ca_date, send_ca_no, send_customer, send_purpose;
        ImageView img_check;
        public ViewHolder(View itemView) {
            super(itemView);
            send_ca_date = (TextView)itemView.findViewById(R.id.send_ca_date);
            send_ca_no = (TextView)itemView.findViewById(R.id.send_ca_no);
            send_customer = (TextView)itemView.findViewById(R.id.send_customer);
            send_purpose = (TextView)itemView.findViewById(R.id.send_purpose);
            send_purpose = (TextView)itemView.findViewById(R.id.send_purpose);
            img_check = (ImageView) itemView.findViewById(R.id.btn_check);
        }
        void bind(final SendCallApprovalList ca_no){
            img_check.setVisibility(ca_no.isChecked() ? View.VISIBLE : View.GONE);


                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(SendCallApproval.btn_send.getVisibility() == View.GONE){
                           Toast.makeText(context, "Finish all Your Call Approval First", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            ca_no.setChecked(!ca_no.isChecked());
                            img_check.setVisibility(ca_no.isChecked() ? View.VISIBLE : View.GONE);
                        }
                    }
                });

        }
    }
    public List<SendCallApprovalList> getAll(){
        return sendCAList;
    }

    public List<SendCallApprovalList> getSelected() {
        ArrayList<SendCallApprovalList> selected = new ArrayList<>();
        for (int i = 0; i < sendCAList.size(); i++) {
            if (sendCAList.get(i).isChecked()) {
                selected.add(sendCAList.get(i));
            }
        }
        return selected;
    }
}
