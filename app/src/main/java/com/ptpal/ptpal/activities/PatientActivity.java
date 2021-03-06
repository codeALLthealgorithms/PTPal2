package com.ptpal.ptpal.activities;

import android.content.Intent;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.ptpal.ptpal.R;
import com.ptpal.ptpal.model.Patient;
import com.ptpal.ptpal.model.ProfilePicture;
import com.ptpal.ptpal.model.Therapy;
import com.ptpal.ptpal.sql.PTPalDB;

import java.util.ArrayList;
import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

public class PatientActivity extends AppCompatActivity implements View.OnClickListener
{
    private final AppCompatActivity activity = PatientActivity.this;

    private NestedScrollView nestedScrollViewP;

    private TextView textViewName;
    private TextView textViewEmail;
    private TextView textViewTherapist;
    private TextView textViewExercise;
    private TextView textViewSD;
    private TextView textViewSPD;
    private TextView textViewDPW;
    private TextView textViewTW;

    private AppCompatButton appCompatButtonChangeExercise;
    private AppCompatButton appCompatButtonTherapyHistory;
    private AppCompatButton appCompatButtonConfirmExercise;

    private CircleImageView circleImageView;

    private AppCompatTextView textViewLinkLogout;
    private AppCompatTextView textViewLinkEditProfile;
    private AppCompatTextView textViewLinkCreateTherapy;

    private PTPalDB myDB;
    private Patient patient;
    private Therapy therapy;
    private ProfilePicture profPic;

    private String patEmail;
    private Spinner skySpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);
        getSupportActionBar().hide();
        patEmail = getIntent().getStringExtra("EMAIL");

        initObjects();
        initViews();
        initListeners();
    }
    private void initObjects()
    {
        myDB = new PTPalDB(activity);
        patient = myDB.getPatient(patEmail);
        profPic = myDB.getPP(patEmail);
    }
    private void initViews() {
        nestedScrollViewP = (NestedScrollView) findViewById(R.id.nestedScrollViewP);

        textViewName = (TextView) findViewById(R.id.textViewName);
        textViewName.setText(patient.getFirstName() + " " + patient.getLastName());

        textViewEmail = (TextView) findViewById(R.id.textViewEmail);
        textViewEmail.setText(patient.getEmail());

        textViewTherapist = (TextView) findViewById(R.id.textViewTherapist);

        textViewExercise = (TextView) findViewById(R.id.textViewExercise);

        textViewSD = (TextView) findViewById(R.id.textViewSD);

        textViewSPD = (TextView) findViewById(R.id.textViewSPD);

        textViewDPW = (TextView) findViewById(R.id.textViewDPW);

        textViewTW = (TextView) findViewById(R.id.textViewTW);

        appCompatButtonChangeExercise = (AppCompatButton) findViewById(R.id.appCompatButtonChangeExercise);
        appCompatButtonTherapyHistory = (AppCompatButton) findViewById(R.id.appCompatButtonTherapyHistory);

        circleImageView = (CircleImageView) findViewById(R.id.circleImageView);
        if(profPic.getData() != null)
        {
            Bitmap map = getImage(profPic.getData());
            circleImageView.setImageBitmap(map);
        }

        textViewLinkLogout = (AppCompatTextView) findViewById(R.id.textViewLinkLogout);
        textViewLinkEditProfile = (AppCompatTextView) findViewById(R.id.textViewLinkEditProfile);
        textViewLinkCreateTherapy = (AppCompatTextView) findViewById(R.id.textViewLinkCreateTherapy);
    }
    private void initListeners(){
        appCompatButtonChangeExercise.setOnClickListener(this);
        textViewLinkLogout.setOnClickListener(this);
        textViewLinkEditProfile.setOnClickListener(this);
        textViewLinkCreateTherapy.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.appCompatButtonChangeExercise:
                exercisePopup();
                break;
            case R.id.textViewLinkCreateTherapy:
                Intent intentTherapy = new Intent(getApplicationContext(), TherapyActivity.class);
                intentTherapy.putExtra("EMAIL", patient.getEmail());
                startActivity(intentTherapy);
                break;
            case R.id.textViewLinkEditProfile:
                Intent intentProfile = new Intent(getApplicationContext(), EditProfileActivity.class);
                intentProfile.putExtra("EMAIL", patient.getEmail());
                startActivityForResult(intentProfile, 1);
                break;
            case R.id.textViewLinkLogout:
                Intent intentLogin = new Intent(activity, LoginActivity.class);
                startActivity(intentLogin);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            patient = myDB.getPatient(data.getStringExtra("EMAIL"));
            profPic = myDB.getPP(data.getStringExtra("EMAIL"));
            textViewName.setText(patient.getFirstName() + " " + patient.getLastName());
            textViewEmail.setText(patient.getEmail());
            if(profPic.getData() != null)
            {
                Bitmap map = getImage(profPic.getData());
                circleImageView.setImageBitmap(map);
            }
        }
    }

    public void exercisePopup()
    {
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.activity_popup, null);
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, focusable);

        String[] exercises = myDB.getPatientTherapies(patEmail);
        ArrayList<String> exer = new ArrayList<String>(Arrays.asList(exercises));
        appCompatButtonConfirmExercise = (AppCompatButton)popupView.findViewById(R.id.appCompatButtonConfirmExercise);
        Spinner mySpinner;
        mySpinner = (Spinner)popupView.findViewById(R.id.exercisePopupSpinner);

        ArrayAdapter<String> myAdapterE = new ArrayAdapter<String>(PatientActivity.this, R.layout.spinner_item, exer);
        myAdapterE.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(myAdapterE);
        skySpinner = mySpinner;

        appCompatButtonConfirmExercise.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                popupWindow.dismiss();
                displayTherapy();
            }
        });

        popupWindow.showAtLocation(textViewEmail, Gravity.TOP,0,0);
    }

   public void displayTherapy()
   {
       therapy = myDB.getSelectedTherapy(patEmail, skySpinner.getSelectedItem().toString());
       textViewTherapist.setText("Therapist: \n" + String.valueOf(therapy.getTherapistEmail()));
       textViewExercise.setText("Exercise: \n" + String.valueOf(therapy.getExercise()));
       textViewSD.setText("Session Duration: " + String.valueOf(therapy.getSessionDuration()));
       textViewSPD.setText("Sessions Per Day: " + String.valueOf(therapy.getSessionsPerDay()));
       textViewDPW.setText("Days Per Week: " + String.valueOf(therapy.getDaysPerWeek()));
       textViewTW.setText("Total Weeks: " + String.valueOf(therapy.getTotalWeeks()));
   }
   public static Bitmap getImage(byte[] image) {
       return BitmapFactory.decodeByteArray(image, 0, image.length);
   }
}
