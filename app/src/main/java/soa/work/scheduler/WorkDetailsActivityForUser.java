package soa.work.scheduler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;

import static soa.work.scheduler.Constants.CURRENTLY_AVAILABLE_WORKS;
import static soa.work.scheduler.Constants.PHONE_NUMBER;
import static soa.work.scheduler.Constants.USER_ACCOUNTS;
import static soa.work.scheduler.Constants.WORK_ASSIGNED_AT;
import static soa.work.scheduler.Constants.WORK_ASSIGNED_TO_ID;

public class WorkDetailsActivityForUser extends AppCompatActivity {

    @BindView(R.id.worker_name_text_view)
    EditText workerNameTextView;
    @BindView(R.id.worker_phone_number)
    EditText workerPhoneNumberTextView;
    @BindView(R.id.posted_at_text_view)
    EditText postedAtTextView;
    @BindView(R.id.price_range_text_view)
    EditText priceRangeTextView;
    @BindView(R.id.user_phone_number)
    EditText userPhoneNumberTextView;
    @BindView(R.id.user_location)
    EditText userLocationTextView;
    @BindView(R.id.deadline_text_view)
    EditText deadlineTextView;
    @BindView(R.id.work_description_text_view)
    EditText workDescriptionTextView;
    @BindView(R.id.phone_icon)
    ImageView call_worker;

    private String created_date;
    private String assigned_to_id;
    private String phone_num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_details_for_user);

        ButterKnife.bind(this);

        workerNameTextView.setInputType(InputType.TYPE_NULL);
        workerPhoneNumberTextView.setInputType(InputType.TYPE_NULL);
        postedAtTextView.setInputType(InputType.TYPE_NULL);
        priceRangeTextView.setInputType(InputType.TYPE_NULL);
        userPhoneNumberTextView.setInputType(InputType.TYPE_NULL);
        userLocationTextView.setInputType(InputType.TYPE_NULL);
        deadlineTextView.setInputType(InputType.TYPE_NULL);
        workDescriptionTextView.setInputType(InputType.TYPE_NULL);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            created_date = (String) bundle.get("created_date");
        }


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference currentWork = database.getReference(CURRENTLY_AVAILABLE_WORKS).child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "-" + created_date);
        currentWork.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UniversalWork work = dataSnapshot.getValue(UniversalWork.class);
                if (work != null) {
                    assigned_to_id = work.getAssigned_to_id();
                }

                if (work != null) {
                    postedAtTextView.setText(work.getCreated_date());
                }
                if (work != null) {
                    priceRangeTextView.setText("Rs." + work.getPrice_range_from() + " - Rs." + work.getPrice_range_to());
                }
                if (work != null) {
                    userPhoneNumberTextView.setText(work.getUser_phone());
                }
                if (work != null) {
                    userLocationTextView.setText(work.getWork_address());
                }
                deadlineTextView.setText(work.getWork_deadline());
                workDescriptionTextView.setText(work.getWork_description());
                workerNameTextView.setText(work.getAssigned_to());

                DatabaseReference currentWorkerPhone = database.getReference().child(USER_ACCOUNTS).child(assigned_to_id).child(PHONE_NUMBER);
                currentWorkerPhone.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        workerPhoneNumberTextView.setText(dataSnapshot.getValue(String.class));
                        phone_num = dataSnapshot.getValue(String.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

       call_worker.setOnClickListener(view -> {
            if (phone_num != null && !phone_num.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone_num));
                startActivity(intent);
            } else {
                Toast.makeText(WorkDetailsActivityForUser.this, "Can't make a call", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
