package com.piwotworki.app.plugins.googledrive;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@CapacitorPlugin(name = "GoogleDrivePlugin")
public class GoogleDrivePluginPlugin extends Plugin {
    private final GoogleDrivePlugin implementation = new GoogleDrivePlugin();
    @PluginMethod
    public void hasDataOnDrive(PluginCall call) {
        try {
            String authToken = call.getString("authToken");
            Object[] retVal = implementation.hasAppDataOnDrive(authToken);

            JSObject ret = new JSObject();
            ret.put("result", retVal[0]);
            ret.put("status", retVal[1]);
            call.resolve(ret);
        } catch (IOException ex) {
            JSObject ret = new JSObject();
            ret.put("status", "IOException: " + ex.getMessage());
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void storeAppData(PluginCall call) {
        try {
            String appData = call.getString("appData");
            String syncState = call.getString("syncState");
            String authToken = call.getString("authToken");
            String appName = call.getString("appName");

            File appDataDumpFile = File.createTempFile("appData_", ".dump", getContext().getCacheDir());
            String storeAppDataStatus = implementation.storeAppData(appData, appDataDumpFile, authToken, appName);

            File syncStateDumpFile = File.createTempFile("syncState_", ".dump", getContext().getCacheDir());
            String syncStateStatus = implementation.storeSyncData(syncState, syncStateDumpFile, authToken, appName);

            JSObject ret = new JSObject();
            if (Objects.equals(storeAppDataStatus, "OK") && Objects.equals(syncStateStatus, "OK")) {
                ret.put("status", "OK");
            }
            else{
                ret.put("status", storeAppDataStatus + " | " + syncStateStatus);
            }

            call.resolve(ret);
        } catch (IOException ex) {
            JSObject ret = new JSObject();
            ret.put("status", "IOException: " + ex.getMessage());
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void fetchAppData(PluginCall call) {
        try {
            String authToken = call.getString("authToken");
            String appName = call.getString("appName");
            String[] retVal = implementation.fetchAppData(authToken, appName);

            JSObject ret = new JSObject();
            ret.put("appData", retVal[0]);
            ret.put("status", retVal[1]);
            call.resolve(ret);
        } catch (IOException ex) {
            JSObject ret = new JSObject();
            ret.put("status", "IOException: " + ex.getMessage());
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void fetchSyncData(PluginCall call) {
        try {
            String authToken = call.getString("authToken");
            String appName = call.getString("appName");
            String[] retVal = implementation.fetchSyncState(authToken, appName);
            String existing = "";
            if (Objects.equals(retVal[1], "OK")) {
                existing = retVal[0];
            }

            JSObject ret = new JSObject();
            ret.put("syncState", existing);
            ret.put("status", retVal[1]);
            call.resolve(ret);
        } catch (IOException ex) {
            JSObject ret = new JSObject();
            ret.put("status", "IOException: " + ex.getMessage());
            call.resolve(ret);
        }
    }
}
