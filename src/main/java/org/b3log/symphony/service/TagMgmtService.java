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
package org.b3log.symphony.service;

import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.URLs;
import org.b3log.symphony.cache.DomainCache;
import org.b3log.symphony.cache.TagCache;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Tag management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.1.6, Apr 16, 2017
 * @since 1.1.0
 */
@Service
public class TagMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(TagMgmtService.class);

    /**
     * Option repository.
     */
    @Inject
    private OptionRepository optionRepository;

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Tag repository.
     */
    @Inject
    private TagRepository tagRepository;

    /**
     * Tag-Tag repository.
     */
    @Inject
    private TagTagRepository tagTagRepository;

    /**
     * User-Tag repository.
     */
    @Inject
    private UserTagRepository userTagRepository;

    /**
     * Domain-Tag repository.
     */
    @Inject
    private DomainTagRepository domainTagRepository;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Domain cache.
     */
    @Inject
    private DomainCache domainCache;

    /**
     * Tag cache.
     */
    @Inject
    private TagCache tagCache;

    /**
     * Removes unused tags.
     */
    @Transactional
    public synchronized void removeUnusedTags() {
        LOGGER.info("Starting remove unused tags....");

        int removedCnt = 0;
        try {
            final JSONArray tags = tagRepository.get(new Query()).optJSONArray(Keys.RESULTS);

            for (int i = 0; i < tags.length(); i++) {
                final JSONObject tag = tags.optJSONObject(i);
                final String tagId = tag.optString(Keys.OBJECT_ID);

                if (0 == tag.optInt(Tag.TAG_REFERENCE_CNT) // article ref cnt
                        && 0 == domainTagRepository.getByTagId(tagId, 1, Integer.MAX_VALUE)
                        .optJSONArray(Keys.RESULTS).length() // domainTagRefCnt
                ) {
                    final JSONArray userTagRels = userTagRepository.getByTagId(tagId, 1, Integer.MAX_VALUE)
                            .optJSONArray(Keys.RESULTS);
                    if (1 == userTagRels.length()
                            && Tag.TAG_TYPE_C_CREATOR == userTagRels.optJSONObject(0).optInt(Common.TYPE)) {
                        // Just the tag's creator but not use it now
                        tagRepository.remove(tagId);
                        removedCnt++;

                        LOGGER.info("Removed a unused tag [title=" + tag.optString(Tag.TAG_TITLE) + "]");
                    }
                }
            }

            final JSONObject tagCntOption = optionRepository.get(Option.ID_C_STATISTIC_TAG_COUNT);
            final int tagCnt = tagCntOption.optInt(Option.OPTION_VALUE);
            tagCntOption.put(Option.OPTION_VALUE, tagCnt - removedCnt);
            optionRepository.update(Option.ID_C_STATISTIC_TAG_COUNT, tagCntOption);

            LOGGER.info("Removed [" + removedCnt + "] unused tags");
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Removes unused tags failed", e);
        }
    }

    /**
     * Adds a tag.
     * <p>
     * <b>Note</b>: This method just for admin console.
     * </p>
     *
     * @param userId   the specified user id
     * @param tagTitle the specified tag title
     * @return tag id
     * @throws ServiceException service exception
     */
    public String addTag(final String userId, final String tagTitle) throws ServiceException {
        String ret;

        final Transaction transaction = tagRepository.beginTransaction();

        try {
            if (null != tagRepository.getByTitle(tagTitle)) {
                throw new ServiceException(langPropsService.get("tagExistLabel"));
            }

            final JSONObject author = userRepository.get(userId);

            JSONObject tag = new JSONObject();
            tag.put(Tag.TAG_TITLE, tagTitle);
            String tagURI = tagTitle;
            tagURI = URLs.encode(tagTitle);
            tag.put(Tag.TAG_URI, tagURI);
            tag.put(Tag.TAG_CSS, "");
            tag.put(Tag.TAG_REFERENCE_CNT, 0);
            tag.put(Tag.TAG_COMMENT_CNT, 0);
            tag.put(Tag.TAG_FOLLOWER_CNT, 0);
            tag.put(Tag.TAG_LINK_CNT, 0);
            tag.put(Tag.TAG_DESCRIPTION, "");
            tag.put(Tag.TAG_ICON_PATH, "");
            tag.put(Tag.TAG_STATUS, 0);
            tag.put(Tag.TAG_GOOD_CNT, 0);
            tag.put(Tag.TAG_BAD_CNT, 0);
            tag.put(Tag.TAG_SEO_TITLE, tagTitle);
            tag.put(Tag.TAG_SEO_KEYWORDS, tagTitle);
            tag.put(Tag.TAG_SEO_DESC, "");
            tag.put(Tag.TAG_RANDOM_DOUBLE, Math.random());
            tag.put(Tag.TAG_AD, "");
            tag.put(Tag.TAG_SHOW_SIDE_AD, 0);

            ret = tagRepository.add(tag);
            tag.put(Keys.OBJECT_ID, ret);

            final JSONObject tagCntOption = optionRepository.get(Option.ID_C_STATISTIC_TAG_COUNT);
            final int tagCnt = tagCntOption.optInt(Option.OPTION_VALUE);
            tagCntOption.put(Option.OPTION_VALUE, tagCnt + 1);
            optionRepository.update(Option.ID_C_STATISTIC_TAG_COUNT, tagCntOption);

            author.put(UserExt.USER_TAG_COUNT, author.optInt(UserExt.USER_TAG_COUNT) + 1);
            userRepository.update(userId, author);

            // User-Tag relation
            final JSONObject userTagRelation = new JSONObject();
            userTagRelation.put(Tag.TAG + '_' + Keys.OBJECT_ID, ret);
            userTagRelation.put(User.USER + '_' + Keys.OBJECT_ID, userId);
            userTagRelation.put(Common.TYPE, Tag.TAG_TYPE_C_CREATOR);
            userTagRepository.add(userTagRelation);

            transaction.commit();

            tagCache.loadAllTags();
            domainCache.loadDomains();

            return ret;
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Adds tag failed", e);

            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * Updates the specified tag by the given tag id.
     * <p>
     * <b>Note</b>: This method just for admin console.
     * </p>
     *
     * @param tagId the given tag id
     * @param tag   the specified tag
     * @throws ServiceException service exception
     */
    public void updateTag(final String tagId, final JSONObject tag) throws ServiceException {
        final Transaction transaction = tagRepository.beginTransaction();

        try {
            tag.put(Tag.TAG_RANDOM_DOUBLE, Math.random());

            tagRepository.update(tagId, tag);

            transaction.commit();

            tagCache.loadTags();

            domainCache.loadDomains();
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Updates a tag[id=" + tagId + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Adds a tag-tag relation.
     *
     * @param tagRelation the specified tag-tag relation
     * @throws ServiceException service exception
     */
    void addTagRelation(final JSONObject tagRelation) throws ServiceException {
        final Transaction transaction = tagTagRepository.beginTransaction();

        try {
            tagTagRepository.add(tagRelation);

            transaction.commit();
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Adds a tag-tag failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Updates the specified tag-tag relation by the given tag relation id.
     *
     * @param tagRelationId the given tag relation id
     * @param tagRelation   the specified tag-tag relation
     * @throws ServiceException service exception
     */
    void updateTagRelation(final String tagRelationId, final JSONObject tagRelation) throws ServiceException {
        final Transaction transaction = tagTagRepository.beginTransaction();

        try {
            tagTagRepository.update(tagRelationId, tagRelation);

            transaction.commit();
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Updates a tag-tag relation [id=" + tagRelationId + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Relates the specified tag string.
     *
     * @param tagString the specified tag string
     * @throws ServiceException service exception
     */
    public void relateTags(final String tagString) throws ServiceException {
        final List<JSONObject> tags = new ArrayList<>();

        try {
            final String[] tagTitles = tagString.split(",");
            for (final String tagTitle : tagTitles) {
                final JSONObject tag = tagRepository.getByTitle(tagTitle.trim());

                if (null != tag) {
                    tags.add(tag);
                }
            }

            for (int i = 0; i < tags.size(); i++) {
                final JSONObject tag1 = tags.get(i);
                final String tag1Id = tag1.optString(Keys.OBJECT_ID);

                for (int j = i + 1; j < tags.size(); j++) {
                    final JSONObject tag2 = tags.get(j);
                    final String tag2Id = tag2.optString(Keys.OBJECT_ID);

                    JSONObject relation = tagTagRepository.getByTag1IdAndTag2Id(tag1Id, tag2Id);
                    if (null != relation) {
                        relation.put(Common.WEIGHT, relation.optInt(Common.WEIGHT) + 1);

                        updateTagRelation(relation.optString(Keys.OBJECT_ID), relation);

                        continue;
                    }

                    relation = tagTagRepository.getByTag1IdAndTag2Id(tag2Id, tag1Id);
                    if (null != relation) {
                        relation.put(Common.WEIGHT, relation.optInt(Common.WEIGHT) + 1);

                        updateTagRelation(relation.optString(Keys.OBJECT_ID), relation);

                        continue;
                    }

                    relation = new JSONObject();
                    relation.put(Tag.TAG + "1_" + Keys.OBJECT_ID, tag1Id);
                    relation.put(Tag.TAG + "2_" + Keys.OBJECT_ID, tag2Id);
                    relation.put(Common.WEIGHT, 1);

                    addTagRelation(relation);
                }
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Relates tag and tag [" + tagString + "] failed", e);
            throw new ServiceException(e);
        }
    }
}
