package ro.atm.proiectretele.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.os.Message;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import ro.atm.proiectretele.R;
import ro.atm.proiectretele.adapters.MessagesAdapter;
import ro.atm.proiectretele.adapters.UserAdapter;
import ro.atm.proiectretele.data.CloudFirestoreRepository;
import ro.atm.proiectretele.data.firestore_models.MessageModel;
import ro.atm.proiectretele.databinding.ActivityMessengerBinding;
import ro.atm.proiectretele.utils.app.login.LogedInUser;

public class MessengerActivity extends AppCompatActivity {
    private ActivityMessengerBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private MessagesAdapter mAdapter;
    private String senderEmail;

    private CollectionReference inboxRef = FirebaseFirestore.getInstance()
            .collection(LogedInUser.getInstance().getEmail());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_messenger);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_messenger);

        senderEmail = "default";
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                senderEmail = null;
            } else {
                senderEmail = extras.getString("init");
            }
        } else {
            senderEmail = (String) savedInstanceState.getSerializable("init");
        }

        List<String> list = new LinkedList<>();
        list.add(senderEmail);
        list.add(LogedInUser.getInstance().getEmail());

        Query query = inboxRef.orderBy("timestamp", Query.Direction.ASCENDING).whereIn("from", list);

        FirestoreRecyclerOptions<MessageModel> options = new FirestoreRecyclerOptions.Builder<MessageModel>()
                .setQuery(query, MessageModel.class)
                .build();

        mAdapter = new MessagesAdapter(options);
        mAdapter.partener = senderEmail;

        binding.messages.setLayoutManager(new LinearLayoutManager(this));
        binding.messages.setAdapter(mAdapter);

    }

    @Override
    protected void onStop() {
        mAdapter.stopListening();
        super.onStop();
    }

    @Override
    protected void onStart() {
        mAdapter.startListening();
        super.onStart();
    }

    public void onSendClicked(View view) {
        MessageModel message = new MessageModel(LogedInUser.getInstance().getEmail(), binding.activityMessengerMessage.getText().toString());
        Map<String, Object> map = new HashMap<>();
        map.put("from", message.getFrom());
        map.put("message", message.getMessage());
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        map.put("timestamp", ts);
        FirebaseFirestore.getInstance().collection(senderEmail).add(map)
                .addOnSuccessListener(documentReference -> Toast.makeText(MessengerActivity.this, "Message send", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(MessengerActivity.this, "Message not send", Toast.LENGTH_SHORT).show());
        FirebaseFirestore.getInstance().collection(LogedInUser.getInstance().getEmail()).add(map);

        binding.activityMessengerMessage.setText("");
    }

}
