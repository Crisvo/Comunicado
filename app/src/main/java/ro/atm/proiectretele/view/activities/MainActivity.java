package ro.atm.proiectretele.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.webrtc.PeerConnection;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ro.atm.proiectretele.R;
import ro.atm.proiectretele.adapters.UserAdapter;
import ro.atm.proiectretele.data.CloudFirestoreRepository;
import ro.atm.proiectretele.data.Constants;
import ro.atm.proiectretele.data.firestore_models.UserModel;
import ro.atm.proiectretele.databinding.ActivityMainBinding;
import ro.atm.proiectretele.utils.app.login.LogedInUser;
import ro.atm.proiectretele.utils.webrtc.IceServer;
import ro.atm.proiectretele.utils.webrtc.TurnServerPojo;
import ro.atm.proiectretele.utils.webrtc.Utils;
import ro.atm.proiectretele.viewmodel.MainActivityViewModel;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    //// MEMBERS
    private ActivityMainBinding binding;
    private MainActivityViewModel mViewModel;

    private UserAdapter mAdapter;

    private FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();
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

        mAdapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                UserModel userModel = documentSnapshot.toObject(UserModel.class);
                String id = documentSnapshot.getId();
                String path = documentSnapshot.getReference().getPath();
                binding.activityMainSelectedUser.setText(userModel.getEmail());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //SignallingClientFirestore.getInstance().doRefresh();
        mAdapter.startListening(); // cand aplicatia revine din backgroud, adaptorul incepe sa asculte din nou

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
        switch (item.getItemId()) {
            case R.id.menu_main_activity_logout:
                this.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onBackPressed() {
        LogedInUser logedInUser = LogedInUser.getInstance();
        if (logedInUser != null) {
            CloudFirestoreRepository.create().onUserSignOut(new UserModel(logedInUser));
        }
        super.onBackPressed();
    }

    public void onCallButtonClicked(View view) {
        //set noteReference
        if (binding.activityMainSelectedUser.getText().toString().equals("Please select a user!")) {
            return;
        }
        // set collection where i send data

        Intent intent = new Intent(MainActivity.this, MediaStreamActivity.class);
        startActivity(intent);
    }
}
