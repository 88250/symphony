/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2019, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.util;

import org.b3log.symphony.model.Article;
import org.elasticsearch.index.query.QueryBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;


/**
 * Author: Zhang Yu
 * Date: 17年8月22日
 * Email: yu.zhang@7fresh.com
 */
public class ElasticsearchTestCase {
    @Test
    public void QueryDslBuilder(){
        QueryBuilder qb = multiMatchQuery("test",
                "test1","test2");
        System.out.println(qb.toString());
    }

    @Test
    public void QueryDslJSONBuilder(){
        final JSONObject reqData = new JSONObject();
        final JSONObject query = new JSONObject();
        final JSONObject bool = new JSONObject();
        query.put("bool",bool);
        final JSONObject must = new JSONObject();
        bool.put("must",must);
        final JSONObject queryString = new JSONObject();
        must.put("query_string",queryString);
        queryString.put("query","test");
        queryString.put("fields",new String[]{Article.ARTICLE_TITLE,Article.ARTICLE_CONTENT});
        queryString.put("default_operator","and");
        reqData.put("query", query);
        reqData.put("from", 0);
        reqData.put("size", 20);
        final JSONArray sort = new JSONArray();
        final JSONObject sortField = new JSONObject();
        sort.put(sortField);
        sortField.put(Article.ARTICLE_CREATE_TIME, "desc");
        sort.put("_score");
        reqData.put("sort", sort);

        System.out.println(reqData.toString());
    }
}
