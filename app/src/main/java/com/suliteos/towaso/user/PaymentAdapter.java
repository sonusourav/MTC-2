package com.suliteos.towaso.user;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentView> implements EventListener<QuerySnapshot> {

    private final List<Payment> mStatus;
    private final LayoutInflater mInflater;
    private final Context mContext;

    public PaymentAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mStatus = new ArrayList<>();
    }

    @Override
    public PaymentAdapter.PaymentView onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_view_fragment, parent, false);
        return new PaymentView(view);
    }

    @Override
    public void onBindViewHolder(PaymentAdapter.PaymentView holder, int position) {
        holder.bindToView(mStatus.get(position));
    }

    public void clear() {
        if (mStatus != null) {
            mStatus.clear();
        }
    }

    @Override
    public int getItemCount() {
        return mStatus.size();
    }

    @Override
    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
        if (e == null) {
            for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                String key = documentSnapshot.getId();
                Payment pw = documentSnapshot.toObject(Payment.class);
                pw.setKey(key);
                mStatus.add(0, pw);
                notifyDataSetChanged();
            }
        }else {
            Toast.makeText(mContext, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    class PaymentView extends RecyclerView.ViewHolder{

        private final TextView mMonthView,mStatusView,mTimeView;

        PaymentView(View itemView) {
            super(itemView);
            mMonthView = itemView.findViewById(R.id.payment_month);
            mStatusView = itemView.findViewById(R.id.payment_status);
            mTimeView = itemView.findViewById(R.id.payment_time);
        }

        void bindToView(final Payment payment){
            String monthYear = payment.getMonth();
            String month = monthYear.substring(0,1);
            String year = monthYear.substring(2);
            mMonthView.setText(monthYear);
            if (payment.getIsPaid().equals("1")){
                mStatusView.setText(R.string.paid);
            }else{
                mStatusView.setText(R.string.unPaid);
            }
            mTimeView.setText(String.valueOf(payment.getTime()));
        }

        private String getMonthName(String s){
            switch (s){
                case "01":
                    return "January";
                case "02":
                    return "February";
                case "03":
                    return "March";
                case "04":
                    return "April";
                case "05":
                    return "May";
                case "06":
                    return "June";
                case "07":
                    return "July";
                case "08":
                    return "August";
                case "09":
                    return "September";
                case "10":
                    return "October";
                case "11":
                    return "November";
                case "12":
                    return "December";
                default:
                    return null;
            }
        }
    }
}
