/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
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
package org.b3log.symphony;

import org.b3log.latke.repository.jdbc.util.JdbcRepositories;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Database reverse generation case.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Sep 1, 2018
 * @since 3.4.0
 */
public class RepositoryJSONGen {

    public static void main(final String[] args) {
        JdbcRepositories.initRepositoryJSON("symphony_", new HashSet<>(Arrays.asList("article")), "reverse-repository.json");
    }
}
