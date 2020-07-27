package in.menukart.menukart.view.order.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;

import java.util.List;

import in.menukart.menukart.CartUpdates;
import in.menukart.menukart.R;
import in.menukart.menukart.db.MenuKartDatabase;
import in.menukart.menukart.entities.order.RestaurantMenu;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.CategoryViewHolder> {
    Context context;
    private List<RestaurantMenu> restaurantMenus;
    private boolean checkValue = false;
    private RestaurantMenu addedRestaurantMenu;
    private CartUpdates cartUpdates;

    public MenuAdapter(List<RestaurantMenu> restaurantMenus, Context context , CartUpdates cartUpdates) {
        this.restaurantMenus = restaurantMenus;
        this.context = context;
        this.cartUpdates = cartUpdates;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //inflate the layout file
        View groceryProductView = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_list_item, parent, false);
        CategoryViewHolder categoryViewHolder = new CategoryViewHolder(groceryProductView);
        return categoryViewHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final CategoryViewHolder holder, final int position) {
        String imgUrl = "http://admin.menukart.online/uploads/menu/" + restaurantMenus.get(position).getMenu_logo();
        addedRestaurantMenu = restaurantMenus.get(position);

        Glide.with(context)
                .load(imgUrl)
                .centerCrop()
                // .placeholder(R.drawable.ic_loader_food)
                .into(holder.imgMenu);

        holder.textMenuName.setText(addedRestaurantMenu.getMenu_name());
        holder.textMenuVegNonVeg.setText(addedRestaurantMenu.getMenu_foodtype());
        holder.textMenuCost.setText("\u20B9 " + addedRestaurantMenu.getMenu_price());

        if(addedRestaurantMenu.isAddedToCart()){
            holder.elegantNumberButton.setVisibility(View.VISIBLE);
            holder.btnAddMenu.setVisibility(View.INVISIBLE);
            holder.elegantNumberButton.setNumber(""+addedRestaurantMenu.getQuantity());
        }else {
            holder.elegantNumberButton.setVisibility(View.INVISIBLE);
            holder.btnAddMenu.setVisibility(View.VISIBLE);
        }

        holder.btnAddMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkValue = true;
                addedRestaurantMenu.setQuantity(1);
                addedRestaurantMenu.setIsAdded(true);
                //TODO move it into thread
                updateItem();

                holder.btnAddMenu.setVisibility(View.INVISIBLE);
                holder.elegantNumberButton.setVisibility(View.VISIBLE);
                holder.elegantNumberButton.setNumber("1");

                cartUpdates.addDataOnMenuSelection();
            }
        });

        holder.elegantNumberButton.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                    if(newValue == 0){
                        holder.elegantNumberButton.setVisibility(View.INVISIBLE);
                        holder.btnAddMenu.setVisibility(View.VISIBLE);
                        addedRestaurantMenu.setIsAdded(false);
                    }
                    addedRestaurantMenu.setQuantity(newValue);
                    updateItem();
                    cartUpdates.addDataOnMenuSelection();
            }
        });
    }

    public void updateItem(){
        new Thread(){
            @Override
            public void run() {
                MenuKartDatabase.getDatabase(context).menuKartDao().updateItem(addedRestaurantMenu.restaurant_id,
                        addedRestaurantMenu.menu_id, addedRestaurantMenu.quantity, addedRestaurantMenu.isAddedToCart);
            }
        }.start();
    }

    @Override
    public int getItemCount() {
        return restaurantMenus.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView imgMenu;
        AppCompatTextView textMenuName, textMenuVegNonVeg, textMenuCost,
                textAdd, textMenuCount, textRemove;
        AppCompatButton btnAddMenu;
        RelativeLayout rlMenuAddRemoveItem;
        ElegantNumberButton elegantNumberButton;

        public CategoryViewHolder(View view) {
            super(view);
            imgMenu = view.findViewById(R.id.iv_menu_promo);
            textMenuName = view.findViewById(R.id.tv_menu_name);
            textMenuVegNonVeg = view.findViewById(R.id.tv_menu_veg_non_veg);
            textMenuCost = view.findViewById(R.id.tv_menu_cost);
            btnAddMenu = view.findViewById(R.id.btn_menu_add);
            elegantNumberButton = view.findViewById(R.id.number_button);
        }
    }

    public void updateList(List<RestaurantMenu> restaurantMenus){
        this.restaurantMenus.clear();
        this.restaurantMenus.addAll(restaurantMenus);
        notifyDataSetChanged();
    }

}
