package ro.atm.proiectretele.adapters;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import ro.atm.proiectretele.R;
import ro.atm.proiectretele.data.firestore_models.MessageModel;
import ro.atm.proiectretele.utils.app.login.LogedInUser;

public class MessagesAdapter extends FirestoreRecyclerAdapter<MessageModel, MessagesAdapter.MessageViewHolder> {
    public String partener;
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public MessagesAdapter(@NonNull FirestoreRecyclerOptions<MessageModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull MessageModel model) {
        if(model.getFrom().equals(LogedInUser.getInstance().getEmail())){ //me
            holder.mFrom.setText(model.getFrom());
            holder.mMessage.setText(model.getMessage());
            holder.mFrom.setGravity(Gravity.RIGHT);
            holder.mMessage.setGravity(Gravity.RIGHT);
            return;
        }
        if(model.getFrom().equals(partener)){
            holder.mFrom.setText(model.getFrom());
            holder.mMessage.setText(model.getMessage());
            holder.mFrom.setGravity(Gravity.LEFT);
            holder.mMessage.setGravity(Gravity.LEFT);
            return;
        }

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_messages, parent, false);
        return new MessagesAdapter.MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        private TextView mFrom;
        private TextView mMessage;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            mFrom = itemView.findViewById(R.id.list_messages_from);
            mMessage = itemView.findViewById(R.id.list_messages_message);
        }
    }
}
