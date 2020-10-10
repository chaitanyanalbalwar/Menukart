package in.menukart.menukart.view.foodcart.cart;

import android.annotation.SuppressLint;
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
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.menukart.menukart.R;
import in.menukart.menukart.db.MenuKartDatabase;
import in.menukart.menukart.entities.foodcart.FoodCart;
import in.menukart.menukart.entities.order.RestaurantMenu;
import in.menukart.menukart.model.order.discount.MenusModel;
import in.menukart.menukart.model.order.menu.MenuModel;
import in.menukart.menukart.util.AppConstants;
import in.menukart.menukart.view.foodcart.signup.LoginActivity;
import in.menukart.menukart.view.order.OrderRecord;

public class FoodCartActivity extends AppCompatActivity {

    List<RestaurantMenu> restaurantMenuList = new ArrayList<>();
    Context context;
    RecyclerView rcvFoodCart;
    AppCompatEditText editPromoCode;
    AppCompatButton btnApplyPromo, btnProceedToCheckout;
    AppCompatTextView textFoodCartTotal, textFoodTax,
            textFoodDeliveryCharges, textPromoDiscount,
            textSubTotal;
    String foodCartTotal, foodCartTax, foodDeliveryCharges, foodPromoDisCount, foodSubTotal;
    double total = 0;
    double gst = 5.0;
    String menuAddedQty = "1";
    String menuRemovedQty = "1";
    private FoodCartAdapter foodCartAdapter;

    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;
    String CGST,SGST;



    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_cart);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        context = FoodCartActivity.this;
       /* restaurantMenuList = (List<RestaurantMenu>) getIntent().
                getSerializableExtra("selectedMenus");*/
        restaurantMenuList = MenuKartDatabase.getDatabase(context).menuKartDao().getAllAddedItems();

        SharedPreferences sharedPreferences = getSharedPreferences(AppConstants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        CGST = sharedPreferences.getString(AppConstants.CGST,"");
        SGST = sharedPreferences.getString(AppConstants.SGST,"");

        initCartViews();
        loadCartViews();
        foodCartItemsData();


    }

    private void initCartViews() {
        editPromoCode = findViewById(R.id.et_food_promo_code);
        textFoodCartTotal = findViewById(R.id.tv_food_cart_total);
        textFoodTax = findViewById(R.id.tv_food_cart_tax);
        textFoodDeliveryCharges = findViewById(R.id.tv_food_delivery_charge);
        textPromoDiscount = findViewById(R.id.tv_food_promo_discount);
        textSubTotal = findViewById(R.id.tv_food_cart_sub_total);
        btnApplyPromo = findViewById(R.id.btn_promo_code_apply);
        btnProceedToCheckout = findViewById(R.id.btn_proceed_to_checkout);
    }

    @SuppressLint("SetTextI18n")
    private void loadCartViews() {
        total = 0;
        if (restaurantMenuList != null) {
            for (int i = 0; i < restaurantMenuList.size(); i++) {
                total = total + Double.parseDouble(restaurantMenuList.get(i).getMenu_price()) * restaurantMenuList.get(i).getQuantity();
            }

            try {

                foodCartTotal = String.valueOf(total);
                textFoodCartTotal.setText("\u20B9 " + String.valueOf(total));
                double amount = total;
                double tax = (amount / 100.0f) * Double.valueOf(CGST);
                foodCartTax = new DecimalFormat("##.##").format(tax);
                textFoodTax.setText("\u20B9 " + new DecimalFormat("##.##").format(tax));
                foodDeliveryCharges = "0.00";
                textFoodDeliveryCharges.setText("\u20B9 0.00");
                foodPromoDisCount = "-0.00";
                textPromoDiscount.setText("\u20B9 -0.00");
                double totalAmount = total + tax;
                foodSubTotal = String.valueOf(totalAmount);
                textSubTotal.setText("\u20B9 " + String.valueOf(totalAmount));

            }catch (Exception e){
                e.printStackTrace();
                Log.e("Cgst","CGST amout is null..bcoz of shared pref...clear data.");
            }


        }

        btnProceedToCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try{

                saveArrayList(restaurantMenuList, AppConstants.SELECTED_MENU_LIST);
                FoodCart foodCart = new FoodCart();
                foodCart.setCartSubTotal(foodCartTotal);
                foodCart.setTotalCgst(CGST);
                foodCart.setTotalSgst(SGST);
                foodCart.setCharges(foodDeliveryCharges);
                foodCart.setTotal(foodSubTotal);
                if (!"1".equals(menuAddedQty)) {
                    foodCart.setQty(menuAddedQty);
                } else if (!"1".equals(menuRemovedQty)) {
                    foodCart.setQty(menuRemovedQty);
                } else {
                    foodCart.setQty("1");
                }

                Gson gson = new Gson();
                String json = gson.toJson(foodCart);

                SharedPreferences sharedPreferences = getSharedPreferences(AppConstants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString(AppConstants.FOOD_CART_VALUES, json);
                editor.apply();

                Intent intentLogin = new Intent(FoodCartActivity.this, LoginActivity.class);
                startActivity(intentLogin);
                finish();

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        btnApplyPromo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getDiscount();
            }
        });
    }

    private void foodCartItemsData() {
        rcvFoodCart = findViewById(R.id.recycler_view_food_cart);
        //  rcvFoodCart.addItemDecoration(new DividerItemDecoration(FoodCartActivity.this, LinearLayoutManager.VERTICAL));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL, false);
        rcvFoodCart.setLayoutManager(linearLayoutManager);
        rcvFoodCart.setHasFixedSize(true);

        foodCartAdapter = new FoodCartAdapter(restaurantMenuList, context);
        rcvFoodCart.setAdapter(foodCartAdapter);
        foodCartAdapter.notifyDataSetChanged();

    }

    public void saveArrayList(List<RestaurantMenu> list, String key) {
        SharedPreferences prefs = getSharedPreferences(AppConstants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();

    }

    @SuppressLint("SetTextI18n")
    public void setDataOnAddItem(String addedMenuPrice, String addedMenuQty) {
        total = total + Integer.parseInt(addedMenuPrice);
        foodCartTotal = String.valueOf(total);
        textFoodCartTotal.setText("\u20B9 " + String.valueOf(total));
        double amount = total;
        double tax = (amount / 100.0f) * gst;
        foodCartTax = new DecimalFormat("##.##").format(tax);
        textFoodTax.setText("\u20B9 " + new DecimalFormat("##.##").format(tax));
        foodDeliveryCharges = "0.00";
        textFoodDeliveryCharges.setText("\u20B9 0.00");
        foodPromoDisCount = "-0.00";
        textPromoDiscount.setText("\u20B9 -0.00");
        double totalAmount = total + tax;
        foodSubTotal = String.valueOf(totalAmount);
        textSubTotal.setText("\u20B9 " + String.valueOf(totalAmount));
        menuAddedQty = addedMenuQty;

    }

    @SuppressLint("SetTextI18n")
    public void setDataOnRemoveItem(String removedMenuPrice, String removedMenuQty) {
        total = total - Integer.parseInt(removedMenuPrice);
        foodCartTotal = String.valueOf(total);
        textFoodCartTotal.setText("\u20B9 " + String.valueOf(total));
        double amount = total;
        double tax = (amount / 100.0f) * gst;
        foodCartTax = new DecimalFormat("##.##").format(tax);
        textFoodTax.setText("\u20B9 " + new DecimalFormat("##.##").format(tax));
        foodDeliveryCharges = "0.00";
        textFoodDeliveryCharges.setText("\u20B9 0.00");
        foodPromoDisCount = "-0.00";
        textPromoDiscount.setText("\u20B9 -0.00");
        double totalAmount = total + tax;
        foodSubTotal = String.valueOf(totalAmount);
        textSubTotal.setText("\u20B9 " + String.valueOf(totalAmount));
        menuRemovedQty = removedMenuQty;

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

    public void updateInvoice(List<RestaurantMenu> restaurantMenus){
        this.restaurantMenuList = restaurantMenus;
        OrderRecord.restaurantMenus = restaurantMenus;
        loadCartViews();
        if(total == 0){
            btnProceedToCheckout.setEnabled(false);
            btnProceedToCheckout.setBackgroundColor(getResources().getColor(R.color.colorGray));
        }
    }

    public void getDiscount(){

        mRequestQueue = Volley.newRequestQueue(this);

        StringRequest sr = new StringRequest(Request.Method.POST,"http://api.menukart.online/discount_coupon", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("resp",response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error", String.valueOf(error));
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("coupon","king20");
                params.put("subtotal","400");
                params.put("totalall", "500");
                params.put("restaurant_id","3");

                MenusModel menusModel=new MenusModel();
                menusModel.setMenu_id("9");
                menusModel.setQty("2");

                params.put("menus", String.valueOf(menusModel));
                params.put("categories",String.valueOf(menusModel));
                params.put("ordertype","delivery");

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };

        mRequestQueue.add(sr);
    }
}