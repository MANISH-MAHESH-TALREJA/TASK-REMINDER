
package net.manish.shopping.model.notification;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Result {

    @SerializedName("message_id")
    @Expose
    private String messageId;

    public Result() {
    }

}
