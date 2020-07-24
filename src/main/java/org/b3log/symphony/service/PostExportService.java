/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-present, b3log.org
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
package org.b3log.symphony.service;

import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import jodd.io.ZipUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.FileUploadProcessor;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.CommentRepository;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Post (article/comment) export service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.2, Apr 5, 2018
 * @since 1.4.0
 */
@Service
public class PostExportService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(PostExportService.class);

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Article repository.
     */
    @Inject
    private ArticleRepository articleRepository;

    /**
     * Comment repository.
     */
    @Inject
    private CommentRepository commentRepository;

    /**
     * Pointtransfer management service.
     */
    @Inject
    private PointtransferMgmtService pointtransferMgmtService;

    /**
     * Exports all posts of a user's specified with the given user id.
     *
     * @param userId the given user id
     * @return download URL, returns {@code "-1"} if in sufficient balance, returns {@code null} if other exceptions
     */
    public String exportPosts(final String userId) {
        final int pointDataExport = Symphonys.POINT_DATA_EXPORT;
        try {
            final JSONObject user = userRepository.get(userId);
            final int balance = user.optInt(UserExt.USER_POINT);

            if (balance - pointDataExport < 0) {
                return "-1";
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Checks user failed", e);

            return null;
        }

        final JSONArray posts = new JSONArray();

        Query query = new Query().setFilter(
                new PropertyFilter(Article.ARTICLE_AUTHOR_ID, FilterOperator.EQUAL, userId)).
                select(Keys.OBJECT_ID,
                        Article.ARTICLE_TITLE,
                        Article.ARTICLE_TAGS,
                        Article.ARTICLE_CONTENT,
                        Article.ARTICLE_CREATE_TIME);

        try {
            final List<JSONObject> articles = articleRepository.getList(query);
            for (final JSONObject article : articles) {
                final JSONObject post = new JSONObject();
                post.put("id", article.optString(Keys.OBJECT_ID));
                final JSONObject content = new JSONObject();
                content.put("title", article.optString(Article.ARTICLE_TITLE));
                content.put("tags", article.optString(Article.ARTICLE_TAGS));
                content.put("body", article.optString(Article.ARTICLE_CONTENT));
                post.put("content", content.toString());
                post.put("created", Article.ARTICLE_CREATE_TIME);
                post.put("type", "article");
                posts.put(post);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Export articles failed", e);
            return null;
        }

        query = new Query().setFilter(new PropertyFilter(Comment.COMMENT_AUTHOR_ID, FilterOperator.EQUAL, userId)).
                select(Keys.OBJECT_ID, Comment.COMMENT_CONTENT, Comment.COMMENT_CREATE_TIME);
        try {
            final List<JSONObject> comments = commentRepository.getList(query);
            for (final JSONObject comment : comments) {
                final JSONObject post = new JSONObject();
                post.put("id", comment.optString(Keys.OBJECT_ID));
                final JSONObject content = new JSONObject();
                content.put("title", "");
                content.put("tags", "");
                content.put("body", comment.optString(Comment.COMMENT_CONTENT));
                post.put("content", content.toString());
                post.put("created", Comment.COMMENT_CREATE_TIME);
                post.put("type", "comment");
                posts.put(post);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Export comments failed", e);
            return null;
        }

        LOGGER.info("Exporting posts [size=" + posts.length() + "]");

        final boolean succ = null != pointtransferMgmtService.transfer(userId, Pointtransfer.ID_C_SYS,
                Pointtransfer.TRANSFER_TYPE_C_DATA_EXPORT, Pointtransfer.TRANSFER_SUM_C_DATA_EXPORT,
                String.valueOf(posts.length()), System.currentTimeMillis(), "");
        if (!succ) {
            return null;
        }

        final String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String fileKey = "export-" + userId + "-" + uuid + ".zip";

        final String tmpDir = System.getProperty("java.io.tmpdir");
        String localFilePath = tmpDir + "/" + uuid + ".json";
        LOGGER.info(localFilePath);
        final File localFile = new File(localFilePath);

        try {
            final byte[] data = posts.toString(2).getBytes(StandardCharsets.UTF_8);

            try (final OutputStream output = new FileOutputStream(localFile)) {
                IOUtils.write(data, output);
            }

            final File zipFile = ZipUtil.zip(localFile);

            final FileInputStream inputStream = new FileInputStream(zipFile);
            final byte[] zipData = IOUtils.toByteArray(inputStream);

            if (Symphonys.QN_ENABLED) {
                final Auth auth = Auth.create(Symphonys.UPLOAD_QINIU_AK, Symphonys.UPLOAD_QINIU_SK);
                final UploadManager uploadManager = new UploadManager(new Configuration());
                uploadManager.put(zipData, fileKey, auth.uploadToken(Symphonys.UPLOAD_QINIU_BUCKET),
                        null, "application/zip", false);

                return Symphonys.UPLOAD_QINIU_DOMAIN + "/" + fileKey;
            } else {
                fileKey = FileUploadProcessor.genFilePath(fileKey);
                final String filePath = Symphonys.UPLOAD_LOCAL_DIR + fileKey;

                FileUtils.copyFile(zipFile, new File(filePath));

                return Latkes.getServePath() + "/upload/" + fileKey;
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Uploading exprted data failed", e);

            return null;
        }
    }
}
