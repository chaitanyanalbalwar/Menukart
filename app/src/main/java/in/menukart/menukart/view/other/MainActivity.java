package in.menukart.menukart.view.other;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.nex3z.notificationbadge.NotificationBadge;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

import in.menukart.menukart.R;
import in.menukart.menukart.db.MenuKartDatabase;
import in.menukart.menukart.entities.foodcart.UserDetails;
import in.menukart.menukart.util.AppConstants;
import in.menukart.menukart.view.explore.ExploreFragment;
import in.menukart.menukart.view.foodcart.cart.FoodCartActivity;
import in.menukart.menukart.view.order.orderlist.OrdersFragment;
import in.menukart.menukart.view.setting.SettingsFragment;

import android.location.LocationListener;
import android.location.LocationManager;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final int REQUEST_CODE = 101;
    BottomNavigationView bottomNavigationView;
    Toolbar toolbarHome;
    LinearLayout linearLayoutOther;
    RelativeLayout relativeLayoutToolbar;
    AppCompatTextView toolbarTitle, tvLocationName;
    SharedPreferences sharedPreferences;
    UserDetails userDetails;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private Context context;
    private ImageView mImgCartIcon;

    NotificationBadge notificationBadge;
    private int cartItemCounts = 0;

    int REQUEST_CHECK_SETTINGS = 100;

    LocationManager service;
    boolean enabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setCustomToolbar();
        initHomeViews();

        service = (LocationManager) getSystemService(LOCATION_SERVICE);
        enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!enabled) {
            EnableGPSAutoMatically();
        }

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        Log.d("ip Address",ipAddress);




       /* try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "in.menukart.menukart",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        }
        catch (PackageManager.NameNotFoundException e) {
        }
        catch (NoSuchAlgorithmException e) {
        }*/

    }

    private void setCustomToolbar() {
        toolbarHome = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbarHome);

        toolbarTitle = toolbarHome.findViewById(R.id.tv_toolbar_title);
        tvLocationName = toolbarHome.findViewById(R.id.tv_location_name);
        tvLocationName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tvLocationName.setSelected(true);
        tvLocationName.setSingleLine(true);

        sharedPreferences = getSharedPreferences(AppConstants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(AppConstants.USER_DETAILS, null);
        userDetails = gson.fromJson(json, UserDetails.class);
        relativeLayoutToolbar = toolbarHome.findViewById(R.id.ll_toolbar_explore);
        linearLayoutOther = toolbarHome.findViewById(R.id.ll_toolbar_other);
        mImgCartIcon = findViewById(R.id.ic_cart);

        notificationBadge = findViewById(R.id.badge);
        actions();
    }

    private void actions() {
        mImgCartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (tvLocationName.getText().toString().isEmpty()){
                    Toast.makeText(context, "Please Enable your location...", Toast.LENGTH_SHORT).show();
                }else {
                    navigateToCartScreen();
                }

            }
        });
        notificationBadge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tvLocationName.getText().toString().isEmpty()){
                    Toast.makeText(context, "Please Enable your location...", Toast.LENGTH_SHORT).show();

                }else {
                    navigateToCartScreen();
                }
            }
        });

        tvLocationName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchLocation();

            }
        });
    }

    private void navigateToCartScreen() {
        if (cartItemCounts == 0) {
            /*Toast toast = Toast.makeText(context, "Cart is empty, " +
                    "Please add something.", Toast.LENGTH_SHORT);
            toast.getView().setBackgroundColor(getResources().getColor(R.color.shadow));
            TextView toastMessage = toast.getView().findViewById(android.R.id.message);
            toastMessage.setTextColor(getResources().getColor(R.color.colorWhite));
            toast.show();*/

            Toast.makeText(context, "Cart is empty,Please add something.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intentFoodCart = new Intent(MainActivity.this, FoodCartActivity.class);
        startActivity(intentFoodCart);
    }

    private void initHomeViews() {
        context = MainActivity.this;

        bottomNavigationView = findViewById(R.id.navigation);
        setBottomNavigationMenus();
        Fragment fragment = new ExploreFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
        //   bottomNavigationView.setSelectedItemId(R.id.menu_explore);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLocation();

    }

    private void fetchLocation() {

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    //Toast.makeText(getApplicationContext(), currentLocation.getLatitude() + "" + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    getUserCurrentAddress(currentLocation.getLatitude(), currentLocation.getLongitude());
                }
            }
        });
    }

    private void setBottomNavigationMenus() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.menu_explore:
                        fragment = new ExploreFragment();
                        loadFragment(fragment);
                        relativeLayoutToolbar.setVisibility(View.VISIBLE);
                        linearLayoutOther.setVisibility(View.GONE);
                        //Toast.makeText(context, "Explore Clicked", Toast.LENGTH_SHORT).show();
                        return true;
                    //break;
                    case R.id.menu_orders:
                        fragment = new OrdersFragment();
                        loadFragment(fragment);
                        toolbarTitle.setText("Order History");
                        relativeLayoutToolbar.setVisibility(View.GONE);
                        linearLayoutOther.setVisibility(View.VISIBLE);
                        // Toast.makeText(context, "Orders Clicked", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.menu_settings:
                        fragment = new SettingsFragment();
                        loadFragment(fragment);
                        toolbarTitle.setText("Settings");
                        relativeLayoutToolbar.setVisibility(View.GONE);
                        linearLayoutOther.setVisibility(View.VISIBLE);

                        return true;
                    default:
                }
                return false;
            }
        });


    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void getUserCurrentAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            // Toast.makeText(MapsActivity.this,address,Toast.LENGTH_SHORT).show();
            tvLocationName.setText(address);
            SharedPreferences sharedPreferences = getSharedPreferences(AppConstants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(AppConstants.USER_CURRENT_ADDRESS, address);
            editor.putString(AppConstants.USER_CURRENT_CITY, city);
            editor.putString(AppConstants.USER_CURRENT_LATITUDE, String.valueOf(latitude));
            editor.putString(AppConstants.USER_CURRENT_LONGITUDE, String.valueOf(longitude));
            editor.apply();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        BottomNavigationView mBottomNavigationView = findViewById(R.id.navigation);
        if (mBottomNavigationView.getSelectedItemId() == R.id.menu_explore) {
            super.onBackPressed();
            //  clearBackStack();
            finish();
        } else {
            mBottomNavigationView.setSelectedItemId(R.id.menu_explore);
        }
    }

    public void clearBackStack() {

        FragmentManager.BackStackEntry entry = getSupportFragmentManager().getBackStackEntryAt(
                0);
        getSupportFragmentManager().popBackStack(entry.getId(),
                FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().executePendingTransactions();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLocation();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cartItemCounts = MenuKartDatabase.getDatabase(context)
                .menuKartDao().getAllAddedItems().size();
        notificationBadge.setText("" + cartItemCounts);
        notificationBadge.setVisibility(cartItemCounts == 0 ? View.INVISIBLE : View.VISIBLE);

    }

    private void EnableGPSAutoMatically() {
        GoogleApiClient googleApiClient = null;
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            googleApiClient.connect();
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            // **************************
            builder.setAlwaysShow(true); // this is the key ingredient
            // **************************

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                    .checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {

                                status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);

                                LocationRequest mLocationRequest = LocationRequest.create();
                                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                                fetchLocation();


                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                            break;
                    }
                }
            });
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        fetchLocation();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}