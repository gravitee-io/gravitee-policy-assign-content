/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
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
 */
package io.gravitee.policy.assigncontent.freemarker;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class CustomTemplateLoaderTest {

    private CustomTemplateLoader cut;

    @Before
    public void before() {
        cut = new CustomTemplateLoader();
    }

    @Test
    public void shouldPutInCache() {
        cut.putIfAbsent("test", "template body");
        assertEquals(1, cut.getCache().size());
    }

    @Test
    public void shouldNotPutInCacheIfAlreadyIn() {
        cut.putIfAbsent("test", "template body");
        cut.putIfAbsent("test", "template body");
        assertEquals(1, cut.getCache().size());
    }

    @Test
    public void shouldFind() {
        cut.putIfAbsent("test", "template body");
        assertNotNull(cut.findTemplateSource("test"));
    }

    @Test
    public void shouldNotFindIfNothingInCache() {
        assertNull(cut.findTemplateSource("test"));
    }
}
