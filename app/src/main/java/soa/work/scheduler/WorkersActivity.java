package soa.work.scheduler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static soa.work.scheduler.Constants.USER_ACCOUNTS;
import static soa.work.scheduler.Constants.CURRENTLY_AVAILABLE_WORKS;
import static soa.work.scheduler.Constants.WORK_CATEGORY;

@SuppressWarnings("FieldCanBeLocal")
public class WorkersActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.nav_view)
    NavigationView navigationView;

    private FirebaseUser currentUser;
    private ImageView profilePictureImageView;
    private TextView profileNameTextView;
    private WorksAvailableAdapter worksAvailableAdapter;
    private ArrayList<UniversalWork> workList = new ArrayList<>();
    @BindView(R.id.works_recycler_view)
    RecyclerView worksRecyclerView;
    @BindView(R.id.no_history)
    TextView noWorksTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workers);

        ButterKnife.bind(this);
        setTitle("Worker Account");

        worksAvailableAdapter = new WorksAvailableAdapter(workList, this);
        worksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        worksRecyclerView.setHasFixedSize(true);
        worksRecyclerView.setAdapter(worksAvailableAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        View header = navigationView.getHeaderView(0);
        profilePictureImageView = header.findViewById(R.id.profile_picture_image_view);
        profileNameTextView = header.findViewById(R.id.profile_name_text_view);
        Picasso.get().load(currentUser.getPhotoUrl()).into(profilePictureImageView);
        profileNameTextView.setText(currentUser.getDisplayName());

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference currently_available = database.getReference().child(CURRENTLY_AVAILABLE_WORKS);
        currently_available.orderByChild(WORK_CATEGORY)
                .equalTo("Mechanic") //need to fetch the work_category of current user
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                workList.clear();
                if (dataSnapshot.getChildrenCount() == 0) {
                    noWorksTextView.setVisibility(View.VISIBLE);
                    worksAvailableAdapter.notifyDataSetChanged();
                    return;
                }
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    workList.add(item.getValue(UniversalWork.class));
                }
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
}
