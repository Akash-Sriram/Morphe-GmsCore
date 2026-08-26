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

package org.microg.gms.maps.vtm.camera;

import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.dynamic.IObjectWrapper;
import com.google.android.gms.dynamic.ObjectWrapper;
import com.google.android.gms.maps.internal.ICameraUpdateFactoryDelegate;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.microg.gms.maps.vtm.GmsMapsTypeHelper;
import org.oscim.core.MapPosition;
import org.oscim.map.Map;

public class CameraUpdateFactoryImpl extends ICameraUpdateFactoryDelegate.Stub {
    private static final String TAG = "GmsMapCamUpdateFactory";

    private CameraUpdateFactoryImpl() {

    }

    private static CameraUpdateFactoryImpl instance;
    public static CameraUpdateFactoryImpl get() {
        if (instance == null) {
            instance = new CameraUpdateFactoryImpl();
        }
        return instance;
    }

    @Override
    public IObjectWrapper zoomIn() throws RemoteException {
        Log.d(TAG, "zoomIn");
        return new ObjectWrapper<CameraUpdate>(new MapPositionCameraUpdate() {

            @Override
            MapPosition getMapPosition(Map map) {
                MapPosition mapPosition = map.getMapPosition();
                mapPosition.setScale(GmsMapsTypeHelper.fromZoom(
                        GmsMapsTypeHelper.toZoom(mapPosition.getScale()) + 1));
                return mapPosition;
            }
        });
    }

    @Override
    public IObjectWrapper zoomOut() throws RemoteException {
        Log.d(TAG, "zoomOut");
        return new ObjectWrapper<CameraUpdate>(new MapPositionCameraUpdate() {
            @Override
            MapPosition getMapPosition(Map map) {
                MapPosition mapPosition = map.getMapPosition();
                mapPosition.setScale(GmsMapsTypeHelper.fromZoom(
                        GmsMapsTypeHelper.toZoom(mapPosition.getScale()) - 1));
                return mapPosition;
            }
        });
    }

    @Override
    public IObjectWrapper scrollBy(final float x, final float y) throws RemoteException {
        Log.d(TAG, "scrollBy");
        return new ObjectWrapper<CameraUpdate>(new MapPositionCameraUpdate() {
            @Override
            MapPosition getMapPosition(Map map) {
                MapPosition mapPosition = map.getMapPosition();
                mapPosition.setPosition(map.viewport()
                        .fromScreenPoint((float) (map.getWidth() / 2.0 + x),
                                (float) (map.getHeight() / 2.0 + y)));
                return mapPosition;
            }
        });
    }

    @Override
    public IObjectWrapper zoomTo(final float zoom) throws RemoteException {
        Log.d(TAG, "zoomTo");
        return new ObjectWrapper<CameraUpdate>(new MapPositionCameraUpdate() {
            @Override
            MapPosition getMapPosition(Map map) {
                MapPosition mapPosition = map.getMapPosition();
                mapPosition.setScale(GmsMapsTypeHelper.fromZoom(zoom));
                return mapPosition;
            }
        });
    }

    @Override
    public IObjectWrapper zoomBy(final float zoomDelta) throws RemoteException {
        Log.d(TAG, "zoomBy");
        return new ObjectWrapper<CameraUpdate>(new MapPositionCameraUpdate() {
            @Override
            MapPosition getMapPosition(Map map) {
                MapPosition mapPosition = map.getMapPosition();
                mapPosition.setScale(GmsMapsTypeHelper.fromZoom(
                        GmsMapsTypeHelper.toZoom(mapPosition.getScale()) + zoomDelta));
                return mapPosition;
            }
        });
    }

    @Override
    public IObjectWrapper zoomByWithFocus(final float zoomDelta, int x, int y)
            throws RemoteException {
        Log.d(TAG, "zoomByWithFocus");
        return new ObjectWrapper<CameraUpdate>(new MapPositionCameraUpdate() {
            @Override
            MapPosition getMapPosition(Map map) {
                MapPosition mapPosition = map.getMapPosition();
                mapPosition.setScale(GmsMapsTypeHelper.fromZoom(
                        GmsMapsTypeHelper.toZoom(mapPosition.getScale()) + zoomDelta));
                Log.w(TAG, "zoomBy with focus not yet supported"); // TODO
                return mapPosition;
            }
        });
    }

