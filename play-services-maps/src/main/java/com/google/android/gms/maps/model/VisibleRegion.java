/*
 * Copyright (C) 2013-2017 microG Project Team
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

package com.google.android.gms.maps.model;

import org.microg.gms.common.PublicApi;
import org.microg.safeparcel.AutoSafeParcelable;
import org.microg.safeparcel.SafeParceled;

@PublicApi
public class VisibleRegion extends AutoSafeParcelable {
    @SafeParceled(1)
    private int versionCode;
    @SafeParceled(2)
    private LatLng nearLeft;
    @SafeParceled(3)
    private LatLng nearRight;
    @SafeParceled(4)
    private LatLng farLeft;
    @SafeParceled(5)
    private LatLng farRight;
    @SafeParceled(6)
    private LatLngBounds bounds;

    private VisibleRegion() {
    }

    public VisibleRegion(int versionCode, LatLng nearLeft, LatLng nearRight, LatLng farLeft,
            LatLng farRight, LatLngBounds bounds) {
        this.versionCode = versionCode;
        this.nearLeft = nearLeft;
        this.nearRight = nearRight;
        this.farLeft = farLeft;
        this.farRight = farRight;
        this.bounds = bounds;
    }

    public VisibleRegion(LatLng nearLeft, LatLng nearRight, LatLng farLeft, LatLng farRight,
            LatLngBounds bounds) {
        this(1, nearLeft, nearRight, farLeft, farRight, bounds);
    }

    /**
     * This is assuming that the visible region matches the bounds, which means that it's a north
     * orientated top view
     */
    public VisibleRegion(LatLngBounds bounds) {
        this(getSouthwest(bounds),
             new LatLng(getLat(getSouthwest(bounds)), getLon(getNortheast(bounds))),
             new LatLng(getLat(getNortheast(bounds)), getLon(getSouthwest(bounds))),
             getNortheast(bounds),
             bounds);
    }

    private static double getLat(Object latLng) {
        if (latLng == null) return 0;
        try {
            java.lang.reflect.Field f = latLng.getClass().getField("latitude");
            return f.getDouble(latLng);
        } catch (Throwable ignored) {}
        try {
            java.lang.reflect.Method m = latLng.getClass().getMethod("getLatitude");
            return (Double) m.invoke(latLng);
        } catch (Throwable ignored) {}
        try {
            for (java.lang.reflect.Field f : latLng.getClass().getDeclaredFields()) {
                if (f.getType() == double.class) {
                    f.setAccessible(true);
                    return f.getDouble(latLng);
                }
            }
        } catch (Throwable ignored) {}
        return 0;
    }

    private static double getLon(Object latLng) {
        if (latLng == null) return 0;
        try {
            java.lang.reflect.Field f = latLng.getClass().getField("longitude");
            return f.getDouble(latLng);
        } catch (Throwable ignored) {}
        try {
            java.lang.reflect.Method m = latLng.getClass().getMethod("getLongitude");
            return (Double) m.invoke(latLng);
        } catch (Throwable ignored) {}
        try {
            int count = 0;
            for (java.lang.reflect.Field f : latLng.getClass().getDeclaredFields()) {
                if (f.getType() == double.class) {
                    count++;
                    if (count == 2) {
                        f.setAccessible(true);
                        return f.getDouble(latLng);
                    }
                }
            }
        } catch (Throwable ignored) {}
        return 0;
    }

    private static LatLng getSouthwest(Object bounds) {
        if (bounds == null) return new LatLng(0, 0);
        try {
            java.lang.reflect.Field f = bounds.getClass().getField("southwest");
            return (LatLng) f.get(bounds);
        } catch (Throwable ignored) {}
        try {
            for (java.lang.reflect.Field f : bounds.getClass().getDeclaredFields()) {
                if (LatLng.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    return (LatLng) f.get(bounds);
                }
            }
        } catch (Throwable ignored) {}
        return new LatLng(0, 0);
    }

    private static LatLng getNortheast(Object bounds) {
        if (bounds == null) return new LatLng(0, 0);
        try {
            java.lang.reflect.Field f = bounds.getClass().getField("northeast");
            return (LatLng) f.get(bounds);
        } catch (Throwable ignored) {}
        try {
            int count = 0;
            for (java.lang.reflect.Field f : bounds.getClass().getDeclaredFields()) {
                if (LatLng.class.isAssignableFrom(f.getType())) {
                    count++;
                    if (count == 2) {
                        f.setAccessible(true);
                        return (LatLng) f.get(bounds);
                    }
                }
            }
        } catch (Throwable ignored) {}
        return new LatLng(0, 0);
    }

    public static Creator<VisibleRegion> CREATOR = new AutoCreator<VisibleRegion>(VisibleRegion.class);
}
