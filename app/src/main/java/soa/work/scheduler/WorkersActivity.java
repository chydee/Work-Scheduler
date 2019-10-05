package soa.work.scheduler;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import soa.work.scheduler.models.AppStatus;
import soa.work.scheduler.models.UniversalWork;

import static soa.work.scheduler.Constants.CURRENTLY_AVAILABLE_WORKS;
import static soa.work.scheduler.Constants.USER_ACCOUNTS;
import static soa.work.scheduler.Constants.WORKER_ACCOUNT;
import static soa.work.scheduler.Constants.WORK_CATEGORY;

@SuppressWarnings("FieldCanBeLocal")
public class WorkersActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, WorksAvailableAdapter.ItemCLickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    private String work_category;
    private FirebaseUser firebaseUser;
    private ImageView profilePictureImageView;
    private TextView profileNameTextView;
    private AppStatus appStatus;
    private WorksAvailableAdapter worksAvailableAdapter;
    private ArrayList<UniversalWork> workList = new ArrayList<>();
    @BindView(R.id.works_recycler_view)
    RecyclerView worksRecyclerView;
    @BindView(R.id.no_history)
    TextView noWorksTextView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workers);

        ButterKnife.bind(this);

        new PrefManager(this).setLastOpenedActivity(WORKER_ACCOUNT);

        appStatus = new AppStatus(this);
        setTitle("Worker Account");
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCancelable(false);
        if (!appStatus.isOnline()) {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        } else {
            worksAvailableAdapter = new WorksAvailableAdapter(workList, this);
            worksAvailableAdapter.setItemClickListener(this);
            worksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            worksRecyclerView.setHasFixedSize(true);
            worksRecyclerView.setAdapter(worksAvailableAdapter);

            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            View header = navigationView.getHeaderView(0);
            profilePictureImageView = header.findViewById(R.id.profile_picture_image_view);
            profileNameTextView = header.findViewById(R.id.profile_name_text_view);
            Picasso.get().load(firebaseUser.getPhotoUrl()).into(profilePictureImageView);
            profileNameTextView.setText(firebaseUser.getDisplayName());

            setSupportActionBar(toolbar);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            navigationView.setNavigationItemSelectedListener(this);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference workCategoryRef = database.getReference(USER_ACCOUNTS).child(firebaseUser.getUid()).child(WORK_CATEGORY);
            DatabaseReference currently_available = database.getReference().child(CURRENTLY_AVAILABLE_WORKS);

            workCategoryRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    work_category = dataSnapshot.getValue(String.class);
                    OneSignal.sendTag(WORK_CATEGORY, work_category);

                    currently_available.orderByChild(WORK_CATEGORY)
                            .equalTo(work_category)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    workList.clear();
                                    if (dataSnapshot.getChildrenCount() == 0) {
                                        noWorksTextView.setVisibility(View.VISIBLE);
                                        worksAvailableAdapter.notifyDataSetChanged();
                                        return;
                                    }
                                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                                        UniversalWork work = item.getValue(UniversalWork.class);
                                        if (Objects.requireNonNull(work).getAssigned_at().isEmpty() && work.getAssigned_to().isEmpty()) {
                                            if (!work.getWork_posted_by_account_id().equals(firebaseUser.getUid())) {
                                                workList.add(work);
                                            }
                                        } else {
                                            if (work.getAssigned_to_id().equals(firebaseUser.getUid())) {
                                                workList.add(work);
                                            }
                                        }
                                    }
                                    if (workList.isEmpty()) {
                                        noWorksTextView.setVisibility(View.VISIBLE);
                                        worksAvailableAdapter.notifyDataSetChanged();
                                        return;
                                    }
                                    progressDialog.dismiss();
                                    Collections.sort(workList, (individualWork, t1) -> individualWork.getCreated_date().compareTo(t1.getCreated_date()));
                                    Collections.reverse(workList);
                                    noWorksTextView.setVisibility(View.GONE);
                                    worksAvailableAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(WorkersActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                }
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
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
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.switch_to_user_account_menu_item:
                Intent mainActivity = new Intent(WorkersActivity.this, MainActivity.class);
                startActivity(mainActivity);
                return true;
            case R.id.settings_menu_item:
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.logout:
                Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(WorkersActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                drawerLayout.closeDrawer(GravityCompat.START);
                return false;
        }
    }

    @Override
    public void onItemClick(UniversalWork work) {
        Intent intent = new Intent(this, WorkDetailsActivity.class);
        intent.putExtra("created_date", work.getCreated_date());
        intent.putExtra("work_posted_by_account_id", work.getWork_posted_by_account_id());
        startActivity(intent);
    }
}
