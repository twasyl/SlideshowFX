package com.twasyl.slideshowfx.gradle.plugins.sfxrelease.extensions;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * Extension allowing to change the next version token and the product version.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class ReleaseExtension {
    private final Property<String> nextVersionToken;
    private final Property<String> productVersion;

    @Inject
    public ReleaseExtension(ObjectFactory objects) {
        this.nextVersionToken = objects.property(String.class);
        this.nextVersionToken.convention("@@NEXT-VERSION@@");

        this.productVersion = objects.property(String.class);
        this.productVersion.convention(System.getenv("PRODUCT_VERSION"));
    }

    public Property<String> getNextVersionToken() {
        return nextVersionToken;
    }

    public Property<String> getProductVersion() {
        return productVersion;
    }
}
