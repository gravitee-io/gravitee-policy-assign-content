/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.policy.assigncontent.utils;

import io.gravitee.gateway.api.http.HttpHeaders;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class HeaderMapAdapter implements Map<String, List<String>> {

    private final HttpHeaders headers;

    public HeaderMapAdapter(HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    public int size() {
        return headers.size();
    }

    @Override
    public boolean isEmpty() {
        return headers.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return headers.contains((String) key);
    }

    @Override
    public boolean containsValue(Object value) {
        throw new IllegalStateException();
    }

    @Override
    public List<String> get(Object key) {
        return headers.getAll((String) key);
    }

    @Override
    public List<String> put(String key, List<String> values) {
        throw new IllegalStateException();
    }

    @Override
    public List<String> remove(Object key) {
        List<String> oldValue = get(key);
        headers.remove((String) key);
        return oldValue;
    }

    @Override
    public void putAll(Map<? extends String, ? extends List<String>> m) {
        throw new IllegalStateException();
    }

    @Override
    public void clear() {
        throw new IllegalStateException();
    }

    @Override
    public Set<String> keySet() {
        throw new IllegalStateException();
    }

    @Override
    public Collection<List<String>> values() {
        throw new IllegalStateException();
    }

    @Override
    public Set<Entry<String, List<String>>> entrySet() {
        throw new IllegalStateException();
    }
}