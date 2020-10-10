package in.menukart.menukart.entities.order;

import java.io.Serializable;

public class Discount implements Serializable {

    private String status,msg,discount_amount,totalall;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getDiscount_amount() {
        return discount_amount;
    }

    public void setDiscount_amount(String discount_amount) {
        this.discount_amount = discount_amount;
    }

    public String getTotalall() {
        return totalall;
    }

    public void setTotalall(String totalall) {
        this.totalall = totalall;
    }
}
