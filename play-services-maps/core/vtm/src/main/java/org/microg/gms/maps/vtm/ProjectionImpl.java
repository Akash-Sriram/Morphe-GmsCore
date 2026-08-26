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

import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.dynamic.IObjectWrapper;
import com.google.android.gms.dynamic.ObjectWrapper;
import com.google.android.gms.maps.internal.IProjectionDelegate;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;

import org.oscim.core.Point;
import org.oscim.map.Viewport;

public class ProjectionImpl extends IProjectionDelegate.Stub {
    private static final String TAG = "GmsProjectionImpl";
    private static final String DESCRIPTOR = "com.google.android.gms.maps.internal.IProjectionDelegate";
    private static final int TRANSACTION_fromScreenLocation = FIRST_CALL_TRANSACTION + 0;
    private static final int TRANSACTION_toScreenLocation = FIRST_CALL_TRANSACTION + 1;
    private static final int TRANSACTION_getVisibleRegion = FIRST_CALL_TRANSACTION + 2;
    private Viewport viewport;
    private float[] extents = new float[8];

    public ProjectionImpl(Viewport viewport) {
        this.viewport = viewport;
    }

    @Override
    public LatLng fromScreenLocation(IObjectWrapper obj) throws RemoteException {
        Point point = GmsMapsTypeHelper
                .fromPoint((android.graphics.Point) ObjectWrapper.unwrap(obj));
        return GmsMapsTypeHelper
                .toLatLng(viewport.fromScreenPoint((float) point.x, (float) point.y));
    }

    @Override
    public IObjectWrapper toScreenLocation(LatLng latLng) throws RemoteException {
        Point point = new Point();
        viewport.toScreenPoint(GmsMapsTypeHelper.fromLatLng(latLng), point);
        return ObjectWrapper.wrap(GmsMapsTypeHelper.toPoint(point));
    }

    @Override
    public VisibleRegion getVisibleRegion() throws RemoteException {
        viewport.getMapExtents(extents, 0);
        // TODO: Support non-flat map extents
        return new VisibleRegion(GmsMapsTypeHelper.toLatLngBounds(viewport.getBBox(null, 0)));
    }

    @Override
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code == TRANSACTION_toScreenLocation) {
            data.enforceInterface(DESCRIPTOR);
            LatLng latLng = null;
            if (data.readInt() != 0) {
                try {
                    Class<?> clazz = Class.forName("com.google.android.gms.maps.model.LatLng");
                    Parcelable.Creator<?> creator = (Parcelable.Creator<?>) clazz.getField("CREATOR").get(null);
                    latLng = (LatLng) creator.createFromParcel(data);
                } catch (Throwable t) {
                    Log.w(TAG, "Failed to read LatLng from parcel", t);
                }
            }
            IObjectWrapper result = this.toScreenLocation(latLng);
            reply.writeNoException();
            reply.writeStrongBinder(result != null ? result.asBinder() : null);
            return true;
        } else if (code == TRANSACTION_fromScreenLocation) {
            data.enforceInterface(DESCRIPTOR);
            IObjectWrapper arg0 = IObjectWrapper.Stub.asInterface(data.readStrongBinder());
            LatLng result = this.fromScreenLocation(arg0);
            reply.writeNoException();
            if (result != null) {
                reply.writeInt(1);
                result.writeToParcel(reply, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
            } else {
                reply.writeInt(0);
            }
            return true;
        } else if (code == TRANSACTION_getVisibleRegion) {
            data.enforceInterface(DESCRIPTOR);
            VisibleRegion result = this.getVisibleRegion();
            reply.writeNoException();
            if (result != null) {
                reply.writeInt(1);
                result.writeToParcel(reply, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
            } else {
                reply.writeInt(0);
            }
            return true;
        }
        return super.onTransact(code, data, reply, flags);
    }
}
