// Copyright 2024 Espressif Systems (Shanghai) PTE LTD
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.espressif.ui.activities;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.espressif.AppConstants;
import com.espressif.wifi_provisioning.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView tvAppVersion = findViewById(R.id.tv_app_version);

        String version = "";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String appVersion = getString(R.string.app_version) + " - v" + version;
        tvAppVersion.setText(appVersion);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Note : Overriding this method to make ActionBar "Back" button working.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        SwitchPreferenceCompat securityPref;
        EditTextPreference userNamePrefWifi, userNamePrefThread, userNamesRnetWifiSsid, userNamessRnetWifiPass, userNamessRnetMQTTAddr;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            PreferenceManager prefMgr = getPreferenceManager();
            prefMgr.setSharedPreferencesName(AppConstants.ESP_PREFERENCES);
            setPreferencesFromResource(R.xml.preferences, rootKey);

            SharedPreferences sharedPreferences = prefMgr.getSharedPreferences();
            securityPref = prefMgr.findPreference(AppConstants.KEY_SECURITY_TYPE);
            userNamePrefWifi = prefMgr.findPreference(AppConstants.KEY_USER_NAME_WIFI);
            userNamePrefThread = prefMgr.findPreference(AppConstants.KEY_USER_NAME_THREAD);
            userNamesRnetWifiSsid = prefMgr.findPreference(AppConstants.sRnet_WIFI_SSID);
            userNamessRnetWifiPass = prefMgr.findPreference(AppConstants.sRnet_WIFI_PASSWORD);
            userNamessRnetMQTTAddr = prefMgr.findPreference(AppConstants.sRnet_MQTT_ADDR);

            boolean isSecure = sharedPreferences.getBoolean(AppConstants.KEY_SECURITY_TYPE, true);
            if (isSecure) {
                securityPref.setSummary(R.string.summary_secured);
                userNamePrefWifi.setVisible(true);
                userNamePrefThread.setVisible(true);
            } else {
                securityPref.setSummary(R.string.summary_unsecured);
                userNamePrefWifi.setVisible(false);
                userNamePrefThread.setVisible(false);
            }
            userNamesRnetWifiSsid.setVisible(true);
            userNamessRnetWifiPass.setVisible(true);
            userNamessRnetMQTTAddr.setVisible(true);

            if (userNamessRnetWifiPass != null) {
                userNamessRnetWifiPass.setSummaryProvider(preference -> {
                    String password = sharedPreferences.getString(AppConstants.sRnet_WIFI_PASSWORD, "");
                    if (!password.isEmpty()) {
                        return "********"; // Hide actual password
                    } else {
                        return "No password set";
                    }
                });
            }

            // Set listener to mask the password in the EditText dialog
            userNamessRnetWifiPass.setOnPreferenceClickListener(preference -> {
                userNamessRnetWifiPass.setOnBindEditTextListener(editText -> {
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                });
                return true;
            });

            securityPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean isSecure = (boolean) newValue;

                    if (isSecure) {
                        preference.setSummary(R.string.summary_secured);
                        userNamePrefWifi.setVisible(true);
                        userNamePrefThread.setVisible(true);
                    } else {
                        preference.setSummary(R.string.summary_unsecured);
                        userNamePrefWifi.setVisible(false);
                        userNamePrefThread.setVisible(false);
                    }
                    userNamesRnetWifiSsid.setVisible(true);
                    userNamessRnetWifiPass.setVisible(true);
                    userNamessRnetMQTTAddr.setVisible(true);
                    return true;
                }
            });
        }
    }
}