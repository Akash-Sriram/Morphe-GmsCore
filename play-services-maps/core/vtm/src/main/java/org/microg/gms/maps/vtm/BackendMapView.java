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
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.i(TAG, ">>> onSizeChanged: " + w + "x" + h + " (old: " + oldw + "x" + oldh + ")");
        if (map() != null && map().viewport() != null) {
            map().viewport().setScreenSize(w, h);
            map().updateMap(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, ">>> onResume MapView");
        if (map() != null) {
            map().updateMap(true);
        }
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
            InputStream is = null;
            if (context != null && context.getAssets() != null) {
                try {
                    is = context.getAssets().open(path);
                } catch (Throwable ignored) {}
            }
            if (is == null && classLoader != null) {
                try {
                    is = classLoader.getResourceAsStream("assets/" + path);
                } catch (Throwable ignored) {}
                if (is == null) {
                    try {
                        is = classLoader.getResourceAsStream(path);
                    } catch (Throwable ignored) {}
                }
            }
            if (is == null) {
                try {
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    if (cl != null) {
                        is = cl.getResourceAsStream("assets/" + path);
                        if (is == null) is = cl.getResourceAsStream(path);
                    }
                } catch (Throwable ignored) {}
            }
            Log.i("GmsMapView", "openFileAsStream: " + path + " -> " + (is != null ? "FOUND" : "NOT_FOUND"));
            return is;
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

    public static class CustomBitmapTileSource extends org.oscim.tiling.source.bitmap.BitmapTileSource {
        public CustomBitmapTileSource(String url, String tilePath, int zoomMin, int zoomMax) {
            super(url, tilePath, zoomMin, zoomMax);
        }

        @Override
        public org.oscim.tiling.ITileDataSource getDataSource() {
            return new org.oscim.tiling.ITileDataSource() {
                private boolean canceled = false;

                @Override
                public void query(org.oscim.layers.tile.MapTile mapTile, org.oscim.tiling.ITileDataSink sink) {
                    if (canceled) {
                        sink.completed(org.oscim.tiling.QueryResult.FAILED);
                        return;
                    }
                    String urlString = getTileUrl(mapTile);
                    Log.i(TAG, ">>> CustomBitmapTileSource fetching: " + urlString);
                    java.net.HttpURLConnection conn = null;
                    try {
                        java.net.URL url = new java.net.URL(urlString);
                        conn = (java.net.HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setConnectTimeout(15000);
                        conn.setReadTimeout(15000);
                        conn.setRequestProperty("User-Agent", "MorphePhotos/1.0 (Android; Map)");
                        int code = conn.getResponseCode();
                        if (code == 200) {
                            java.io.InputStream in = conn.getInputStream();
                            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                            byte[] buf = new byte[4096];
                            int n;
                            while ((n = in.read(buf)) != -1) {
                                baos.write(buf, 0, n);
                            }
                            in.close();
                            byte[] bytes = baos.toByteArray();
                            if (bytes.length > 0) {
                                BitmapFactory.Options opts = new BitmapFactory.Options();
                                opts.inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888;
                                android.graphics.Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
                                if (bmp != null) {
                                    sink.setTileImage(new AndroidBitmap(bmp));
                                    sink.completed(org.oscim.tiling.QueryResult.SUCCESS);
                                    Log.i(TAG, ">>> CustomBitmapTileSource SUCCESS for: " + urlString);
                                    return;
                                }
                            }
                        } else {
                            Log.e(TAG, ">>> HTTP error " + code + " for: " + urlString);
                        }
                    } catch (Throwable t) {
                        Log.e(TAG, ">>> Error fetching tile " + urlString, t);
                    } finally {
                        if (conn != null) {
                            try { conn.disconnect(); } catch (Throwable ignored) {}
                        }
                    }
                    sink.completed(org.oscim.tiling.QueryResult.FAILED);
                }

                @Override
                public void dispose() {
                    canceled = true;
                }

                @Override
                public void cancel() {
                    canceled = true;
                }
            };
        }
    }

    private void initialize() {
        Log.i("GmsMapView", ">>> initialize() starting MapView setup");
        ITileCache cache = new SharedTileCache(getContext());
        cache.setCacheSize(512 * (1 << 10));
        CustomBitmapTileSource tileSource = new CustomBitmapTileSource(
                "https://a.tile.openstreetmap.fr/hot",
                "/{Z}/{X}/{Y}.png",
                0,
                19
        );
        tileSource.setCache(cache);
        org.oscim.layers.tile.bitmap.BitmapTileLayer baseLayer = new org.oscim.layers.tile.bitmap.BitmapTileLayer(map(), tileSource);
        map().setBaseMap(baseLayer);
        Layers layers = map().layers();
        layers.add(drawables = new ClearableVectorLayer(map()));
        layers.add(items = new ItemizedLayer<MarkerItem>(map(), new MarkerSymbol(
                new AndroidBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.nop)), 0.5F, 1)));
        map().updateMap(true);
        Log.i("GmsMapView", ">>> initialize() completed");
    }
}
