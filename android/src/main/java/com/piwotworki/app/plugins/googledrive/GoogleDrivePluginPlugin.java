package com.piwotworki.app.plugins.googledrive;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.File;
import java.io.IOException;

@CapacitorPlugin(name = "GoogleDrivePlugin")
public class GoogleDrivePluginPlugin extends Plugin {

    private GoogleDrivePlugin implementation = new GoogleDrivePlugin();

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");
        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    @PluginMethod
    public void storeRecipes(PluginCall call) {

        try {
            String recipesJson = call.getString("recipesJson");
            String authToken = call.getString("authToken");
            File dumpFile = File.createTempFile("recipes_", ".dump", getContext().getCacheDir());
            String retVal = implementation.storeRecipes(recipesJson, dumpFile, authToken);
            JSObject ret = new JSObject();
            ret.put("status", retVal);
            call.resolve(ret);
        } catch(IOException ex) {
            JSObject ret = new JSObject();
            ret.put("status", "IOException: " + ex.getMessage());
            call.resolve(ret);
        }
    }
}
