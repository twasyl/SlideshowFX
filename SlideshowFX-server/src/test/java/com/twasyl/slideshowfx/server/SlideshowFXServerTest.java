/*
 * Copyright 2016 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.server;

import com.twasyl.slideshowfx.utils.NetworkUtils;
import io.vertx.ext.unit.TestContext;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * @author Thierry Wasylczenko
 */
@RunWith(io.vertx.ext.unit.junit.VertxUnitRunner.class)
public class SlideshowFXServerTest {

    @BeforeClass
    public static void setUp() {
        SlideshowFXServer.create(NetworkUtils.getIP(), 10080, null);
        SlideshowFXServer.getSingleton().start();
    }
}
