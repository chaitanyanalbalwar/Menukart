package in.menukart.menukart.view.foodcart.signup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import in.menukart.menukart.R;
import in.menukart.menukart.api.ApiClient;
import in.menukart.menukart.entities.foodcart.SendOtp;
import in.menukart.menukart.presenter.foodcart.otpverification.SendOtpPresenterImp;
import in.menukart.menukart.util.AppConstants;
import in.menukart.menukart.view.foodcart.otpverification.PhoneVerificationActivity;
import in.menukart.menukart.view.foodcart.otpverification.SendOtpView;
import in.menukart.menukart.view.foodcart.trackorder.OrderSummaryActivity;

public class LoginActivity extends AppCompatActivity implements SendOtpView, GoogleApiClient.OnConnectionFailedListener {

    private AppCompatEditText editLoginMobileNumber;
    private AppCompatButton btnLoginSendCode;
    private Context context;

    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    SignInButton signInButton;
    private GoogleApiClient googleApiClient;
    private static final int RC_SIGN_IN = 1;

    AccessToken accessToken;
    boolean isLoggedIn;

    private static final String EMAIL = "email";
    private static final String AUTH_TYPE = "rerequest";
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = LoginActivity.this;

        sharedPreferences = getSharedPreferences(AppConstants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        /*Boolean isLogin=sharedPreferences.getBoolean("FbLogin",false);
        if (isLogin==true){

            Intent intentOrderSummary = new Intent(LoginActivity.this, OrderSummaryActivity.class);
            startActivity(intentOrderSummary);
        }else {
            initLoginViews();
        }*/

        initLoginViews();



        GoogleSignInOptions gso =  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

    }

    private void initLoginViews() {
        editLoginMobileNumber = findViewById(R.id.et_login_mobile_number);
        btnLoginSendCode = findViewById(R.id.btn_login_send_code);
        btnLoginSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editLoginMobileNumber.getText().toString().isEmpty()) {
                    editLoginMobileNumber.setError("Enter Mobile Number!");
                } else {
                    if (ApiClient.isConnectedToInternet(context)) {
                        callSendOtpAPI();
                    } else {
                        ApiClient.openAlertDialogWithPositive(context, getString(R.string.error_check_network),
                                getString(R.string.dialog_label_ok));
                    }
                }
            }
        });

        isLoggedIn  = accessToken != null && !accessToken.isExpired();

        if (isLoggedIn){
            Intent intentOrderSummary = new Intent(LoginActivity.this, OrderSummaryActivity.class);
            startActivity(intentOrderSummary);
        }

        mCallbackManager = CallbackManager.Factory.create();
        LoginButton mLoginButton = findViewById(R.id.login_button);
        mLoginButton.setPermissions(Arrays.asList(EMAIL));
        mLoginButton.setAuthType(AUTH_TYPE);

        accessToken = AccessToken.getCurrentAccessToken();

        mLoginButton.registerCallback(
                mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        setResult(RESULT_OK);
                        Log.d("login details",loginResult.toString());

                        getUserProfile(accessToken.getCurrentAccessToken());

                        editor.putBoolean("FbLogin",true);
                        editor.commit();
                        editor.apply();

                        finish();
                    }

                    @Override
                    public void onCancel() {
                        setResult(RESULT_CANCELED);
                        finish();
                    }

                    @Override
                    public void onError(FacebookException e) {
                        // Handle exception
                    }
                });


        signInButton=(SignInButton)findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent,RC_SIGN_IN);
            }
        });

    }

    private void callSendOtpAPI() {
        ApiClient.showProgressBar(LoginActivity.this);
        try {
            SendOtpPresenterImp sendOtpPresenterImp =
                    new SendOtpPresenterImp(this,
                            new ApiClient(LoginActivity.this));
            Map<String, String> params = new HashMap<String, String>();
            params.put("mobileno", editLoginMobileNumber.getText().toString());
            String string = params.toString();
            Log.d("TAG", string);
            sendOtpPresenterImp.requestSendOtp(params);

        } catch (Exception e) {
            Log.d("TAG", "sendOtp" + e.getMessage());
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void showError(String error) {
        ApiClient.hideProgressBar();
        Log.d("TAG", "onMenuList: error");
        Toast.makeText(LoginActivity.this, getString(R.string.error_invalid_response), Toast.LENGTH_LONG).show();

    }

    @Override
    public void onSuccessful(SendOtp sendOtp) {

        try{
            ApiClient.hideProgressBar();
        if (sendOtp.getOtp() != null) {


                Toast.makeText(context, "OTP sent successfully!", Toast.LENGTH_SHORT).show();
                Intent intentVerifyOtp = new Intent(LoginActivity.this, PhoneVerificationActivity.class);
                intentVerifyOtp.putExtra(AppConstants.USER_MOBILE_NUMBER, editLoginMobileNumber.getText().toString());
                startActivity(intentVerifyOtp);
                this.finish();

        }else {
            Log.d("error","otp issue...");
        }
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        if(requestCode==RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result){
        if(result.isSuccess()){

            GoogleSignInAccount account=result.getSignInAccount();

            Log.d("name",account.getDisplayName());
            Log.d("mail",account.getEmail());
            Log.d("id",account.getId());
            Log.d("photo", String.valueOf(account.getPhotoUrl()));

        }else{
            Toast.makeText(getApplicationContext(),"Sign in cancel",Toast.LENGTH_LONG).show();
        }
    }


    private void getUserProfile(AccessToken currentAccessToken) {

        GraphRequest request = GraphRequest.newMeRequest(
                currentAccessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                     //   Log.d("TAG", object.toString());
                        try {
                            String first_name = object.getString("first_name");
                            String last_name = object.getString("last_name");
                            String email = object.getString("email");
                            String id = object.getString("id");
                            String mobile = object.getString("mobile");
                            String image_url = "https://graph.facebook.com/" + id + "/picture?type=normal";

                            /*txtUsername.setText("First Name: " + first_name + "\nLast Name: " + last_name);
                            txtEmail.setText(email);
                            Picasso.with(MainActivity.this).load(image_url).into(imageView);*/

                            Log.d("First name",first_name);
                            Log.d("Last name",last_name);
                            Log.d("email",email);
                            Log.d("photoUrl",image_url);


                            editor.putBoolean("FbLogin",true);
                            editor.commit();
                            editor.apply();



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name,last_name,email,id");
        request.setParameters(parameters);
        request.executeAsync();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}