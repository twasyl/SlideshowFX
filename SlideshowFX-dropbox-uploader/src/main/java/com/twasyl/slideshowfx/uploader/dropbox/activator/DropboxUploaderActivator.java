/*
 * Copyright 2014 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.uploader.dropbox.activator;

import com.twasyl.slideshowfx.uploader.IUploader;
import com.twasyl.slideshowfx.uploader.dropbox.DropboxUploader;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;

/**
 * Activator class for the uploader that allows to upload presentations on Dropbox.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class DropboxUploaderActivator implements BundleActivator {
    private DropboxUploader uploader;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        this.uploader = new DropboxUploader();
        Hashtable<String, String> props = new Hashtable<>();

        bundleContext.registerService(IUploader.class.getName(), this.uploader, props);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        if(this.uploader.isAuthenticated()) this.uploader.disconnect();
    }
}
