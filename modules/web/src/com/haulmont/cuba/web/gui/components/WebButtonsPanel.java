/*
 * Copyright (c) 2008-2016 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.components.ButtonsPanel;
import com.haulmont.cuba.gui.components.VisibilityChangeNotifier;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class WebButtonsPanel extends WebHBoxLayout implements ButtonsPanel, VisibilityChangeNotifier {
    public static final String BUTTONS_PANNEL_STYLENAME = "c-buttons-panel";

    protected List<VisibilityChangeListener> visibilityChangeListeners;

    public WebButtonsPanel() {
        setSpacing(true);
        setMargin(false);

        component.addStyleName(BUTTONS_PANNEL_STYLENAME);
    }

    @Override
    public void setStyleName(String name) {
        super.setStyleName(name);

        component.addStyleName(BUTTONS_PANNEL_STYLENAME);
    }

    @Override
    public String getStyleName() {
        return StringUtils.normalizeSpace(super.getStyleName().replace(BUTTONS_PANNEL_STYLENAME, ""));
    }

    @Override
    public void addVisibilityChangeListener(VisibilityChangeListener listener) {
        if (visibilityChangeListeners == null) {
            visibilityChangeListeners = new LinkedList<>();
        }

        if (!visibilityChangeListeners.contains(listener)) {
            visibilityChangeListeners.add(listener);
        }
    }

    @Override
    public void removeVisibilityChangeListener(VisibilityChangeListener listener) {
        if (!visibilityChangeListeners.contains(listener)) {
            visibilityChangeListeners.remove(listener);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (visibilityChangeListeners != null) {
            VisibilityChangeEvent event = new VisibilityChangeEvent(this, visible);
            for (VisibilityChangeListener listener : new ArrayList<>(visibilityChangeListeners)) {
                listener.componentVisibilityChanged(event);
            }
        }
    }
}