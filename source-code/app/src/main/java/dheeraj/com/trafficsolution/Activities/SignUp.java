package dheeraj.com.trafficsolution.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.FileNotFoundException;
import java.io.IOException;

import dheeraj.com.trafficsolution.R;

public class SignUp extends AppCompatActivity {

    CircularImageView imagesetter;
    TextView uploadphotolabel;
    Button Signup;
    TextView signintent;
    EditText name;
    EditText email;
    EditText password;
    EditText village;

    ImageButton toggleViewPassword;
    boolean passwordShown=false;

    String namestring;
    String emailString;
    String passwordString;
    String villageString;
    public static final int GET_FROM_GALLERY = 3;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorageRef ;
    Uri selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        imagesetter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });

        uploadphotolabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });

        Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                namestring = name.getText().toString();
                emailString = email.getText().toString();
                passwordString = password.getText().toString();
                villageString = village.getText().toString();

                if (passwordString.length() < 5) {
                    password.setError("Minimum 6 characters required .");
                }

                if (emailString.equals("") && passwordString.equals("") && villageString.equals("") && namestring.equals("")) {
                    village.setError("You can not leave this field empy");
                    password.setError("You can not leave this field empty");
                    name.setError("You can not leave this field empty");
                    email.setError("You can not leave this field empty");
                } else {

                    final ProgressDialog rd = new ProgressDialog(SignUp.this);
                    rd.setTitle("Please Wait!");
                    rd.setMessage("Creating Your Account...");
                    rd.setCancelable(false);
                    rd.show();

                    mAuth.createUserWithEmailAndPassword(emailString, passwordString)
                            .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull final Task<AuthResult> task) {
                                    Log.d("TAG", "createUserWithEmail:onComplete:" + task.isSuccessful());


                                    if (!task.isSuccessful()) {
                                        Toast.makeText(getBaseContext(), "Sign Up Unsuccessful" + task.getException(), Toast.LENGTH_SHORT).show();
                                        rd.cancel();
                                    }else {

                                        try {

                                            StorageReference filepath = mStorageRef.child("photos and videos").child(task.getResult().getUser().getUid());

                                            if (selectedImage != null){
                                                filepath.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                        Toast.makeText(getApplicationContext(), "Upload Done ! ", Toast.LENGTH_LONG).show();
                                                        Log.e("Try custom pic", "" + taskSnapshot.getDownloadUrl().toString());

                                                        mDatabase.child("people").child(task.getResult().getUser().getUid()).child("village").setValue(villageString);
                                                        mDatabase.child("people").child(task.getResult().getUser().getUid()).child("displayNames").setValue(namestring);
                                                        mDatabase.child("people").child(task.getResult().getUser().getUid()).child("photoUrls").setValue(taskSnapshot.getDownloadUrl().toString());
                                                        mDatabase.child("people").child(task.getResult().getUser().getUid()).child("points").setValue("0");
                                                    }
                                                });
                                            }else{
                                                Uri uri = Uri.parse("android.resource://kisan.gramseva.metalwihen.com.farmersnetwork/drawable/user");

                                                filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>(){

                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                    Toast.makeText(getApplicationContext(), "Upload Done ! " + taskSnapshot.getDownloadUrl(), Toast.LENGTH_LONG).show();
                                                    mDatabase.child("people").child(task.getResult().getUser().getUid()).child("village").setValue(villageString);
                                                    mDatabase.child("people").child(task.getResult().getUser().getUid()).child("displayNames").setValue(namestring);
                                                    mDatabase.child("people").child(task.getResult().getUser().getUid()).child("points").setValue("0");
                                                    mDatabase.child("people").child(task.getResult().getUser().getUid()).child("photoUrls").setValue(taskSnapshot.getDownloadUrl().toString());
                                                                                                         }
                                            });
                                            }

                                        }catch (Exception ex){
                                            ex.printStackTrace();
                                        }

                                        rd.cancel();
                                        Toast.makeText(SignUp.this, "Account Successfully Created. Sign In for Security Purpose.", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(SignUp.this,SignIn.class));
                                        finish();
                                    }
                                }
                            });
                }
            }
        });
    }

    void init() {
        setContentView(R.layout.activity_sign_up);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        imagesetter = (CircularImageView) findViewById(R.id.image);
        uploadphotolabel = (TextView) findViewById(R.id.upload_label);
        name = (EditText) findViewById(R.id.username);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        village = (EditText) findViewById(R.id.village);
        Signup = (Button) findViewById(R.id.signup1);
        signintent = (TextView) findViewById(R.id.signinhere);
        toggleViewPassword=(ImageButton)findViewById(R.id.toggle_view_password);

        Typeface MontReg = Typeface.createFromAsset(getApplication().getAssets(), "Montserrat-Regular.otf");
        Typeface MontBold = Typeface.createFromAsset(getApplication().getAssets(), "Montserrat-Bold.otf");
        //Typeface MontHair = Typeface.createFromAsset(getApplication().getAssets(), "Montserrat-Hairline.otf");

        uploadphotolabel.setTypeface(MontReg);
        name.setTypeface(MontReg);
        email.setTypeface(MontReg);
        password.setTypeface(MontReg);
        village.setTypeface(MontReg);
        Signup.setTypeface(MontBold);
        signintent.setTypeface(MontReg);

        toggleViewPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(passwordShown){
                    //DONT SHOW PASSWORD
                    Glide.with(getApplicationContext()).load(R.drawable.ic_view_password).into(toggleViewPassword);
                    passwordShown=false;
                    password.setTransformationMethod(new PasswordTransformationMethod());
                }
                else{
                    //SHOW PASSWORD
                    Glide.with(getApplicationContext()).load(R.drawable.ic_unview_password).into(toggleViewPassword);
                    passwordShown=true;
                    password.setTransformationMethod(null);
                }
                password.setSelection(password.getText().length());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                imagesetter.setBorderColor(getResources().getColor(R.color.colorPrimary));
                imagesetter.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5 && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(SignUp.this, LoginRegisterChoose.class));
        finish();
    }
}