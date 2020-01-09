package ro.atm.proiectretele.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import ro.atm.proiectretele.R;
import ro.atm.proiectretele.data.firestore_models.UserModel;

public class UserAdapter extends FirestoreRecyclerAdapter<UserModel, UserAdapter.UserViewHolder> {
    private OnItemClickListener mListener;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public UserAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull UserModel model) {
        holder.mUserId.setText(model.getUid());
        holder.mUserEmail.setText(model.getEmail());
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_users, parent, false);
        return new UserViewHolder(v);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }
    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        //// MEMBERS
        private TextView mUserId;
        private TextView mUserEmail;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            mUserEmail = itemView.findViewById(R.id.list_users_userEmail);
            mUserId = itemView.findViewById(R.id.list_users_userID);

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && mListener != null){
                        mListener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }

    }
}
