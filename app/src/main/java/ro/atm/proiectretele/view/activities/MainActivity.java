package ro.atm.proiectretele.view.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import ro.atm.proiectretele.R;
import ro.atm.proiectretele.adapters.UserAdapter;
import ro.atm.proiectretele.data.CloudFirestoreRepository;
import ro.atm.proiectretele.data.Constants;
import ro.atm.proiectretele.data.firestore_models.UserModel;
import ro.atm.proiectretele.databinding.ActivityMainBinding;
import ro.atm.proiectretele.utils.app.login.LogedInUser;
import ro.atm.proiectretele.utils.webrtc.SignallingClient;
import ro.atm.proiectretele.viewmodel.MainActivityViewModel;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    //// MEMBERS
    private ActivityMainBinding binding;
    private MainActivityViewModel mViewModel;

    private UserAdapter mAdapter;

    private FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();
    private DocumentReference noteRef = mDatabase.document("user-com/sig-client");
    private CollectionReference onlineUsersReference = mDatabase.collection(Constants.COLLECTION_ONLINE_USERS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        Query query = onlineUsersReference.orderBy("email", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class)
                .build();

        mAdapter = new UserAdapter(options);
        // RecyclerView


        binding.activityMainUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.activityMainUsers.setAdapter(mAdapter);

        Testing();

        mAdapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                UserModel userModel = documentSnapshot.toObject(UserModel.class);
                String id = documentSnapshot.getId();
                String path = documentSnapshot.getReference().getPath();
                Toast.makeText(MainActivity.this, "POS: " + position + " ID: " + id, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.startListening(); // cand aplicatia revine din backgroud, adaptorul incepe sa asculte din nou
        noteRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null)
                    return;

                if(documentSnapshot.exists()){
                    String str = documentSnapshot.getString("messagess"); //null if not exist
                    binding.activityMainEt.setText(str);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening(); // cand aplicatia merge in background nu mai asculta nimic pentru ca ar irosi resurse
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_main_activity_logout:
                this.onBackPressed();
                return true;
            default: return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onBackPressed() {
        LogedInUser logedInUser = LogedInUser.getInstance();
        if(logedInUser != null){
            CloudFirestoreRepository.create().onUserSignOut(new UserModel(logedInUser));
        }
        super.onBackPressed();
    }

    //// METHODS
    public void Testing(){
        FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_ONLINE_USERS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> myListOfDocuments = task.getResult().getDocuments();
                            for(DocumentSnapshot documentSnapshot : myListOfDocuments){
                                binding.activityMainEt.setText(binding.activityMainEt.getText().toString() + documentSnapshot.getString("email"));
                            }
                        }

                    }
                });
    }

    public void onButton(View view){


        SignallingClient signallingClient = SignallingClient.getInstance();
        signallingClient.emitMessage("mesajul trimis!");

        Intent intent = new Intent(MainActivity.this, MediaStreamActivity.class);
        startActivity(intent);
    }
}
