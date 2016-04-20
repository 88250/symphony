/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
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
package org.b3log.symphony.model;

/**
 * This class defines all revision model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Apr 20, 2016
 * @since 1.4.0
 */
public final class Revision {

    /**
     * Revision.
     */
    public static final String REVISION = "revision";

    /**
     * Revisions.
     */
    public static final String REVISIONS = "revisions";

    /**
     * Key of revision data type.
     */
    public static final String REVISION_DATA_TYPE = "revisionDataType";

    /**
     * Key of revision data id.
     */
    public static final String REVISION_DATA_ID = "revisionDataId";

    /**
     * Key of revision data.
     */
    public static final String REVISION_DATA = "revisionData";

    /**
     * Key of revision author id.
     */
    public static final String REVISION_AUTHOR_ID = "revisionAuthorId";

    // Data type constants
    /**
     * Data type - article.
     */
    public static final int DATA_TYPE_C_ARTICLE = 0;
}
