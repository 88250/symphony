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
