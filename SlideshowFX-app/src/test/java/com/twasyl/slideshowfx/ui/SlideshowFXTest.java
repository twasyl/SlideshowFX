package com.twasyl.slideshowfx.ui;

import com.twasyl.slideshowfx.app.SlideshowFX;
import com.twasyl.slideshowfx.controllers.SlideshowFXController;
import javafx.event.ActionEvent;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.testfx.api.FxRobot;
import org.testfx.api.FxService;
import org.testfx.api.FxToolkit;
import org.testfx.service.support.CaptureSupport;

import java.io.File;
import java.lang.reflect.Method;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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


        // Do some mocking
        Class clazz = new ByteBuddy()
                .subclass(SlideshowFXController.class)
                .method(ElementMatchers.named("openPresentation").and(ElementMatchers.takesArguments(ActionEvent.class)).and(ElementMatchers.isPrivate()))
                .intercept(MethodCall
                                .invoke(String.class.getMethod("toString"))
                                .on("Thierry"))
                .make()
                .load(SlideshowFXController.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();

        Object instance = clazz.newInstance();

        Method m = clazz.getDeclaredMethod("openPresentation", ActionEvent.class);
        m.setAccessible(true);
        m.invoke(instance, null);

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

    /**
     * Take a screenshot of the screen. The screenshot will be taken and saved in the {@link #testResultsDir} with the
     * given name and the PNG extension.
     * @param screenshotName The name of the screenshot without the extension.
     * @throws InterruptedException
     */
    public void takeScreenShot(final String screenshotName) throws InterruptedException {
        File file = new File(testResultsDir, screenshotName + ".png");

        capture.capturePrimaryScreenToFile(file);
        assertTrue(file.exists());
    }

    @Test public void fileMenu() throws InterruptedException {
        fx.clickOn("#fileMenu");
        takeScreenShot(testName.getMethodName());
    }

    @Test public void viewMenu() throws InterruptedException {
        fx.clickOn("#viewMenu");
        takeScreenShot(testName.getMethodName());
    }

    @Test public void toolsMenu() throws InterruptedException {
        fx.clickOn("#toolsMenu");
        takeScreenShot(testName.getMethodName());
    }

    @Test public void helpMenu() throws InterruptedException {
        takeScreenShot(testName.getMethodName());
    }

    @Test public void optionsMenu() throws InterruptedException {
        takeScreenShot(testName.getMethodName());
    }

    @Test public void mainUIWithoutOpenedPresentation() throws InterruptedException {
        takeScreenShot(testName.getMethodName());
    }

    @Test public void mainUIWithOpenedPresentation() throws InterruptedException {
        fx.clickOn("#openPresentation");

        takeScreenShot(testName.getMethodName());
    }
}
