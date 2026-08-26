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

package org.microg.gms.maps.vtm.markup;

import android.os.RemoteException;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.internal.ITileOverlayDelegate;

public class TileOverlayImpl extends ITileOverlayDelegate.Stub {
    private String id;
    private float zIndex;
    private boolean visible = true;
    private boolean fadeIn = true;
    private float transparency = 0.0f;

    public TileOverlayImpl() {
        this.id = "to0";
    }

    public TileOverlayImpl(String id, TileOverlayOptions options) {
        this.id = id;
        if (options != null) {
            this.zIndex = options.getZIndex();
            this.visible = options.isVisible();
            this.fadeIn = options.getFadeIn();
            this.transparency = options.getTransparency();
        }
    }

    @Override
    public void remove() throws RemoteException {
    }

    @Override
    public void clearTileCache() throws RemoteException {
    }

    @Override
    public String getId() throws RemoteException {
        return id;
    }

    @Override
    public void setZIndex(float zIndex) throws RemoteException {
        this.zIndex = zIndex;
    }

    @Override
    public float getZIndex() throws RemoteException {
        return zIndex;
    }

    @Override
    public void setVisible(boolean visible) throws RemoteException {
        this.visible = visible;
    }

    @Override
    public boolean isVisible() throws RemoteException {
        return visible;
    }

    @Override
    public boolean equalsRemote(ITileOverlayDelegate other) throws RemoteException {
        return other != null && other.getId() != null && other.getId().equals(getId());
    }

    @Override
    public int hashCodeRemote() throws RemoteException {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public void setFadeIn(boolean fadeIn) throws RemoteException {
        this.fadeIn = fadeIn;
    }

    @Override
    public boolean getFadeIn() throws RemoteException {
        return fadeIn;
    }

    @Override
    public void setTransparency(float transparency) throws RemoteException {
        this.transparency = transparency;
    }

    @Override
    public float getTransparency() throws RemoteException {
        return transparency;
    }
}
