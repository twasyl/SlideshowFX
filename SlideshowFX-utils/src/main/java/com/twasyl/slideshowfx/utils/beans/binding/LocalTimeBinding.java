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

package com.twasyl.slideshowfx.utils.beans.binding;

import javafx.beans.binding.ObjectExpression;
import javafx.beans.binding.StringBinding;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * This binding returns the time each time a {@link java.time.LocalTime} changes.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class LocalTimeBinding extends StringBinding {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);
    private final ObjectExpression<LocalTime> time;

    public LocalTimeBinding(final ObjectExpression<LocalTime> time) {
        if(time == null) throw new NullPointerException("The time can not be null");

        this.time = time;
        super.bind(this.time);
    }

    @Override
    protected String computeValue() {
        return time.get() == null ? "" : time.get().format(formatter);
    }

    @Override
    public void dispose() {
        super.unbind(this.time);
    }
}
