package com.twasyl.slideshowfx.utils.beans.binding;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.StringBinding;

/**
 * This provides a binding for a {@link javafx.beans.property.BooleanProperty} to be converted to the {@code *} if its
 * value is {@code true} and an empty string otherwhise.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class WildcardBinding extends StringBinding {

    private BooleanExpression booleanExpression;

    /**
     * Construct a {@link StringBinding} returning a String according the value of a {@link BooleanExpression}.
     * @param booleanExpression The property to bind to and create the binding for.
     * @throws java.lang.NullPointerException If the given {@code file} is null.
     */
    public WildcardBinding(BooleanExpression booleanExpression) {
        if(booleanExpression == null) throw new NullPointerException("The property can not be null");

        this.booleanExpression = booleanExpression;
        super.bind(this.booleanExpression);
    }

    /**
     * Convert the value of the {@link BooleanExpression} to a String.
     * @return An empty String if the value if not {@code true}, {@code *} otherwise.
     */
    @Override
    protected String computeValue() {
        return this.booleanExpression.get() == false ? "" : "*";
    }

    @Override
    public void dispose() {
        super.unbind(this.booleanExpression);
    }
}
