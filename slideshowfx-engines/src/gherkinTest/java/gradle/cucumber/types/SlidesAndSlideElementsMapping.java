package gradle.cucumber.types;

import com.twasyl.slideshowfx.engine.presentation.configuration.SlideElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SlidesAndSlideElementsMapping extends HashMap<String, List<SlideElement>> {

    public void addSlideElement(final String slideId, final SlideElement slideElement) {
        if (slideElement != null) {
            if (!containsKey(slideId)) {
                put(slideId, new ArrayList<>());
            }

            get(slideId).add(slideElement);
        }
    }
}
