/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.ui;

import com.athaydes.automaton.FXApp;
import com.athaydes.automaton.FXer;
import com.athaydes.automaton.Speed;
import com.twasyl.slideshowfx.app.SlideshowFX;
import javafx.scene.Node;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 *
 *  @author Thierry Wasyczenko
 *  @version 1.0
 *  @since SlideshowFX 1.0.0
 */
public class SlideshowFXTest {

    private static FXer fxer;

    @BeforeClass
    public static void setUp() {
        System.setProperty("dynamic.java.library.path", "true");
        System.setProperty("project.stage", "test");

        final Thread thread = new Thread(() -> FXApp.launch(SlideshowFX.class));
        thread.setDaemon(true);
        thread.start();

        while(!FXApp.isInitialized()) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        fxer = FXer.getUserWith(FXApp.getScene().getRoot());
    }

    /**
     * Test the presence of the MenuBar in the application.
     */
    @Test public void testMenuBar() {
        final Node menuBar = fxer.getAt("#menuBar");

        assertNotNull(menuBar);
        assertTrue(menuBar instanceof MenuBar);
    }

    /**
     * Test that the menu bar contains four entries:
     * <ul>
     *     <li>File</li>
     *     <li>Options</li>
     *     <li>Tools</li>
     *     <li>?</li>
     * </ul>
     */
    @Test(dependsOnMethods = "testMenuBar") public void testMenuBarContent() {
        final MenuBar menuBar = (MenuBar) fxer.getAt("#menuBar");

        assertEquals(menuBar.getMenus().size(), 4);

        int menuIndex = 0;
        MenuItem menuItem = menuBar.getMenus().get(menuIndex++);
        assertNotNull(menuItem);
        assertEquals(menuItem.getText(), "File");

        menuItem = menuBar.getMenus().get(menuIndex++);
        assertNotNull(menuItem);
        assertEquals(menuItem.getText(), "Options");

        menuItem = menuBar.getMenus().get(menuIndex++);
        assertNotNull(menuItem);
        assertEquals(menuItem.getText(), "Tools");

        menuItem = menuBar.getMenus().get(menuIndex++);
        assertNotNull(menuItem);
        assertEquals(menuItem.getText(), "?");
    }

    @Test(dependsOnMethods = "testMenuBarContent") public void quitApp() {
        fxer.clickOn("#fileMenu", Speed.VERY_FAST);
        fxer.clickOn("#quitMenuItem", Speed.VERY_FAST);
    }
}
