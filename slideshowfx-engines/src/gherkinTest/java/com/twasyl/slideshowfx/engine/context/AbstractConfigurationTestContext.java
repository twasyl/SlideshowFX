package com.twasyl.slideshowfx.engine.context;

import com.twasyl.slideshowfx.engine.IConfiguration;
import com.twasyl.slideshowfx.utils.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AbstractConfigurationTestContext<C extends IConfiguration> {
    protected C configuration;

    public void clean() {
        try {
            IOUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir")));
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(FINE, "Error when trying to clean context", e);
        }
    }

    protected void assertConfigurationNotNull() {
        assertNotNull(this.configuration);
    }
}
