package soa.work.scheduler;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static soa.work.scheduler.Constants.CURRENTLY_AVAILABLE_WORKS;
import static soa.work.scheduler.Constants.WORK_ASSIGNED_AT;
import static soa.work.scheduler.Constants.WORK_ASSIGNED_TO;

public class WorkAcceptedActivity extends AppCompatActivity {

    @BindView(R.id.work_accept)
    Button workaccept;

    private FirebaseUser currentUser;
    private String created_date, work_posted_by_account_id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.work_details);

        ButterKnife.bind(this);
        workaccept.setOnClickListener(view -> new AlertDialog.Builder(WorkAcceptedActivity.this)
                .setMessage("Are you sure want to accept?")
                .setCancelable(true)
                .setPositiveButton("YES", (dialog, which) -> {
                    Toast.makeText(WorkAcceptedActivity.this, "Accepted", Toast.LENGTH_SHORT).show();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    Bundle bundle = getIntent().getExtras();
                    if (bundle != null) {
                        created_date = (String) bundle.get("created_date");
                        work_posted_by_account_id = (String) bundle.get("work_posted_by_account_id");
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault());
                    String currentDateAndTime = sdf.format(new Date());
                    DatabaseReference currently_available = database.getReference(CURRENTLY_AVAILABLE_WORKS).child(work_posted_by_account_id + "-" + created_date);
                    currently_available.child(WORK_ASSIGNED_TO).setValue(currentUser.getDisplayName());
                    currently_available.child(WORK_ASSIGNED_AT).setValue(currentDateAndTime);
                })
                .setNegativeButton("NO", (dialog, which) -> dialog.dismiss()).create().show());
    }
}