    @Override
    public IObjectWrapper newCameraPosition(final CameraPosition cameraPosition)
            throws RemoteException {
        Log.d(TAG, "newCameraPosition");
        return new ObjectWrapper<CameraUpdate>(new MapPositionCameraUpdate() {
            @Override
            MapPosition getMapPosition(Map map) {
                return GmsMapsTypeHelper.fromCameraPosition(cameraPosition);
            }
        });
    }

    @Override
    public IObjectWrapper newLatLng(final LatLng latLng) throws RemoteException {
        Log.d(TAG, "newLatLng");
        return new ObjectWrapper<CameraUpdate>(new MapPositionCameraUpdate() {
            @Override
            MapPosition getMapPosition(Map map) {
                MapPosition mapPosition = map.getMapPosition();
                mapPosition.setPosition(GmsMapsTypeHelper.fromLatLng(latLng));
                return mapPosition;
            }
        });
    }

    @Override
    public IObjectWrapper newLatLngZoom(final LatLng latLng, final float zoom)
            throws RemoteException {
        Log.d(TAG, "newLatLngZoom");
        return new ObjectWrapper<CameraUpdate>(new MapPositionCameraUpdate() {
            @Override
            MapPosition getMapPosition(Map map) {
                MapPosition mapPosition = map.getMapPosition();
                mapPosition.setPosition(GmsMapsTypeHelper.fromLatLng(latLng));
                mapPosition.setScale(GmsMapsTypeHelper.fromZoom(zoom));
                return mapPosition;
            }
        });
    }

    @Override
    public IObjectWrapper newLatLngBounds(final LatLngBounds bounds, int padding) throws RemoteException {
        Log.d(TAG, "newLatLngBounds");
        return new ObjectWrapper<CameraUpdate>(new MapPositionCameraUpdate() {
            @Override
            MapPosition getMapPosition(Map map) {
                MapPosition mapPosition = map.getMapPosition();
                mapPosition.setByBoundingBox(GmsMapsTypeHelper.fromLatLngBounds(bounds),
                        map.getWidth(), map.getHeight());
                return mapPosition;
            }
        });
    }

    @Override
    public IObjectWrapper newLatLngBoundsWithSize(final LatLngBounds bounds, final int width, final int height, int padding)
            throws RemoteException {
        Log.d(TAG, "newLatLngBoundsWithSize");
        return new ObjectWrapper<CameraUpdate>(new MapPositionCameraUpdate() {
            @Override
            MapPosition getMapPosition(Map map) {
                MapPosition mapPosition = map.getMapPosition();
                mapPosition.setByBoundingBox(GmsMapsTypeHelper.fromLatLngBounds(bounds),
                        width, height);
                return mapPosition;
            }
        });
    }

    private static final String DESCRIPTOR = "com.google.android.gms.maps.internal.ICameraUpdateFactoryDelegate";
    private static final int TRANSACTION_zoomIn = FIRST_CALL_TRANSACTION + 0;
    private static final int TRANSACTION_zoomOut = FIRST_CALL_TRANSACTION + 1;
    private static final int TRANSACTION_scrollBy = FIRST_CALL_TRANSACTION + 2;
    private static final int TRANSACTION_zoomTo = FIRST_CALL_TRANSACTION + 3;
    private static final int TRANSACTION_zoomBy = FIRST_CALL_TRANSACTION + 4;
    private static final int TRANSACTION_zoomByWithFocus = FIRST_CALL_TRANSACTION + 5;
    private static final int TRANSACTION_newCameraPosition = FIRST_CALL_TRANSACTION + 6;
    private static final int TRANSACTION_newLatLng = FIRST_CALL_TRANSACTION + 7;
    private static final int TRANSACTION_newLatLngZoom = FIRST_CALL_TRANSACTION + 8;
    private static final int TRANSACTION_newLatLngBounds = FIRST_CALL_TRANSACTION + 9;
    private static final int TRANSACTION_newLatLngBoundsWithSize = FIRST_CALL_TRANSACTION + 10;

    @SuppressWarnings("unchecked")
    private static <T> T readParcelableSafely(android.os.Parcel data, String className) {
        if (data.readInt() == 0) return null;
        try {
            Class<?> clazz = Class.forName(className);
            android.os.Parcelable.Creator<?> creator = (android.os.Parcelable.Creator<?>) clazz.getField("CREATOR").get(null);
            return (T) creator.createFromParcel(data);
        } catch (Throwable t) {
            Log.w(TAG, "Failed to read parcelable: " + className, t);
            return null;
        }
    }

