package com.nearby.whatsnearby.presenters;

import com.nearby.whatsnearby.interfaces.IPermissionPresenter;
import com.nearby.whatsnearby.interfaces.IPermissionView;

public class PermissionPresenter implements IPermissionPresenter {

    private IPermissionView mView;

    public PermissionPresenter(IPermissionView view) {
        this.mView = view;
    }

    @Override
    public void initUI() {
        mView.notifyUIReady();
    }

    @Override
    public void askPermissionsToGrant() {
        mView.grantPermissions();
    }

    @Override
    public void navigateToMainScreen() {
        mView.navigateToHomeScreen();
    }
}