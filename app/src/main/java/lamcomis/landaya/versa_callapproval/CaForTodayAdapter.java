package lamcomis.landaya.versa_callapproval;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class CaForTodayAdapter extends RecyclerView.Adapter<CaForTodayAdapter.ViewHolder> {
    Context context;
    private List<CaForTodayList> todayLists;

    public CaForTodayAdapter(Context context, List<CaForTodayList>todayLists){
        this.context = context;
        this.todayLists = todayLists;
    }


    @NonNull
    @Override
    public CaForTodayAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.ca_today_list, parent, false);
        return new CaForTodayAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CaForTodayAdapter.ViewHolder holder, int position) {
        CaForTodayList list = todayLists.get(position);
        holder.today_caEmployee.setText(list.getToday_caEmployee());
        holder.today_caCustomer.setText(list.getToday_caCustomer());
        holder.today_caPurpose.setText(list.getToday_caPurpose());

    }

    @Override
    public int getItemCount() {
        return todayLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView today_caEmployee, today_caCustomer, today_caPurpose;

        public ViewHolder(View itemView) {
            super(itemView);

            today_caEmployee = itemView.findViewById(R.id.today_ca_employee);
            today_caCustomer = itemView.findViewById(R.id.today_ca_customer);
            today_caPurpose = itemView.findViewById(R.id.today_ca_purpose);
        }
    }
}
