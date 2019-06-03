package gradle.cucumber.types;

import com.twasyl.slideshowfx.engine.template.configuration.SlideElementTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SlideTemplatesAndSlideElementTemplatesMapping extends HashMap<Integer, List<SlideElementTemplate>> {

    public void addSlideElement(final Integer slideTemplateId, final SlideElementTemplate slideElementTemplate) {
        if (slideElementTemplate != null) {
            if (!containsKey(slideTemplateId)) {
                put(slideTemplateId, new ArrayList<>());
            }

            get(slideTemplateId).add(slideElementTemplate);
        }
    }
}
