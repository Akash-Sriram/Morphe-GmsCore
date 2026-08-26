/*
 * Copyright (C) 2019 microG Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.microg.gms.maps.vtm;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;

import org.microg.gms.maps.vtm.data.SharedTileCache;
import org.microg.gms.maps.vtm.markup.ClearableVectorLayer;
import org.microg.gms.maps.vtm.R;
import org.oscim.android.AndroidAssets;
import org.oscim.android.MapView;
import org.oscim.android.canvas.AndroidBitmap;
import org.oscim.backend.AssetAdapter;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Layers;
import org.oscim.theme.MicrogThemes;
import org.oscim.tiling.ITileCache;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class BackendMapView extends MapView {
    private static final String TAG = "GmsMapView";

    private static boolean nativeLibLoaded = false;
    private LabelLayer labels;
    private BuildingLayer buildings;
    private ItemizedLayer<MarkerItem> items;
    private ClearableVectorLayer drawables;

    static synchronized Context loadNativeLib(Context context) {
        try {
            if (nativeLibLoaded) return context;
            ApplicationInfo otherAppInfo = context.getPackageManager().getApplicationInfo(context.getApplicationContext().getPackageName(), 0);

            String primaryCpuAbi = (String) ApplicationInfo.class.getField("primaryCpuAbi").get(otherAppInfo);
            if (primaryCpuAbi != null) {
                String path = "lib/" + primaryCpuAbi + "/libvtm-jni.so";
                File cacheFile = new File(context.getApplicationContext().getCacheDir().getAbsolutePath() + "/.gmscore/" + path);
                cacheFile.getParentFile().mkdirs();
                File apkFile = new File(context.getPackageCodePath());
                if (!cacheFile.exists() || cacheFile.lastModified() < apkFile.lastModified()) {
                    ZipFile zipFile = new ZipFile(apkFile);
                    ZipEntry entry = zipFile.getEntry(path);
                    if (entry != null) {
                        copyInputStream(zipFile.getInputStream(entry), new FileOutputStream(cacheFile));
                    } else {
                        Log.d(TAG, "Can't load native library: " + path + " does not exist in " + apkFile);
                    }
                }
                Log.d(TAG, "Loading vtm-jni from " + cacheFile.getPath());
                System.load(cacheFile.getAbsolutePath());
                nativeLibLoaded = true;
            }
        } catch (Exception e) {
            Log.w(TAG, e);
        }
        if (!nativeLibLoaded) {
            Log.d(TAG, "Loading native vtm-jni");
            System.loadLibrary("vtm-jni");
            nativeLibLoaded = true;
        }
        return context;
    }

    private static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);

        in.close();
        out.close();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private static class CustomAssetAdapter extends org.oscim.backend.AssetAdapter {
        private final Context context;
        private final ClassLoader classLoader;

        public CustomAssetAdapter(Context context) {
            this.context = context;
            this.classLoader = BackendMapView.class.getClassLoader();
        }

    @Override
    public InputStream openFileAsStream(String path) {
        if (context != null && context.getAssets() != null) {
            try {
                return context.getAssets().open(path);
            } catch (Throwable ignored) {}
        }
        if (classLoader != null) {
            try {
                InputStream is = classLoader.getResourceAsStream("assets/" + path);
                if (is != null) return is;
            } catch (Throwable ignored) {}
            try {
                InputStream is = classLoader.getResourceAsStream(path);
                if (is != null) return is;
            } catch (Throwable ignored) {}
        }
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl != null) {
                InputStream is = cl.getResourceAsStream("assets/" + path);
                if (is != null) return is;
                is = cl.getResourceAsStream(path);
                if (is != null) return is;
            }
        } catch (Throwable ignored) {}
        return null;
    }
}

    public BackendMapView(Context context) {
        super(loadNativeLib(context));
        try {
            AssetAdapter.init(new CustomAssetAdapter(context));
        } catch (Throwable t) {
            Log.w(TAG, "AssetAdapter.init failed", t);
        }
        initialize();
    }

    public BackendMapView(Context context, AttributeSet attributeSet) {
        super(loadNativeLib(context), attributeSet);
        try {
            AssetAdapter.init(new CustomAssetAdapter(context));
        } catch (Throwable t) {
            Log.w(TAG, "AssetAdapter.init failed", t);
        }
        initialize();
    }

    ItemizedLayer<MarkerItem> items() {
        return items;
    }

    BuildingLayer buildings() {
        return buildings;
    }

    ClearableVectorLayer drawables() {
        return drawables;
    }

    private static class StandardHttpEngine implements org.oscim.tiling.source.HttpEngine {
        private final org.oscim.tiling.source.UrlTileSource tileSource;
        private java.net.HttpURLConnection connection;
        private java.io.InputStream inputStream;
        private java.io.OutputStream cacheStream;

        public StandardHttpEngine(org.oscim.tiling.source.UrlTileSource tileSource) {
            this.tileSource = tileSource;
        }

        @Override
        public void sendRequest(org.oscim.core.Tile tile) throws java.io.IOException {
            String urlString = tileSource.getTileUrl(tile);
            java.net.URL url = new java.net.URL(urlString);
            connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0) Gecko/120.0 Firefox/120.0");
            int code = connection.getResponseCode();
            if (code == 200) {
                inputStream = connection.getInputStream();
            } else {
                throw new java.io.IOException("Tile request HTTP error: " + code);
            }
        }

        @Override
        public java.io.InputStream read() throws java.io.IOException {
            return inputStream;
        }

        @Override
        public void close() {
            if (inputStream != null) {
                try { inputStream.close(); } catch (Throwable ignored) {}
                inputStream = null;
            }
            if (connection != null) {
                connection.disconnect();
                connection = null;
            }
        }

        @Override
        public void setCache(java.io.OutputStream cacheStream) {
            this.cacheStream = cacheStream;
        }

        @Override
        public boolean requestCompleted(boolean success) {
            return success;
        }
    }

    private static class StandardHttpEngineFactory implements org.oscim.tiling.source.HttpEngine.Factory {
        @Override
        public org.oscim.tiling.source.HttpEngine create(org.oscim.tiling.source.UrlTileSource tileSource) {
            return new StandardHttpEngine(tileSource);
        }
    }

    private void initialize() {
        ITileCache cache = new SharedTileCache(getContext());
        cache.setCacheSize(512 * (1 << 10));
        org.oscim.tiling.source.bitmap.BitmapTileSource tileSource = org.oscim.tiling.source.bitmap.BitmapTileSource.builder()
                .url("https://a.tile.openstreetmap.fr/hot")
                .tilePath("/{Z}/{X}/{Y}.png")
                .build();
        tileSource.setCache(cache);
        tileSource.setHttpEngine(new StandardHttpEngineFactory());
        org.oscim.layers.tile.bitmap.BitmapTileLayer baseLayer = new org.oscim.layers.tile.bitmap.BitmapTileLayer(map(), tileSource);
        map().layers().add(0, baseLayer);
        Layers layers = map().layers();
        layers.add(drawables = new ClearableVectorLayer(map()));
        layers.add(items = new ItemizedLayer<MarkerItem>(map(), new MarkerSymbol(
                new AndroidBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.nop)), 0.5F, 1)));
    }
}
