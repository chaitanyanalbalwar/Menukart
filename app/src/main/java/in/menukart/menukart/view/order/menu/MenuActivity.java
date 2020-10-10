package in.menukart.menukart.view.order.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.loader.content.AsyncTaskLoader;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import in.menukart.menukart.CartUpdates;
import in.menukart.menukart.api.MyApplication;
import in.menukart.menukart.db.MenuKartDatabase;
import in.menukart.menukart.entities.explore.Restaurant;
import in.menukart.menukart.entities.foodcart.FoodCart;
import in.menukart.menukart.view.foodcart.cart.FoodCartActivity;
import in.menukart.menukart.R;
import in.menukart.menukart.api.ApiClient;
import in.menukart.menukart.entities.order.Category;
import in.menukart.menukart.entities.order.Menu;
import in.menukart.menukart.entities.order.RestaurantMenu;
import in.menukart.menukart.presenter.order.menu.MenuPresenterImp;
import in.menukart.menukart.util.AppConstants;
import in.menukart.menukart.view.order.OrderRecord;

public class MenuActivity extends AppCompatActivity implements MenuView, CartUpdates {
    MenuPresenterImp menuPresenterImp;
    Context context;
    RecyclerView rcvCategory, rcvMenu;
    String restaurantId,CGST,SGST;
    List<RestaurantMenu> restaurantMenus;
    List<RestaurantMenu> restaurantMenuList;
    List<Category> categories;
    List<RestaurantMenu> filteredMenus;
    LinearLayout llCart;
    AppCompatTextView textCartItems, textCartValue;
    private String TAG = "MenuActivity";
    private CategoryAdapter categoryAdapter;
    private MenuAdapter menuAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        context = MenuActivity.this;

        String restaurantName = getIntent().getExtras().getString(AppConstants.RESTAURANT_NAME);
        restaurantId = getIntent().getExtras().getString(AppConstants.RESTAURANT_ID);
        CGST = getIntent().getExtras().getString(AppConstants.CGST);
        SGST = getIntent().getExtras().getString(AppConstants.SGST);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(restaurantName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initMenuViews();

        categoryAdapter = new CategoryAdapter(new ArrayList<Category>(), context);
        rcvCategory.setAdapter(categoryAdapter);

        menuAdapter = new MenuAdapter(new ArrayList<RestaurantMenu>(), context, this, restaurantName);
        rcvMenu.setAdapter(menuAdapter);
    }

