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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Transactional;
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
 * @version 1.3.1.3, Jun 6, 2019
 * @since 0.2.5
 */
@Service
public class FollowMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(FollowMgmtService.class);

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
     * @param followerId     the specified follower id
     * @param followingTagId the specified following tag id
     */
    @Transactional
    public void followTag(final String followerId, final String followingTagId) {
        try {
            follow(followerId, followingTagId, Follow.FOLLOWING_TYPE_C_TAG);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] follows a tag[id=" + followingTagId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);
        }
    }

    /**
     * The specified follower follows the specified following user.
     *
     * @param followerId      the specified follower id
     * @param followingUserId the specified following user id
     */
    @Transactional
    public void followUser(final String followerId, final String followingUserId) {
        try {
            follow(followerId, followingUserId, Follow.FOLLOWING_TYPE_C_USER);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] follows a user[id=" + followingUserId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);
        }
    }

    /**
     * The specified follower follows the specified following article.
     *
     * @param followerId         the specified follower id
     * @param followingArticleId the specified following article id
     */
    @Transactional
    public void followArticle(final String followerId, final String followingArticleId) {
        try {
            follow(followerId, followingArticleId, Follow.FOLLOWING_TYPE_C_ARTICLE);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] follows an article[id=" + followingArticleId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);
        }
    }

    /**
     * The specified follower watches the specified following article.
     *
     * @param followerId         the specified follower id
     * @param followingArticleId the specified following article id
     */
    @Transactional
    public void watchArticle(final String followerId, final String followingArticleId) {
        try {
            follow(followerId, followingArticleId, Follow.FOLLOWING_TYPE_C_ARTICLE_WATCH);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] watches an article[id=" + followingArticleId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);
        }
    }

    /**
     * The specified follower unfollows the specified following tag.
     *
     * @param followerId     the specified follower id
     * @param followingTagId the specified following tag id
     */
    @Transactional
    public void unfollowTag(final String followerId, final String followingTagId) {
        try {
            unfollow(followerId, followingTagId, Follow.FOLLOWING_TYPE_C_TAG);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] unfollows a tag[id=" + followingTagId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);
        }
    }

    /**
     * The specified follower unfollows the specified following user.
     *
     * @param followerId      the specified follower id
     * @param followingUserId the specified following user id
     */
    @Transactional
    public void unfollowUser(final String followerId, final String followingUserId) {
        try {
            unfollow(followerId, followingUserId, Follow.FOLLOWING_TYPE_C_USER);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] unfollows a user[id=" + followingUserId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);
        }
    }

    /**
     * The specified follower unfollows the specified following article.
     *
     * @param followerId         the specified follower id
     * @param followingArticleId the specified following article id
     */
    @Transactional
    public void unfollowArticle(final String followerId, final String followingArticleId) {
        try {
            unfollow(followerId, followingArticleId, Follow.FOLLOWING_TYPE_C_ARTICLE);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] unfollows an article[id=" + followingArticleId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);
        }
    }


    /**
     * The specified follower unwatches the specified following article.
     *
     * @param followerId         the specified follower id
     * @param followingArticleId the specified following article id
     */
    @Transactional
    public void unwatchArticle(final String followerId, final String followingArticleId) {
        try {
            unfollow(followerId, followingArticleId, Follow.FOLLOWING_TYPE_C_ARTICLE_WATCH);
        } catch (final RepositoryException e) {
            final String msg = "User[id=" + followerId + "] unwatches an article[id=" + followingArticleId + "] failed";
            LOGGER.log(Level.ERROR, msg, e);
        }
    }

    /**
     * The specified follower follows the specified following entity with the specified following type.
     *
     * @param followerId    the specified follower id
     * @param followingId   the specified following entity id
     * @param followingType the specified following type
     * @throws RepositoryException repository exception
     */
    private synchronized void follow(final String followerId, final String followingId, final int followingType) throws RepositoryException {
        if (followRepository.exists(followerId, followingId, followingType)) {
            return;
        }

        if (Follow.FOLLOWING_TYPE_C_TAG == followingType) {
            final JSONObject tag = tagRepository.get(followingId);
            if (null == tag) {
                LOGGER.log(Level.ERROR, "Not found tag [id={}] to follow", followingId);
                return;
            }

            tag.put(Tag.TAG_FOLLOWER_CNT, tag.optInt(Tag.TAG_FOLLOWER_CNT) + 1);
            tag.put(Tag.TAG_RANDOM_DOUBLE, Math.random());

            tagRepository.update(followingId, tag);
        } else if (Follow.FOLLOWING_TYPE_C_ARTICLE == followingType) {
            final JSONObject article = articleRepository.get(followingId);
            if (null == article) {
                LOGGER.log(Level.ERROR, "Not found article [id={}] to follow", followingId);
                return;
            }

            article.put(Article.ARTICLE_COLLECT_CNT, article.optInt(Article.ARTICLE_COLLECT_CNT) + 1);

            articleRepository.update(followingId, article, Article.ARTICLE_COLLECT_CNT);
        } else if (Follow.FOLLOWING_TYPE_C_ARTICLE_WATCH == followingType) {
            final JSONObject article = articleRepository.get(followingId);
            if (null == article) {
                LOGGER.log(Level.ERROR, "Not found article [id={}] to watch", followingId);
                return;
            }

            article.put(Article.ARTICLE_WATCH_CNT, article.optInt(Article.ARTICLE_WATCH_CNT) + 1);

            articleRepository.update(followingId, article, Article.ARTICLE_WATCH_CNT);
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
     * @param followerId    the specified follower id
     * @param followingId   the specified following entity id
     * @param followingType the specified following type
     * @throws RepositoryException repository exception
     */
    public synchronized void unfollow(final String followerId, final String followingId, final int followingType) throws RepositoryException {
        followRepository.removeByFollowerIdAndFollowingId(followerId, followingId, followingType);

        if (Follow.FOLLOWING_TYPE_C_TAG == followingType) {
            final JSONObject tag = tagRepository.get(followingId);
            if (null == tag) {
                LOGGER.log(Level.ERROR, "Not found tag [id={}] to unfollow", followingId);
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
                LOGGER.log(Level.ERROR, "Not found article [id={}] to unfollow", followingId);
                return;
            }

            article.put(Article.ARTICLE_COLLECT_CNT, article.optInt(Article.ARTICLE_COLLECT_CNT) - 1);
            if (article.optInt(Article.ARTICLE_COLLECT_CNT) < 0) {
                article.put(Article.ARTICLE_COLLECT_CNT, 0);
            }

            articleRepository.update(followingId, article, Article.ARTICLE_COLLECT_CNT);
        } else if (Follow.FOLLOWING_TYPE_C_ARTICLE_WATCH == followingType) {
            final JSONObject article = articleRepository.get(followingId);
            if (null == article) {
                LOGGER.log(Level.ERROR, "Not found article [id={}] to unwatch", followingId);
                return;
            }

            article.put(Article.ARTICLE_WATCH_CNT, article.optInt(Article.ARTICLE_WATCH_CNT) - 1);
            if (article.optInt(Article.ARTICLE_WATCH_CNT) < 0) {
                article.put(Article.ARTICLE_WATCH_CNT, 0);
            }

            articleRepository.update(followingId, article, Article.ARTICLE_WATCH_CNT);
        }
    }
}
