package com.suliteos.towaso.user;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ComplainAdapter extends RecyclerView.Adapter<ComplainAdapter.ComplainView> implements EventListener<QuerySnapshot> {

    private final List<Complain> mComplains;
    private final LayoutInflater mInflator;
    private final int mExpandedHalfHeight;
    private final int mCollapsedHeight;
    private final int mExpandedFullHeight;
    private String state,district;
    private final Context mContext;

    public ComplainAdapter(Context context, String state, String district) {
        mContext = context;
        mInflator = LayoutInflater.from(context);
        mComplains = new ArrayList<>();
        Resources r = context.getResources();
        this.state = state;
        this.district = district;
        mCollapsedHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, r.getDisplayMetrics());
        mExpandedFullHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 158, r.getDisplayMetrics());
        mExpandedHalfHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 154, r.getDisplayMetrics());
    }

    @Override
    public ComplainView onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflator.inflate(R.layout.complain_row, parent, false);
        return new ComplainView(view);
    }

    @Override
    public void onBindViewHolder(ComplainView holder, int position) {
        holder.bindToView(mComplains.get(position));
    }

    @Override
    public int getItemCount() {
        return mComplains.size();
    }

    void clear() {
        if (mComplains != null){
            mComplains.clear();
        }
    }

    @Override
    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
        if (e == null) {
            for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                FirebaseFirestore mRootRef = FirebaseFirestore.getInstance();
                mRootRef.collection("Complain").document(state).collection(district).document("Complains").collection("Complain").document(documentSnapshot.getId()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if(e == null) {
                            String key = documentSnapshot.getId();
                            for (Complain complain : mComplains) {
                                if (TextUtils.equals(complain.getKey(), key)) {
                                    return;
                                }
                            }
                            Complain complain = documentSnapshot.toObject(Complain.class);
                            complain.setKey(key);
                            mComplains.add(0, complain);
                            notifyDataSetChanged();
                        }else {
                            Toast.makeText(mContext, e.getLocalizedMessage()+"1", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }else {
            Toast.makeText(mContext, e.getLocalizedMessage()+"0", Toast.LENGTH_SHORT).show();
        }
    }

    public class ComplainView extends RecyclerView.ViewHolder{

        private final TextView mComplainTitleView,mDescView,mComplainTime,mLocation;
        private final View mCard;
        private boolean mToggled;
        private final ImageView mImageView;
        private final View mStatusLayout;

        public ComplainView(View itemView) {
            super(itemView);
            mComplainTitleView = itemView.findViewById(R.id.complain_title);
            mImageView = itemView.findViewById(R.id.home_icon);
            mDescView = itemView.findViewById(R.id.complain_desc);
            mComplainTime = itemView.findViewById(R.id.complain_timing);
            mLocation = itemView.findViewById(R.id.loc);
            mStatusLayout = itemView.findViewById(R.id.status_layout);
            mLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showMap(view);
                }
            });
            mToggled = false;
            mCard = itemView.findViewById(R.id.lyt_container);
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Complain complain = mComplains.get(getAdapterPosition());

                    View imageViewDialog = mInflator.inflate(R.layout.fragment_image, null, false);
                    final ImageView fullImageView = imageViewDialog.findViewById(R.id.house_large);
                    final ProgressBar progressBar = imageViewDialog.findViewById(R.id.progress_bar_image);
                    progressBar.setVisibility(View.VISIBLE);
                    String picLocation = complain.getImageUrl();
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference mStorageRef = storage.getReference().child(picLocation);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    mStorageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            fullImageView.setImageBitmap(bitmap);
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                    Dialog dialog = new AlertDialog.Builder(mInflator.getContext())
                            .setTitle(R.string.house_image)
                            .setView(imageViewDialog)
                            .create();
                    dialog.show();

                }
            });
            mCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    expandRecycler();
                }
            });
        }

        void bindToView(final Complain complain){
            String picLocation = complain.getImageUrl();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference mStorageRef = storage.getReference().child(picLocation);
            final long ONE_MEGABYTE = 1024 * 1024;
            mStorageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    mImageView.setImageBitmap(bitmap);

                }
            });
            mComplainTitleView.setText(complain.getTitle());
            mDescView.setText(complain.getDesc());
            String status = complain.getStatus();
            switch (status) {
                case "red":
                    mStatusLayout.setBackgroundResource(R.drawable.red);
                    break;
                case "yellow":
                    mStatusLayout.setBackgroundResource(R.drawable.yellow);
                    break;
                case "green":
                    mStatusLayout.setBackgroundResource(R.drawable.green);
                    break;
            }
            String lan = String.valueOf(complain.getLoc().getLatitude());
            String lon = String.valueOf(complain.getLoc().getLongitude());
            mLocation.setText(lan + "," + lon);
            mComplainTime.setText(String.valueOf(complain.getTimeStamp()));
        }

        void showMap(View view){
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + mLocation.getText());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(view.getContext().getPackageManager()) != null) {
                view.getContext().startActivity(mapIntent);
            }
        }

        void expandRecycler(){
            mToggled = !mToggled;
            Animation toggleAnimation = new ToggleAnimation(mCard, mToggled, mDescView.getVisibility() == View.VISIBLE);
            toggleAnimation.setDuration(750);
            toggleAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    if (mToggled) {
                        mDescView.setVisibility(View.VISIBLE);
                        mComplainTime.setVisibility(View.VISIBLE);
                        mLocation.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (!mToggled) {
                        mComplainTime.setVisibility(View.GONE);
                        mDescView.setVisibility(View.GONE);
                        mLocation.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mCard.startAnimation(toggleAnimation);
        }
    }

    private class ToggleAnimation extends Animation {
        private final View mView;
        private final int mEndHeight;
        private final int mStartHeight;

        ToggleAnimation(View view, boolean toggled, boolean fullExapand) {
            this.mView = view;
            int expandedHeight = fullExapand ? mExpandedFullHeight : mExpandedHalfHeight;
            mStartHeight = toggled ? mCollapsedHeight : expandedHeight;
            mEndHeight = toggled ? expandedHeight : mCollapsedHeight;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            mView.getLayoutParams().height = (int) ((mEndHeight - mStartHeight) * interpolatedTime) + mStartHeight;
            mView.requestLayout();
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }
}
