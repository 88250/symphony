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
package org.b3log.symphony.service;

import javax.inject.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Follow;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.FollowRepository;
import org.b3log.symphony.repository.TagRepository;
import org.json.JSONObject;

/**
 * Follow management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.0.1, Aug 12, 2015
 * @since 0.2.5
 */
@Service
public class FollowMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(FollowMgmtService.class.getName());

    /**
     * Follow repository.
     */
    @Inject
    private FollowRepository followRepository;

    /**
     * Tag repository.
     */
    @Inject
    private TagRepository tagRepository;

    /**
     * Article repository.
     */
    @Inject
    private ArticleRepository articleRepository;

    /**
     * The specified follower follows the specified following tag.
     *
     * @param followerId the specified follower id
     * @param followingTagId the specified following tag id
     * @throws ServiceException service exception
     */
    @Transactional
    public void followTag(final String followerId, final String followingTagId) throws ServiceException {
        try {
            follow(followerId, followingTagId, Follow.FOLLOWING_TYPE_C_TAG);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] follows a tag[id=" + followingTagId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * The specified follower follows the specified following user.
     *
     * @param followerId the specified follower id
     * @param followingUserId the specified following user id
     * @throws ServiceException service exception
     */
    @Transactional
    public void followUser(final String followerId, final String followingUserId) throws ServiceException {
        try {
            follow(followerId, followingUserId, Follow.FOLLOWING_TYPE_C_USER);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] follows a user[id=" + followingUserId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * The specified follower follows the specified following article.
     *
     * @param followerId the specified follower id
     * @param followingArticleId the specified following article id
     * @throws ServiceException service exception
     */
    @Transactional
    public void followArticle(final String followerId, final String followingArticleId) throws ServiceException {
        try {
            follow(followerId, followingArticleId, Follow.FOLLOWING_TYPE_C_ARTICLE);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] follows an article[id=" + followingArticleId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * The specified follower unfollows the specified following tag.
     *
     * @param followerId the specified follower id
     * @param followingTagId the specified following tag id
     * @throws ServiceException service exception
     */
    @Transactional
    public void unfollowTag(final String followerId, final String followingTagId) throws ServiceException {
        try {
            unfollow(followerId, followingTagId, Follow.FOLLOWING_TYPE_C_TAG);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] unfollows a tag[id=" + followingTagId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * The specified follower unfollows the specified following user.
     *
     * @param followerId the specified follower id
     * @param followingUserId the specified following user id
     * @throws ServiceException service exception
     */
    @Transactional
    public void unfollowUser(final String followerId, final String followingUserId) throws ServiceException {
        try {
            unfollow(followerId, followingUserId, Follow.FOLLOWING_TYPE_C_USER);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] unfollows a user[id=" + followingUserId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * The specified follower unfollows the specified following article.
     *
     * @param followerId the specified follower id
     * @param followingArticleId the specified following article id
     * @throws ServiceException service exception
     */
    @Transactional
    public void unfollowArticle(final String followerId, final String followingArticleId) throws ServiceException {
        try {
            unfollow(followerId, followingArticleId, Follow.FOLLOWING_TYPE_C_ARTICLE);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] unfollows an article[id=" + followingArticleId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * The specified follower follows the specified following entity with the specified following type.
     *
     * @param followerId the specified follower id
     * @param followingId the specified following entity id
     * @param followingType the specified following type
     * @throws RepositoryException repository exception
     */
    private void follow(final String followerId, final String followingId, final int followingType) throws RepositoryException {
        if (followRepository.exists(followerId, followingId)) {
            return;
        }

        if (Follow.FOLLOWING_TYPE_C_TAG == followingType) {
            final JSONObject tag = tagRepository.get(followingId);
            if (null == tag) {
                LOGGER.log(Level.ERROR, "Not found tag [id={0}] to follow", followingId);

                return;
            }

            tag.put(Tag.TAG_FOLLOWER_CNT, tag.optInt(Tag.TAG_FOLLOWER_CNT) + 1);
            tag.put(Tag.TAG_RANDOM_DOUBLE, Math.random());

            tagRepository.update(followingId, tag);
        } else if (Follow.FOLLOWING_TYPE_C_ARTICLE == followingType) {
            final JSONObject article = articleRepository.get(followingId);
            if (null == article) {
                LOGGER.log(Level.ERROR, "Not found article [id={0}] to follow", followingId);

                return;
            }

            article.put(Article.ARTICLE_COLLECT_CNT, article.optInt(Article.ARTICLE_COLLECT_CNT) + 1);

            articleRepository.update(followingId, article);
        }

        final JSONObject follow = new JSONObject();
        follow.put(Follow.FOLLOWER_ID, followerId);
        follow.put(Follow.FOLLOWING_ID, followingId);
        follow.put(Follow.FOLLOWING_TYPE, followingType);

        followRepository.add(follow);
    }

    /**
     * Removes a follow relationship.
     *
     * @param followerId the specified follower id
     * @param followingId the specified following entity id
     * @param followingType the specified following type
     * @throws RepositoryException repository exception
     */
    public void unfollow(final String followerId, final String followingId, final int followingType) throws RepositoryException {
        followRepository.removeByFollowerIdAndFollowingId(followerId, followingId);

        if (Follow.FOLLOWING_TYPE_C_TAG == followingType) {
            final JSONObject tag = tagRepository.get(followingId);
            if (null == tag) {
                LOGGER.log(Level.ERROR, "Not found tag [id={0}] to unfollow", followingId);

                return;
            }

            tag.put(Tag.TAG_FOLLOWER_CNT, tag.optInt(Tag.TAG_FOLLOWER_CNT) - 1);

            if (tag.optInt(Tag.TAG_FOLLOWER_CNT) < 0) {
                tag.put(Tag.TAG_FOLLOWER_CNT, 0);
            }
            
            tag.put(Tag.TAG_RANDOM_DOUBLE, Math.random());

            tagRepository.update(followingId, tag);
        } else if (Follow.FOLLOWING_TYPE_C_ARTICLE == followingType) {
            final JSONObject article = articleRepository.get(followingId);
            if (null == article) {
                LOGGER.log(Level.ERROR, "Not found article [id={0}] to unfollow", followingId);

                return;
            }

            article.put(Article.ARTICLE_COLLECT_CNT, article.optInt(Article.ARTICLE_COLLECT_CNT) - 1);

            if (article.optInt(Article.ARTICLE_COLLECT_CNT) < 0) {
                article.put(Article.ARTICLE_COLLECT_CNT, 0);
            }

            articleRepository.update(followingId, article);
        }
    }
}
