package in.menukart.menukart.model.foodcart.trackorder;

import java.util.Map;

import in.menukart.menukart.api.APIClientCallback;
import in.menukart.menukart.api.ApiClient;
import in.menukart.menukart.entities.setting.ResponseSuccess;
import in.menukart.menukart.presenter.foodcart.trackorder.UpdateOrderStatusPresenterImp;

public class UpdateOrderStatusModelImp implements UpdateOrderStatusModel {

    private ApiClient apiClient;
    private APIClientCallback<ResponseSuccess> callback;

    public UpdateOrderStatusModelImp(UpdateOrderStatusPresenterImp callback, ApiClient client) {
        this.callback = (APIClientCallback<ResponseSuccess>) callback;
        this.apiClient = client;
    }

    @Override
    public void updateOrderStatus(Map<String, String> params) {
        apiClient.userUpdateOrderStatus(callback, params);

    }

}
