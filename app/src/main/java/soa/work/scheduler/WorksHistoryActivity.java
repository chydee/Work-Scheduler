package soa.work.scheduler;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import soa.work.scheduler.models.IndividualWork;

import static soa.work.scheduler.Constants.USER_ACCOUNTS;
import static soa.work.scheduler.Constants.WORKS_POSTED;

public class WorksHistoryActivity extends AppCompatActivity implements WorksHistoryAdapter.ItemCLickListener {

    @BindView(R.id.history_recycler_view)
    RecyclerView historyRecyclerView;
    @BindView(R.id.no_history)
    TextView noHistoryTextView;

    private WorksHistoryAdapter worksHistoryAdapter;
    private ArrayList<IndividualWork> workList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_works_history);

        ButterKnife.bind(this);

        worksHistoryAdapter = new WorksHistoryAdapter(workList);
        worksHistoryAdapter.setItemClickListener(this);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setHasFixedSize(true);
        historyRecyclerView.setAdapter(worksHistoryAdapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userAccounts = database.getReference(USER_ACCOUNTS);
        DatabaseReference userAccount = userAccounts.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        DatabaseReference worksPosted = userAccount.child(WORKS_POSTED);

        worksPosted.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                workList.clear();
                if (dataSnapshot.getChildrenCount() == 0) {
                    noHistoryTextView.setVisibility(View.VISIBLE);
                    worksHistoryAdapter.notifyDataSetChanged();
                    return;
                }
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    workList.add(item.getValue(IndividualWork.class));
                }
                Collections.sort(workList, new Comparator<IndividualWork>() {
                    @Override
                    public int compare(IndividualWork individualWork, IndividualWork t1) {
                        return individualWork.getCreated_date().compareTo(t1.getCreated_date());
                    }
                });
                Collections.reverse(workList);
                noHistoryTextView.setVisibility(View.GONE);
                worksHistoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(WorksHistoryActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(IndividualWork work) {
        Intent intent = new Intent(WorksHistoryActivity.this, WorkDetailsActivityForUser.class);
        intent.putExtra("created_date", work.getCreated_date());
        startActivity(intent);
    }
}
