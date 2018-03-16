package com.infoskillstechnology.settingsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.widget.Toast;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.EmptyRequestListener;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;

import javax.annotation.Nonnull;

public class SettingsActivity extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = SettingsActivity.class.getSimpleName();
    public static final String SOUND_OFF_ON = "sound_off_on";
    public static final String REMOVE_ADS = "remove_ads";
    private SwitchPreference removeAdsPrefs;

    private ActivityCheckout mCheckout;
    private boolean mAdFree = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        addPreferencesFromResource(R.xml.pref_notification);
        removeAdsPrefs = (SwitchPreference) findPreference(REMOVE_ADS);

        final Billing billing = SettingsApp.get(this).getBilling();
        mCheckout = Checkout.forActivity(this, billing);
        mCheckout.start();
        mCheckout.loadInventory(Inventory.Request.create().loadAllPurchases(), new InventoryCallback());
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCheckout.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void buyAdFree() {
        mCheckout.startPurchaseFlow(ProductTypes.IN_APP, REMOVE_ADS, null, new PurchaseListener());
    }

    @Override
    protected void onDestroy() {
        mCheckout.stop();
        super.onDestroy();
    }

    private class PurchaseListener extends EmptyRequestListener<Purchase> {
        @Override
        public void onSuccess(@Nonnull Purchase purchase) {
            hideAds();
        }
    }

    private class InventoryCallback implements Inventory.Callback {
        @Override
        public void onLoaded(@Nonnull Inventory.Products products) {
            final Inventory.Product product = products.get(ProductTypes.IN_APP);
            if (!product.supported) {
                // billing is not supported, user can't purchase anything. Don't show ads in this
                // case
                return;
            }
            if (product.isPurchased(REMOVE_ADS)) {
                return;
            }
            showAds();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
        removeAdsPrefsHide(sharedPreferences, REMOVE_ADS, removeAdsPrefs);

    }

    private void removeAdsPrefsHide(SharedPreferences sharedPreferences, String removeAds, SwitchPreference removeAdsPrefs) {
        if (!sharedPreferences.getBoolean(removeAds, false)) {
            removeAdsPrefs.setEnabled(false);
            PreferenceScreen screen = getPreferenceScreen();
            screen.removePreference(removeAdsPrefs);
        }
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SOUND_OFF_ON)) {
            SwitchPreference exercisesPref = (SwitchPreference) findPreference(key);
            exercisesPref.setChecked(sharedPreferences.getBoolean(key, false));
            Toast.makeText(this, "sharedPreferences " + sharedPreferences.getBoolean(key, false), Toast.LENGTH_SHORT).show();
        } else if (key.equals(REMOVE_ADS)) {
            SwitchPreference exercisesPref = (SwitchPreference) findPreference(key);
            exercisesPref.setChecked(sharedPreferences.getBoolean(key, false));
            buyAdFree();
//            removeAdsPrefsHide(sharedPreferences, key, exercisesPref);
            Toast.makeText(this, "sharedPreferences " + sharedPreferences.getBoolean(key, false), Toast.LENGTH_SHORT).show();
        }


    }


    private void hideAds(){
        mAdFree = true;
        PreferenceScreen screen = getPreferenceScreen();
        screen.removePreference(removeAdsPrefs);
    }


    private void showAds(){
        mAdFree = true;
        PreferenceScreen screen = getPreferenceScreen();
        screen.addPreference(removeAdsPrefs);
    }
}
