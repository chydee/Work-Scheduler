package soa.work.scheduler;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import soa.work.scheduler.Retrofit.ApiService;
import soa.work.scheduler.Retrofit.RetrofitClient;

import static soa.work.scheduler.Constants.CURRENTLY_AVAILABLE_WORKS;
import static soa.work.scheduler.Constants.USER_ACCOUNTS;
import static soa.work.scheduler.Constants.WORKS_POSTED;
import static soa.work.scheduler.Constants.WORK_CATEGORY;

public class WorkFormActivity extends AppCompatActivity implements DatePickerFragment.DateDialogListener, TimePickerFragment.TimeDialogListener {

    @BindView(R.id.address_edit_text)
    EditText addressEditText;
    @BindView(R.id.price_range_from)
    EditText priceRangeFromEditText;
    @BindView(R.id.price_range_to)
    EditText priceRangeToEditText;
    @BindView(R.id.phone_number)
    EditText phoneNumberEditText;
    @BindView(R.id.work_description_edit_text)
    EditText workDescriptionEditText;
    @BindView(R.id.picked_date_text_view)
    TextView pickedDateTextView;
    @BindView(R.id.date)
    Button button_date;
    private String oneSignalAppId;
    private String oneSignalRestApiKey;
    private String workCategory;
    private static final String DIALOG_DATE = "MainActivity.DateDialog";
    private static final String DIALOG_TIME = "MainActivity.TimeDialog";
    private String deadline, deadline_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_form);

        ButterKnife.bind(this);
        workCategory = getIntent().getStringExtra(WORK_CATEGORY);
        getOneSignalKeys();

        button_date.setOnClickListener(arg0 -> {
            DatePickerFragment dialog = new DatePickerFragment();
            dialog.show(getSupportFragmentManager(), DIALOG_DATE);
        });
    }

    private void broadcast() {
        //Toast.makeText(this, date1, Toast.LENGTH_SHORT).show();
        if (addressEditText.getText().toString().trim().isEmpty() ||
                phoneNumberEditText.getText().toString().trim().isEmpty() ||
                workDescriptionEditText.getText().toString().trim().isEmpty() ||
                deadline == null ||
                deadline.trim().isEmpty() ||
                priceRangeFromEditText.getText().toString().trim().isEmpty() ||
                priceRangeToEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (oneSignalAppId == null || oneSignalAppId.isEmpty() || oneSignalRestApiKey == null || oneSignalRestApiKey.isEmpty()) {
            Toast.makeText(this, "Something went wrong. Please try again", Toast.LENGTH_SHORT).show();
            getOneSignalKeys();
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();


        //Saving work info to currently_available_works
        DatabaseReference currentlyAvailableWorksRef = database.getReference(CURRENTLY_AVAILABLE_WORKS);
        UniversalWork work = new UniversalWork();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        work.setAssigned_at("");
        work.setAssigned_to("");
        work.setCreated_date(currentDateAndTime);
        work.setUser_phone(phoneNumberEditText.getText().toString());
        work.setWork_address(addressEditText.getText().toString());
        work.setPrice_range_from(priceRangeFromEditText.getText().toString());
        work.setPrice_range_to(priceRangeToEditText.getText().toString());
        work.setWork_category(workCategory);
        work.setWork_completed(false);
        work.setWork_deadline(deadline);
        work.setWork_description(workDescriptionEditText.getText().toString());
        if (user != null) {
            work.setWork_posted_by_account_id(user.getUid());
        }
        if (user != null) {
            work.setWork_posted_by_name(user.getDisplayName());
        }
        if (user != null) {
            currentlyAvailableWorksRef.child(user.getUid() + "-" + currentDateAndTime).setValue(work);
        }

        //Saving work info to user's history
        DatabaseReference userAccountsRef = database.getReference(USER_ACCOUNTS);
        IndividualWork individualWork = new IndividualWork();
        individualWork.setWork_category(workCategory);
        individualWork.setWork_description(workDescriptionEditText.getText().toString());
        individualWork.setWork_address(addressEditText.getText().toString());
        individualWork.setUser_phone(phoneNumberEditText.getText().toString());
        individualWork.setPrice_range_from(priceRangeFromEditText.getText().toString());
        individualWork.setPrice_range_to(priceRangeToEditText.getText().toString());
        individualWork.setCreated_date(currentDateAndTime);
        individualWork.setAssigned_to("");
        individualWork.setAssigned_at("");
        individualWork.setWork_completed(false);
        individualWork.setWork_deadline(deadline);
        if (user != null) {
            userAccountsRef.child(user.getUid()).child(WORKS_POSTED).child(user.getUid() + "-" + currentDateAndTime).setValue(individualWork);
        }

        AsyncTask.execute(this::sendNotification);

        Toast.makeText(this, "Broadcast complete", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void sendNotification() {
        try {
            String jsonResponse;

            URL url = new URL("https://onesignal.com/api/v1/notifications");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setUseCaches(false);
            con.setDoOutput(true);
            con.setDoInput(true);

            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Authorization", "Basic " + oneSignalRestApiKey);
            con.setRequestMethod("POST");

            String strJsonBody = "{"
                    + "\"app_id\": \"" + oneSignalAppId + "\","
                    + "\"filters\": [{\"field\": \"tag\", \"key\": \"" + WORK_CATEGORY + "\", \"relation\": \"=\", \"value\": \"" + workCategory + "\"},{\"operator\": \"OR\"},{\"field\": \"amount_spent\", \"relation\": \">\",\"value\": \"0\"}],"
                    + "\"data\": {\"foo\": \"bar\"},"
                    + "\"contents\": {\"en\": \"A new work is available\"}"
                    + "}";


            System.out.println("strJsonBody:\n" + strJsonBody);

            byte[] sendBytes = strJsonBody.getBytes(StandardCharsets.UTF_8);
            con.setFixedLengthStreamingMode(sendBytes.length);

            OutputStream outputStream = con.getOutputStream();
            outputStream.write(sendBytes);

            int httpResponse = con.getResponseCode();
            System.out.println("httpResponse: " + httpResponse);

            if (httpResponse >= HttpURLConnection.HTTP_OK
                    && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
                jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                scanner.close();
            } else {
                Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
                jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                scanner.close();
            }
            System.out.println("jsonResponse:\n" + jsonResponse);

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void getOneSignalKeys() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        storageRef.child("onesignal_id.txt").getDownloadUrl().addOnSuccessListener(uri -> {
            ApiService apiService = RetrofitClient.getApiService();
            Call call = apiService.getOneSignalIds(uri.toString());
            call.enqueue(new Callback<OneSignalIds>() {

                @Override
                public void onResponse(@NonNull Call<OneSignalIds> call, @NonNull Response<OneSignalIds> response) {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        oneSignalAppId = response.body().getOnesignalAppId();
                        oneSignalRestApiKey = response.body().getRestApiKey();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<OneSignalIds> call, @NonNull Throwable t) {

                }
            });
        }).addOnFailureListener(e -> {

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.work_form_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.send_work_menu_item) {
            broadcast();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFinishDialog(Date date) {
        deadline_date = formatDate(date);
        TimePickerFragment dialog = new TimePickerFragment();
        dialog.show(getSupportFragmentManager(), DIALOG_TIME);
    }

    public String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onFinishDialog(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
            Date date = sdf.parse(deadline_date + " " + time);
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault());
            deadline = sdf2.format(date);
            pickedDateTextView.setText(deadline.replace("_", " "));
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Something went wrong. Try again", Toast.LENGTH_SHORT).show();
        }
    }
}