    private void initMenuViews() {
        menuPresenterImp = new MenuPresenterImp(this,
                new ApiClient(context));
        //restaurantMenuList = new ArrayList<>();

        llCart = findViewById(R.id.ll_cart_items);
        textCartItems = findViewById(R.id.tv_cart_items);
        textCartValue = findViewById(R.id.tv_items_total_value);
        rcvCategory = findViewById(R.id.rcv_category);
        rcvMenu = findViewById(R.id.rcv_menu);
        // add a divider after each item for more clarity
        rcvCategory.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL));
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(context,
                LinearLayoutManager.HORIZONTAL, false);
        rcvCategory.setLayoutManager(horizontalLayoutManager);
        rcvCategory.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL, false);
        rcvMenu.setLayoutManager(linearLayoutManager);
        rcvMenu.setHasFixedSize(true);
        restaurantMenus = new ArrayList<>();
        categories = new ArrayList<>();

        if (ApiClient.isConnectedToInternet(context)) {
            getListOfMenuData();
        } else {
            ApiClient.openAlertDialogWithPositive(context, getString(R.string.error_check_network),
                    getString(R.string.dialog_label_ok));
        }

    }

    private void getListOfMenuData() {
        ApiClient.showProgressBar(context);
         try {
            menuPresenterImp.requestMenuList(restaurantId);
        } catch (Exception e) {
            Log.d(TAG, "MenuList" + e.getMessage());
        }
    }

    @Override
    public void showError(String error) {
        ApiClient.hideProgressBar();
        Log.d(TAG, "onMenuList: error");
        Toast.makeText(context, getString(R.string.error_invalid_response), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSuccessfulMenuList(Menu menu) {
        ApiClient.hideProgressBar();
        if (menu.getData().getCategories() != null) {
            categories = menu.getData().getCategories();
            Category category = new Category();
            category.setId("");
            category.setCategory_name("All");
            categories.add(0, category);
            categoryAdapter.updateList(categories);
        }

        //restaurantMenus.addAll(menu.getData().getMenus());
        //TODO put db query into thread
        MenuKartDatabase.getDatabase(MenuActivity.this).menuKartDao().insertAll(menu.getData().getMenus());
        restaurantMenus = MenuKartDatabase.getDatabase(MenuActivity.this).menuKartDao().getAllByRestaurantId(restaurantId);

        menuAdapter.updateList(restaurantMenus);

        Log.i("onSuccessfulMenuList", restaurantMenus.toString());
    }

    public void setDataOnCategorySelection(String categoryId) {
        if(!categoryId.isEmpty()){
            filteredMenus = new ArrayList<>();
            for (RestaurantMenu restaurantMenu : restaurantMenus) {
                if (restaurantMenu.getMenu_categoryid().equalsIgnoreCase(categoryId)) {
                    filteredMenus.add(restaurantMenu);
                }
            }
            menuAdapter.updateList(filteredMenus);
        }else {
            menuAdapter.updateList(restaurantMenus);
        }
    }


//    public void addDataOnMenuSelction() {
//        restaurantMenuList = MenuKartDatabase.getDatabase(context).menuKartDao().getAllAddedItems();
//        Log.i("addDataOnMenuSelectio:", restaurantMenuList.toString());
//
//        if(!restaurantMenuList.isEmpty()){
//           showItemCart();
//        }else {
//            llCart.setVisibility(View.GONE);
//        }
//       /* if (restaurantMenu != null) {
//            textCartValue.setText("\u20B9 " + restaurantMenu.getMenu_price());
//            if (restaurantMenuList != null) {
//                showItemCart();
//            } else {
//                llCart.setVisibility(View.GONE);
//            }
//        }*/
//    }

    private void showItemCart() {

        llCart.setVisibility(View.VISIBLE);
        textCartItems.setText("Go To Cart " + "(" + restaurantMenuList.size() + " items added)");
        textCartItems.setSingleLine();
        textCartItems.setTextSize(12);

        int total = 0;
        for (int i = 0; i < restaurantMenuList.size(); i++) {
            total = total + Integer.parseInt(restaurantMenuList.get(i).getMenu_price()) * restaurantMenuList.get(i).getQuantity();
        }
        textCartValue.setText("\u20B9 " + String.valueOf(total));

        llCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentFoodCart = new Intent(MenuActivity.this, FoodCartActivity.class);
                intentFoodCart.putExtra("selectedMenus", (Serializable) restaurantMenuList);
                intentFoodCart.putExtra("CGST", CGST);
                intentFoodCart.putExtra("SGST", SGST);
                startActivity(intentFoodCart);
            }
        });
    }

    public int getTotal(List<RestaurantMenu> list) {

        int total = 0;
        for (int i = 0; i < list.size(); i++) {
            total = total + Integer.parseInt(list.get(i).getMenu_price());
        }
        return total;
    }



    public List<RestaurantMenu> getArrayList(String key) {
        SharedPreferences prefs = getSharedPreferences(AppConstants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        addDataOnMenuSelection();
        restaurantMenus = MenuKartDatabase.getDatabase(MenuActivity.this).menuKartDao().getAllByRestaurantId(restaurantId);
        menuAdapter.updateList(restaurantMenus);
    }

    @Override
    public void addDataOnMenuSelection() {
        restaurantMenus = MenuKartDatabase.getDatabase(context).menuKartDao().getAllByRestaurantId(restaurantId);
        menuAdapter.updateList(restaurantMenus);
        restaurantMenuList = MenuKartDatabase.getDatabase(context).menuKartDao().getAllAddedItems();
        Log.i("addDataOnMenuSelectio:", restaurantMenuList.toString());

        if(!restaurantMenuList.isEmpty()){
            showItemCart();
        }else {
            llCart.setVisibility(View.GONE);
        }
    }
}