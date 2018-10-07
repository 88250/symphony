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

import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.*;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Paginator;
import org.b3log.symphony.cache.TagCache;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Domain;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.*;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tag query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.9.0.4, Oct 7, 2018
 * @since 0.2.0
 */
@Service
public class TagQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(TagQueryService.class);

    /**
     * Tag repository.
     */
    @Inject
    private TagRepository tagRepository;

    /**
     * User-Tag repository.
     */
    @Inject
    private UserTagRepository userTagRepository;

    /**
     * Tag-Tag repository.
     */
    @Inject
    private TagTagRepository tagTagRepository;

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Domain repository.
     */
    @Inject
    private DomainRepository domainRepository;

    /**
     * Domain tag repository.
     */
    @Inject
    private DomainTagRepository domainTagRepository;

    /**
     * Avatar query service.
     */
    @Inject
    private AvatarQueryService avatarQueryService;

    /**
     * Short link query service.
     */
    @Inject
    private ShortLinkQueryService shortLinkQueryService;

    /**
     * Tag cache.
     */
    @Inject
    private TagCache tagCache;

    /**
     * Builds tag objects with the specified tags string.
     *
     * @param tagsStr the specified tags string
     * @return tag objects
     */
    public List<JSONObject> buildTagObjs(final String tagsStr) {
        final List<JSONObject> ret = new ArrayList<>();

        final String[] tagTitles = tagsStr.split(",");
        for (final String tagTitle : tagTitles) {
            final JSONObject tag = new JSONObject();
            tag.put(Tag.TAG_TITLE, tagTitle);

            final String uri = tagRepository.getURIByTitle(tagTitle);
            if (null != uri) {
                tag.put(Tag.TAG_URI, uri);
            } else {
                tag.put(Tag.TAG_URI, tagTitle);
            }

            Tag.fillDescription(tag);

            ret.add(tag);
        }

        return ret;
    }

    /**
     * Gets domains of the specified tag belongs to.
     *
     * @param tagTitle the specified tag title
     * @return domains, returns an empty list if not found
     */
    public List<JSONObject> getDomains(final String tagTitle) {
        final List<JSONObject> ret = new ArrayList<>();

        try {
            final JSONObject tag = tagRepository.getByTitle(tagTitle);
            if (null == tag) {
                return ret;
            }

            final String tagId = tag.optString(Keys.OBJECT_ID);
            final JSONArray relations = domainTagRepository.getByTagId(tagId, 1, Integer.MAX_VALUE).optJSONArray(Keys.RESULTS);
            if (1 > relations.length()) {
                return ret;
            }

            final List<String> domainIds = new ArrayList<>();
            for (int i = 0; i < relations.length(); i++) {
                final JSONObject relation = relations.optJSONObject(i);
                final String domainId = relation.optString(Domain.DOMAIN + "_" + Keys.OBJECT_ID);
                domainIds.add(domainId);
            }

            Collections.sort(domainIds);
            for (final String domainId : domainIds) {
                final JSONObject domain = domainRepository.get(domainId);
                ret.add(domain);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets domains of tag [title=" + tagTitle + "] failed", e);
        }

        return ret;
    }

    /**
     * Gets tags by the specified title prefix.
     *
     * @param titlePrefix the specified title prefix
     * @param fetchSize   the specified fetch size
     * @return a list of tags, for example      <pre>
     * [
     *     {
     *         "tagTitle": "",
     *         "tagIconPath": "",
     *     }, ....
     * ]
     * </pre>
     */
    public List<JSONObject> getTagsByPrefix(final String titlePrefix, final int fetchSize) {
        final JSONObject titleToSearch = new JSONObject();
        titleToSearch.put(Tag.TAG_T_TITLE_LOWER_CASE, titlePrefix.toLowerCase());

        final List<JSONObject> tags = tagCache.getTags();

        int index = Collections.binarySearch(tags, titleToSearch, (t1, t2) -> {
            String u1Title = t1.optString(Tag.TAG_T_TITLE_LOWER_CASE);
            final String inputTitle = t2.optString(Tag.TAG_T_TITLE_LOWER_CASE);

            if (u1Title.length() < inputTitle.length()) {
                return u1Title.compareTo(inputTitle);
            }

            u1Title = u1Title.substring(0, inputTitle.length());

            return u1Title.compareTo(inputTitle);
        });

        final List<JSONObject> ret = new ArrayList<>();

        if (index < 0) {
            return ret;
        }

        int start = index;
        int end = index;

        while (start > -1
                && tags.get(start).optString(Tag.TAG_T_TITLE_LOWER_CASE).startsWith(titlePrefix.toLowerCase())) {
            start--;
        }

        start++;

        while (end < tags.size()
                && tags.get(end).optString(Tag.TAG_T_TITLE_LOWER_CASE).startsWith(titlePrefix.toLowerCase())) {
            end++;
        }

        List<JSONObject> subList = tags.subList(start, end);
        if (64 <= tags.size()) {
            // 标签自动完成进行过滤 https://github.com/b3log/symphony/issues/778
            subList = subList.stream().filter(tag -> tag.optInt(Tag.TAG_REFERENCE_CNT) > 3).collect(Collectors.toList());
        }
        Collections.sort(subList, (t1, t2) -> t2.optInt(Tag.TAG_REFERENCE_CNT) - t1.optInt(Tag.TAG_REFERENCE_CNT));

        return subList.subList(0, subList.size() > fetchSize ? fetchSize : subList.size());
    }

    /**
     * Generates tags for the specified content.
     *
     * @param content      the specified content
     * @param tagFetchSize the specified tag fetch size
     * @return tags
     */
    public List<String> generateTags(final String content, final int tagFetchSize) {
        final List<String> ret = new ArrayList<>();

        final String token = Symphonys.get("boson.token");
        if (StringUtils.isBlank(token)) {
            return ret;
        }

        try {
            final HttpResponse response = HttpRequest.post("http://api.bosonnlp.com/keywords/analysis?top_k=" + tagFetchSize).
                    header("Content-Type", "application/json").
                    header("Accept", "application/json").
                    header("X-Token", token).bodyText("\"" + content + "\"").timeout(5000).send();
            response.charset("UTF-8");
            final String str = response.bodyText();
            try {
                final JSONArray data = new JSONArray(str);
                for (int i = 0; i < data.length(); i++) {
                    final String tag = data.getJSONArray(i).optString(1);
                    if (!StringUtils.isAlphanumericSpace(tag)) {
                        ret.add(tag);
                    }
                }
            } catch (final JSONException e) {
                final JSONObject data = new JSONObject(str);

                LOGGER.log(Level.ERROR, "Boson process failed [" + data.toString(4) + "]");
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Generates tags error: " + content, e);
        }

        return ret;
    }

    /**
     * Determines whether the specified tag title is reserved.
     *
     * @param tagTitle the specified tag title
     * @return {@code true} if it is reserved, otherwise returns {@code false}
     */
    public boolean isReservedTag(final String tagTitle) {
        return ArrayUtils.contains(Symphonys.RESERVED_TAGS, tagTitle);
    }

    /**
     * Gets invalid tags.
     *
     * @return invalid tags, returns an empty list if not found
     */
    public List<String> getInvalidTags() {
        try {
            return tagRepository.getList(new Query().setFilter(new PropertyFilter(Tag.TAG_STATUS, FilterOperator.NOT_EQUAL, Tag.TAG_STATUS_C_VALID))).
                    stream().map(record -> record.optString(Tag.TAG_TITLE)).collect(Collectors.toList());
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets invalid tags error", e);

            return Collections.emptyList();
        }
    }

    /**
     * Gets a tag by the specified tag URI.
     *
     * @param tagURI the specified tag URI
     * @return tag, returns {@code null} if not null
     * @throws ServiceException service exception
     */
    public JSONObject getTagByURI(final String tagURI) throws ServiceException {
        try {
            final JSONObject ret = tagRepository.getByURI(tagURI);
            if (null == ret) {
                return null;
            }

            if (Tag.TAG_STATUS_C_VALID != ret.optInt(Tag.TAG_STATUS)) {
                return null;
            }

            Tag.fillDescription(ret);

            if (StringUtils.isBlank(ret.optString(Tag.TAG_SEO_TITLE))) {
                ret.put(Tag.TAG_SEO_TITLE, tagURI);
            }

            if (StringUtils.isBlank(ret.optString(Tag.TAG_SEO_DESC))) {
                ret.put(Tag.TAG_SEO_DESC, ret.optString(Tag.TAG_T_DESCRIPTION_TEXT));
            }

            if (StringUtils.isBlank(ret.optString(Tag.TAG_SEO_KEYWORDS))) {
                ret.put(Tag.TAG_SEO_KEYWORDS, tagURI);
            }

            final String tagTitle = ret.optString(Tag.TAG_TITLE);
            final List<JSONObject> domains = getDomains(tagTitle);
            ret.put(Tag.TAG_T_DOMAINS, (Object) domains);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets tag [uri=" + tagURI + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets a tag by the specified tag title.
     *
     * @param tagTitle the specified tag title
     * @return tag, returns {@code null} if not null
     * @throws ServiceException service exception
     */
    public JSONObject getTagByTitle(final String tagTitle) throws ServiceException {
        try {
            final JSONObject ret = tagRepository.getByTitle(tagTitle);
            if (null == ret) {
                return null;
            }

            if (Tag.TAG_STATUS_C_VALID != ret.optInt(Tag.TAG_STATUS)) {
                return null;
            }

            Tag.fillDescription(ret);

            if (StringUtils.isBlank(ret.optString(Tag.TAG_SEO_TITLE))) {
                ret.put(Tag.TAG_SEO_TITLE, tagTitle);
            }

            if (StringUtils.isBlank(ret.optString(Tag.TAG_SEO_DESC))) {
                ret.put(Tag.TAG_SEO_DESC, ret.optString(Tag.TAG_T_DESCRIPTION_TEXT));
            }

            if (StringUtils.isBlank(ret.optString(Tag.TAG_SEO_KEYWORDS))) {
                ret.put(Tag.TAG_SEO_KEYWORDS, tagTitle);
            }

            final List<JSONObject> domains = getDomains(tagTitle);
            ret.put(Tag.TAG_T_DOMAINS, (Object) domains);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets tag [title=" + tagTitle + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the trend (sort by reference count descending) tags.
     *
     * @param fetchSize the specified fetch size
     * @return trend tags, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getTrendTags(final int fetchSize) throws ServiceException {
        final Query query = new Query().addSort(Tag.TAG_REFERENCE_CNT, SortDirection.DESCENDING).
                setCurrentPageNum(1).setPageSize(fetchSize).setPageCount(1);

        try {
            final JSONObject result = tagRepository.get(query);
            final List<JSONObject> ret = CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));

            for (final JSONObject tag : ret) {
                Tag.fillDescription(tag);
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets trend tags failed");
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the new (sort by oId descending) tags.
     *
     * @return trend tags, returns an empty list if not found
     */
    public List<JSONObject> getNewTags() {
        return tagCache.getNewTags();
    }

    /**
     * Gets the cold (sort by reference count ascending) tags.
     *
     * @param fetchSize the specified fetch size
     * @return trend tags, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getColdTags(final int fetchSize) throws ServiceException {
        final Query query = new Query().addSort(Tag.TAG_REFERENCE_CNT, SortDirection.ASCENDING).
                setCurrentPageNum(1).setPageSize(fetchSize).setPageCount(1);

        try {
            final JSONObject result = tagRepository.get(query);
            final List<JSONObject> ret = CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));

            for (final JSONObject tag : ret) {
                Tag.fillDescription(tag);
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets cold tags failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the tags the specified fetch size.
     *
     * @param fetchSize the specified fetch size
     * @return tags, returns an empty list if not found
     */
    public List<JSONObject> getTags(final int fetchSize) {
        return tagCache.getIconTags(fetchSize);
    }

    /**
     * Gets the creator of the specified tag of the given tag id.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param tagId          the given tag id
     * @return tag creator, for example,      <pre>
     * {
     *     "tagCreatorThumbnailURL": "",
     *     "tagCreatorThumbnailUpdateTime": 0,
     *     "tagCreatorName": ""
     * }
     * </pre>, returns {@code null} if not found
     * @throws ServiceException service exception
     */
    public JSONObject getCreator(final int avatarViewMode, final String tagId) throws ServiceException {
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Tag.TAG + '_' + Keys.OBJECT_ID, FilterOperator.EQUAL, tagId));

        final List<Filter> orFilters = new ArrayList<>();
        orFilters.add(new PropertyFilter(Common.TYPE, FilterOperator.EQUAL, Tag.TAG_TYPE_C_CREATOR));
        orFilters.add(new PropertyFilter(Common.TYPE, FilterOperator.EQUAL, Tag.TAG_TYPE_C_USER_SELF));

        filters.add(new CompositeFilter(CompositeFilterOperator.OR, orFilters));

        final Query query = new Query().setCurrentPageNum(1).setPageSize(1).setPageCount(1).
                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).
                addSort(Keys.OBJECT_ID, SortDirection.ASCENDING);

        try {
            final JSONObject ret = new JSONObject();

            final JSONObject result = userTagRepository.get(query);
            final JSONArray results = result.optJSONArray(Keys.RESULTS);
            final JSONObject creatorTagRelation = results.optJSONObject(0);
            if (null == creatorTagRelation) {
                LOGGER.log(Level.WARN, "Can't find tag [id=" + tagId + "]'s creator, uses anonymous user instead");
                ret.put(Tag.TAG_T_CREATOR_NAME, UserExt.ANONYMOUS_USER_NAME);

                return ret;
            }

            final String creatorId = creatorTagRelation.optString(User.USER + '_' + Keys.OBJECT_ID);
            if (UserExt.ANONYMOUS_USER_ID.equals(creatorId)) {
                ret.put(Tag.TAG_T_CREATOR_NAME, UserExt.ANONYMOUS_USER_NAME);

                return ret;
            }

            final JSONObject creator = userRepository.get(creatorId);

            final String thumbnailURL = avatarQueryService.getAvatarURLByUser(avatarViewMode, creator, "48");
            ret.put(Tag.TAG_T_CREATOR_THUMBNAIL_URL, thumbnailURL);
            ret.put(Tag.TAG_T_CREATOR_NAME, creator.optString(User.USER_NAME));

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets tag creator failed [tagId=" + tagId + "]", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the participants (article ref) of the specified tag of the given tag id.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param tagId          the given tag id
     * @param fetchSize      the specified fetch size
     * @return tag participants, for example,      <pre>
     * [
     *     {
     *         "tagParticipantName": "",
     *         "tagParticipantThumbnailURL": "",
     *         "tagParticipantThumbnailUpdateTime": long
     *     }, ....
     * ]
     * </pre>, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getParticipants(final int avatarViewMode,
                                            final String tagId, final int fetchSize) throws ServiceException {
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Tag.TAG + '_' + Keys.OBJECT_ID, FilterOperator.EQUAL, tagId));
        filters.add(new PropertyFilter(Common.TYPE, FilterOperator.EQUAL, 1));

        Query query = new Query().setCurrentPageNum(1).setPageSize(fetchSize).setPageCount(1).
                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        final List<JSONObject> ret = new ArrayList<>();

        try {
            JSONObject result = userTagRepository.get(query);
            final JSONArray userTagRelations = result.optJSONArray(Keys.RESULTS);

            final Set<String> userIds = new HashSet<>();
            for (int i = 0; i < userTagRelations.length(); i++) {
                userIds.add(userTagRelations.optJSONObject(i).optString(User.USER + '_' + Keys.OBJECT_ID));
            }

            query = new Query().setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.IN, userIds));
            result = userRepository.get(query);

            final List<JSONObject> users = CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            for (final JSONObject user : users) {
                final JSONObject participant = new JSONObject();

                participant.put(Tag.TAG_T_PARTICIPANT_NAME, user.optString(User.USER_NAME));

                final String thumbnailURL = avatarQueryService.getAvatarURLByUser(avatarViewMode, user, "48");
                participant.put(Tag.TAG_T_PARTICIPANT_THUMBNAIL_URL, thumbnailURL);
                participant.put(Tag.TAG_T_PARTICIPANT_THUMBNAIL_UPDATE_TIME, user.optLong(UserExt.USER_UPDATE_TIME));

                ret.add(participant);
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets tag participants failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the related tags of the specified tag of the given tag id.
     *
     * @param tagId     the given tag id
     * @param fetchSize the specified fetch size
     * @return related tags, for example,      <pre>
     * [{
     *     "oId": "",
     *     "tagTitle": "",
     *     "tagDescription": "",
     *     ....
     * }, ....]
     * </pre>, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getRelatedTags(final String tagId, final int fetchSize) throws ServiceException {
        final List<JSONObject> ret = new ArrayList<>();

        final Set<String> tagIds = new HashSet<>();

        try {
            JSONObject result = tagTagRepository.getByTag1Id(tagId, 1, fetchSize);
            JSONArray relations = result.optJSONArray(Keys.RESULTS);

            boolean full = false;

            for (int i = 0; i < relations.length(); i++) {
                tagIds.add(relations.optJSONObject(i).optString(Tag.TAG + "2_" + Keys.OBJECT_ID));

                if (tagIds.size() >= fetchSize) {
                    full = true;

                    break;
                }
            }

            if (!full) {
                result = tagTagRepository.getByTag2Id(tagId, 1, fetchSize);
                relations = result.optJSONArray(Keys.RESULTS);

                for (int i = 0; i < relations.length(); i++) {
                    tagIds.add(relations.optJSONObject(i).optString(Tag.TAG + "1_" + Keys.OBJECT_ID));

                    if (tagIds.size() >= fetchSize) {
                        break;
                    }
                }
            }

            for (final String tId : tagIds) {
                final JSONObject tag = tagRepository.get(tId);
                if (null != tag) {
                    Tag.fillDescription(tag);
                    ret.add(tag);
                }
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets related tags failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets tags by the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          "tagTitle": "", // optional
     *                          "paginationCurrentPageNum": 1,
     *                          "paginationPageSize": 20,
     *                          "paginationWindowSize": 10
     *                          , see {@link Pagination} for more details
     * @param tagFields         the specified tag fields to return
     * @return for example,      <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "tags": [{
     *         "oId": "",
     *         "tagTitle": "",
     *         "tagDescription": "",
     *         ....
     *      }, ....]
     * }
     * </pre>
     * @throws ServiceException service exception
     * @see Pagination
     */
    public JSONObject getTags(final JSONObject requestJSONObject, final Map<String, Class<?>> tagFields) throws ServiceException {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);
        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
        for (final Map.Entry<String, Class<?>> tagField : tagFields.entrySet()) {
            query.addProjection(tagField.getKey(), tagField.getValue());
        }

        if (requestJSONObject.has(Tag.TAG_TITLE)) {
            query.setFilter(new PropertyFilter(Tag.TAG_TITLE, FilterOperator.EQUAL, requestJSONObject.optString(Tag.TAG_TITLE)));
        }

        JSONObject result = null;

        try {
            result = tagRepository.get(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets tags failed", e);

            throw new ServiceException(e);
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final JSONArray data = result.optJSONArray(Keys.RESULTS);
        final List<JSONObject> tags = CollectionUtils.jsonArrayToList(data);

        for (final JSONObject tag : tags) {
            tag.put(Tag.TAG_T_CREATE_TIME, new Date(tag.optLong(Keys.OBJECT_ID)));
        }

        ret.put(Tag.TAGS, tags);

        return ret;
    }

    /**
     * Gets a tag by the specified id.
     *
     * @param tagId the specified id
     * @return tag, return {@code null} if not found
     * @throws ServiceException service exception
     */
    public JSONObject getTag(final String tagId) throws ServiceException {
        try {
            return tagRepository.get(tagId);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets a tag [tagId=" + tagId + "] failed", e);
            throw new ServiceException(e);
        }
    }
}
