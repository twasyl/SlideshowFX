package com.twasyl.slideshowfx.uploader;

import com.twasyl.slideshowfx.osgi.OSGiManager;

import java.util.List;

/**
 * @author Thierry Wasylczenko
 */
public class UploaderManager {

    public static List<IUploader> getInstalledUploaders() { return OSGiManager.getInstalledServices(IUploader.class); }
}
