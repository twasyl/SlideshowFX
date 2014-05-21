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

import javafx.stage.FileChooser;

public interface SlideshowFXExtensionFilter {

    public static FileChooser.ExtensionFilter TEMPLATE_FILTER = new FileChooser.ExtensionFilter("Template files", "*.sfxt");

    public static FileChooser.ExtensionFilter PRESENTATION_FILES = new FileChooser.ExtensionFilter("Presentation files", "*.sfx");

    public static FileChooser.ExtensionFilter PLUGIN_FILES = new FileChooser.ExtensionFilter("Plugin files", "*.jar");
}
