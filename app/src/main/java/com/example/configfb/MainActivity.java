package com.example.configfb;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.configfb.data.adapter.UserAdapter;
import com.example.configfb.data.dao.UserDao;
import com.example.configfb.data.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private UserDao userDao;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        userDao = new UserDao(db);

        btnStart = findViewById(R.id.btnStart);
        recyclerView = findViewById(R.id.rvUser);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userDao.getAll(new OnSuccessListener<List<User>>() {
                    @Override
                    public void onSuccess(List<User> users) {

                        userAdapter = new UserAdapter(users, new OnSuccessListener<User>() {
                            @Override
                            public void onSuccess(User user) {
                                Toast.makeText(MainActivity.this, "User: " + user.getName(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        recyclerView.setAdapter(userAdapter);
                        for (User user : users) {
                            Log.d("MainActivity", "User: " + user.getName());
                        }

                    }
                });
            }
        });


        recyclerView.setLayoutManager(new LinearLayoutManager(this));


//        userDao.insert(new User("John Doe", "john.mckinley@examplepetstore.com", ""), new OnSuccessListener<String>() {
//            @Override
//            public void onSuccess(String s) {
//
//            }
//        });
//
//        // Register a photo picker activity launcher for a single image
//        ActivityResultLauncher<PickVisualMediaRequest> pickSingleMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
//            if (uri != null) {
//                Log.d("PhotoPicker", "Selected URI: " + uri);
//
//                userDao.updateImage("fftSM3MeLLRtyTpkK9l0", uri, new OnSuccessListener<Boolean>() {
//                    @Override
//                    public void onSuccess(Boolean aBoolean) {
//
//                    }
//                });
//            } else {
//                Log.d("PhotoPicker", "No media selected");
//            }
//        });
//
//        // Launch the photo picker when needed (e.g., button click)
//        pickSingleMedia.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.terminate();
        db.clearPersistence();
    }
}