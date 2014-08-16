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

package com.twasyl.slideshowfx.io;

import java.io.File;
import java.io.FileFilter;

/**
 * This interface provides default {@link java.io.FileFilter} used in SlideshowFX.
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public interface SlideshowFXFileFilter {

    public static final FileFilter IMAGE_FILTER = new FileFilter() {
        private final String[] extensions = new String[] { ".png", ".bmp", ".gif", ".jpg", ".jpeg" };

        @Override
        public boolean accept(File pathname) {
            boolean accept = false;

            int index = 0;
            while(!accept && index < extensions.length) {
                accept = pathname.getName().endsWith(extensions[index++]);
            }

            return accept;
        }
    };
}
