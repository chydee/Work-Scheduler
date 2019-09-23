package soa.work.scheduler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;

import static soa.work.scheduler.Constants.CURRENTLY_AVAILABLE_WORKS;

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

    private String created_date;

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
                postedAtTextView.setText(work.getCreated_date());
                priceRangeTextView.setText("Rs." + work.getPrice_range_from() + " - Rs." + work.getPrice_range_to());
                userPhoneNumberTextView.setText(work.getUser_phone());
                userLocationTextView.setText(work.getWork_address());
                deadlineTextView.setText(work.getWork_deadline());
                workDescriptionTextView.setText(work.getWork_description());
                workerNameTextView.setText(work.getAssigned_to());
                workerPhoneNumberTextView.setText(work.getWorker_phone_number());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
