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

package com.haulmont.cuba.core.sys;

import com.google.common.base.Strings;
import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.Resources;
import com.haulmont.cuba.security.entity.Permission;
import com.haulmont.cuba.security.entity.PermissionType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class is used for working with default permission values.
 * Default permission values is used when no role define an explicit value for permissions from
 * {@code cuba.defaultPermissionValuesConfig} files.
 */
@Component("cuba_DefaultPermissionValuesConfig")
public class DefaultPermissionValuesConfig {

    private final Logger log = LoggerFactory.getLogger(DefaultPermissionValuesConfig.class);

    protected ReadWriteLock lock = new ReentrantReadWriteLock();

    protected Map<String, Permission> permissionValues = new ConcurrentHashMap<>();

    protected volatile boolean initialized;

    @Inject
    protected Resources resources;

    @Inject
    protected Metadata metadata;

    protected void checkInitialized() {
        if (!initialized) {
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                if (!initialized) {
                    log.info("Initializing default permission values");
                    init();
                    initialized = true;
                }
            } finally {
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
        }
    }

    protected void init() {
        permissionValues.clear();

        String configName = AppContext.getProperty("cuba.defaultPermissionValuesConfig");
        if (!StringUtils.isBlank(configName)) {
            StrTokenizer tokenizer = new StrTokenizer(configName);
            for (String fileName : tokenizer.getTokenArray()) {
                parseConfigFile(fileName);
            }
        }
    }

    protected void parseConfigFile(String fileName) {
        String fileContent = resources.getResourceAsString(fileName);
        if (!Strings.isNullOrEmpty(fileContent)) {
            Document document = Dom4j.readDocument(fileContent);
            List<Element> permissionElements = Dom4j.elements(document.getRootElement(), "permission");

            for (Element element : permissionElements) {
                String target = element.attributeValue("target");
                Integer value = Integer.valueOf(element.attributeValue("value"));
                Integer type = Integer.valueOf(element.attributeValue("type"));
                Permission permission = metadata.create(Permission.class);
                permission.setTarget(target);
                permission.setType(PermissionType.fromId(type));
                permission.setValue(value);
                permissionValues.put(target, permission);
            }
        } else {
            log.error("File {} not found", fileName);
        }
    }

    public Map<String, Permission> getDefaultPermissionValues() {
        lock.readLock().lock();
        try {
            checkInitialized();
            return new HashMap<>(permissionValues);
        } finally {
            lock.readLock().unlock();
        }
    }
}