package com.example.matonique;

import android.content.Context;
import android.os.Environment;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class EspressoUtils {
    // methode pour copier un fichier des assets du test vers le dossier Music du tel
    public static void copyTestAsset(String assetName) {
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        File destDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        if (!destDir.exists()) destDir.mkdirs();

        File destFile = new File(destDir, assetName);

        try (InputStream in = testContext.getAssets().open(assetName);
             OutputStream out = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyAllTestsAssets(){
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        try {
            // on recupere tout ce qu'il y a dans les assets du test
            String[] assets = testContext.getAssets().list("");
            if (assets != null) {
                for (String asset : assets) {
                    if (asset.endsWith(".mp3") || asset.endsWith(".wav") ||
                        asset.endsWith(".flac") || asset.endsWith(".m4a")) {
                        copyTestAsset(asset);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
