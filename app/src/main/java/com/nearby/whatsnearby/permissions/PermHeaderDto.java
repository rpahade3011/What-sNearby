package com.nearby.whatsnearby.permissions;

/**
 * Created by rudhraksh.pahade on 1/31/2017.
 */

public class PermHeaderDto {
    public String getPermName() {
        return permName;
    }

    public void setPermName(String permName) {
        this.permName = permName;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public int resId;
    public String permName;
}
