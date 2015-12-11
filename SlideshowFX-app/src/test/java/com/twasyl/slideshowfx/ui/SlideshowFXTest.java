/*
 * Copyright 2015 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.ui;

import com.twasyl.slideshowfx.app.SlideshowFX;
import org.junit.*;
import org.junit.rules.TestName;
import org.testfx.api.FxRobot;
import org.testfx.api.FxService;
import org.testfx.api.FxToolkit;
import org.testfx.service.support.CaptureSupport;

import java.io.File;

import static org.junit.Assert.assertNotNull;

/**
 *
 *  @author Thierry Wasyczenko
 *  @version 1.0
 *  @since SlideshowFX 1.0.0
 */
public class SlideshowFXTest {

    @Rule public TestName testName = new TestName();

    private static FxRobot fx;
    private static CaptureSupport capture;
    private static File testResultsDir;

    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty("dynamic.java.library.path", "true");
        System.setProperty("project.stage", "test");

        testResultsDir = new File(System.getProperty("testResultsDir", "build"));

        FxToolkit.registerPrimaryStage();
        FxToolkit.setupApplication(SlideshowFX.class);
        FxToolkit.showStage();

        fx = new FxRobot();
        assertNotNull(fx);

        capture = FxService.serviceContext().getCaptureSupport();
    }

    @Before public void clickOnUI() {
        fx.clickOn(".root");
    }

    @After public void takeScreenShot() throws InterruptedException {
        Thread.sleep(100);
        capture.capturePrimaryScreenToFile(new File(testResultsDir, testName.getMethodName() + ".png"));
    }

    @Test public void fileMenu() {
        fx.clickOn("#fileMenu");
    }

    @Test public void viewMenu() {
        fx.clickOn("#viewMenu");
    }

    @Test public void toolsMenu() {
        fx.clickOn("#toolsMenu");
    }

    @Test public void helpMenu() {
        fx.clickOn("#helpMenu");
    }

    @Test public void optionsMenu() {
        fx.clickOn("#optionsMenu");
    }
}
