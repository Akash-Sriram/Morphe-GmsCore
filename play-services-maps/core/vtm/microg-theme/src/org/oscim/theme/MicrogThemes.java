package org.oscim.theme;

import org.oscim.backend.AssetAdapter;

import java.io.InputStream;

public enum MicrogThemes implements ThemeFile {

    DEFAULT("styles/microg.xml");
    // TODO: night theme

    private final String mPath;

    MicrogThemes(String path) {
        mPath = path;
    }

    @Override
    public XmlRenderThemeMenuCallback getMenuCallback() {
        return null;
    }

    @Override
    public String getRelativePathPrefix() {
        return "";
    }

    @Override
    public InputStream getRenderThemeAsStream() {
        InputStream is = null;
        try {
            is = AssetAdapter.readFileAsStream(mPath);
        } catch (Throwable ignored) {}
        if (is == null) {
            try {
                is = getClass().getClassLoader().getResourceAsStream("assets/" + mPath);
            } catch (Throwable ignored) {}
        }
        if (is == null) {
            try {
                is = getClass().getClassLoader().getResourceAsStream(mPath);
            } catch (Throwable ignored) {}
        }
        return is;
    }

    @Override
    public void setMenuCallback(XmlRenderThemeMenuCallback menuCallback) {
    }

    @Override
    public boolean isMapsforgeTheme() {
        return false;
    }
}