    @Override
    public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws RemoteException {
        if (code == TRANSACTION_zoomIn) {
            data.enforceInterface(DESCRIPTOR);
            IObjectWrapper result = this.zoomIn();
            reply.writeNoException();
            reply.writeStrongBinder(result != null ? result.asBinder() : null);
            return true;
        } else if (code == TRANSACTION_zoomOut) {
            data.enforceInterface(DESCRIPTOR);
            IObjectWrapper result = this.zoomOut();
            reply.writeNoException();
            reply.writeStrongBinder(result != null ? result.asBinder() : null);
            return true;
        } else if (code == TRANSACTION_scrollBy) {
            data.enforceInterface(DESCRIPTOR);
            float arg0 = data.readFloat();
            float arg1 = data.readFloat();
            IObjectWrapper result = this.scrollBy(arg0, arg1);
            reply.writeNoException();
            reply.writeStrongBinder(result != null ? result.asBinder() : null);
            return true;
        } else if (code == TRANSACTION_zoomTo) {
            data.enforceInterface(DESCRIPTOR);
            float arg0 = data.readFloat();
            IObjectWrapper result = this.zoomTo(arg0);
            reply.writeNoException();
            reply.writeStrongBinder(result != null ? result.asBinder() : null);
            return true;
        } else if (code == TRANSACTION_zoomBy) {
            data.enforceInterface(DESCRIPTOR);
            float arg0 = data.readFloat();
            IObjectWrapper result = this.zoomBy(arg0);
            reply.writeNoException();
            reply.writeStrongBinder(result != null ? result.asBinder() : null);
            return true;
        } else if (code == TRANSACTION_zoomByWithFocus) {
            data.enforceInterface(DESCRIPTOR);
            float arg0 = data.readFloat();
            int arg1 = data.readInt();
            int arg2 = data.readInt();
            IObjectWrapper result = this.zoomByWithFocus(arg0, arg1, arg2);
            reply.writeNoException();
            reply.writeStrongBinder(result != null ? result.asBinder() : null);
            return true;
        } else if (code == TRANSACTION_newCameraPosition) {
            data.enforceInterface(DESCRIPTOR);
            CameraPosition arg0 = readParcelableSafely(data, "com.google.android.gms.maps.model.CameraPosition");
            IObjectWrapper result = this.newCameraPosition(arg0);
            reply.writeNoException();
            reply.writeStrongBinder(result != null ? result.asBinder() : null);
            return true;
        } else if (code == TRANSACTION_newLatLng) {
            data.enforceInterface(DESCRIPTOR);
            LatLng arg0 = readParcelableSafely(data, "com.google.android.gms.maps.model.LatLng");
            IObjectWrapper result = this.newLatLng(arg0);
            reply.writeNoException();
            reply.writeStrongBinder(result != null ? result.asBinder() : null);
            return true;
        } else if (code == TRANSACTION_newLatLngZoom) {
            data.enforceInterface(DESCRIPTOR);
            LatLng arg0 = readParcelableSafely(data, "com.google.android.gms.maps.model.LatLng");
            float arg1 = data.readFloat();
            IObjectWrapper result = this.newLatLngZoom(arg0, arg1);
            reply.writeNoException();
            reply.writeStrongBinder(result != null ? result.asBinder() : null);
            return true;
        } else if (code == TRANSACTION_newLatLngBounds) {
            data.enforceInterface(DESCRIPTOR);
            LatLngBounds arg0 = readParcelableSafely(data, "com.google.android.gms.maps.model.LatLngBounds");
            int arg1 = data.readInt();
            IObjectWrapper result = this.newLatLngBounds(arg0, arg1);
            reply.writeNoException();
            reply.writeStrongBinder(result != null ? result.asBinder() : null);
            return true;
        } else if (code == TRANSACTION_newLatLngBoundsWithSize) {
            data.enforceInterface(DESCRIPTOR);
            LatLngBounds arg0 = readParcelableSafely(data, "com.google.android.gms.maps.model.LatLngBounds");
            int arg1 = data.readInt();
            int arg2 = data.readInt();
            int arg3 = data.readInt();
            IObjectWrapper result = this.newLatLngBoundsWithSize(arg0, arg1, arg2, arg3);
            reply.writeNoException();
            reply.writeStrongBinder(result != null ? result.asBinder() : null);
            return true;
        }
        return super.onTransact(code, data, reply, flags);
    }
}
