package in.menukart.menukart.view.explore;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import in.menukart.menukart.R;
import in.menukart.menukart.api.ApiClient;
import in.menukart.menukart.db.MenuKartDatabase;
import in.menukart.menukart.entities.explore.Restaurant;
import in.menukart.menukart.entities.explore.RestaurantList;
import in.menukart.menukart.presenter.explore.RestaurantListPresenterImp;

public class ExploreFragment extends Fragment implements RestaurantListView {

    RestaurantListPresenterImp restaurantListPresenterImp;
    RecyclerView recyclerViewExplore;
    SearchView searchViewRestaurant;
    ExploreAdapter exploreAdapter;
  //  SwitchCompat switchCompatVeg;

    TextToSpeech textToSpeech;

    View root;
    private String TAG = "MainActivity";
    private Context context;
    private SwitchCompat switchCompatVeg;
    private boolean isRestaurentLoaded;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_explore, container, false);

        initExploreViews();
        return root;
    }

    private void initExploreViews() {
        context = getActivity();
        recyclerViewExplore = root.findViewById(R.id.recycler_view_restaurants);
        searchViewRestaurant = root.findViewById(R.id.search_view_food);
        switchCompatVeg = root.findViewById(R.id.switch_veg_only);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewExplore.setLayoutManager(layoutManager);
        restaurantListPresenterImp = new RestaurantListPresenterImp(this,
                new ApiClient(context));


        if (ApiClient.isConnectedToInternet(context)) {
            getListOfRestaurantsData();
        } else {
            ApiClient.openAlertDialogWithPositive(context, getString(R.string.error_check_network),
                    getString(R.string.dialog_label_ok));
        }

        setSearchFilter();
      /*  if (switchCompatVeg.isEnabled())
        {
            setVegOnlyFilter();
        }*/

        setVegOnlyFilter();

    }

    private void setVegOnlyFilter() {
        switchCompatVeg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    List<Restaurant> vegOnly = MenuKartDatabase.getDatabase(context).menuKartDao()
                            .getFoodTypOnlyRestaurants("Vegetarian");
                    exploreAdapter.updateList(vegOnly);
                }else  {
                    List<Restaurant> allRestaurants = MenuKartDatabase.getDatabase(context).menuKartDao()
                            .getAllRestaurants();
                    exploreAdapter.updateList(allRestaurants);
                }
            }
        });
    }

    private void setSearchFilter() {
        // listening to search query text change
        searchViewRestaurant.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                // filter recycler view when query submitted
                //  if (restaurantLists.contains(query)) {
                if (exploreAdapter != null) {
                    exploreAdapter.getFilter().filter(query);

                    textToSpeech=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if(status != TextToSpeech.ERROR) {
                                textToSpeech.setLanguage(Locale.UK);
                                textToSpeech.speak(query, TextToSpeech.QUEUE_FLUSH, null);
                            }
                        }
                    });
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                if (exploreAdapter != null) {
                    exploreAdapter.getFilter().filter(query);
                }

                return false;
            }
        });

    }

    private void getListOfRestaurantsData() {
        ApiClient.showProgressBar(context);
        restaurantListPresenterImp.requestRestaurantList();

    }

    @Override
    public void showError(String error) {
        ApiClient.hideProgressBar();
        Log.d(TAG, "onRegistercitizenError: error");
        Toast.makeText(context, getString(R.string.error_invalid_response), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSuccessfulRestaurantList(RestaurantList restaurantList) {
        ApiClient.hideProgressBar();
        isRestaurentLoaded = true;
        if (restaurantList.getList() != null) {
            MenuKartDatabase.getDatabase(context).menuKartDao().deleteAllRestaurants();
            MenuKartDatabase.getDatabase(context).menuKartDao().insertAllRestaurant(restaurantList.getList());
            exploreAdapter = new ExploreAdapter(context,restaurantList.getList());
            recyclerViewExplore.setAdapter(exploreAdapter);
            exploreAdapter.notifyDataSetChanged();
        }
    }
}