<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2018, b3log.org & hacpai.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
<#include "macro-admin.ftl">
<@admin "tags">
<div class="wrapper">
    <div class="module">
        <div class="module-header">
            <h2>${unmodifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <label for="oId">Id</label>
            <input type="text" id="oId" value="${tag.oId}" readonly="readonly" />

            <label for="tagReferenceCount">${refCountLabel}</label>
            <input type="text" id="tagReferenceCount" name="tagReferenceCount" value="${tag.tagReferenceCount}" readonly="readonly" />

            <label for="tagCommentCount">${commentCountLabel}</label>
            <input type="text" id="tagCommentCount" name="tagCommentCount" value="${tag.tagCommentCount}" readonly="readonly" />

            <label for="tagFollowerCount">${followerCountLabel}</label>
            <input type="text" id="tagFollowerCount" name="tagFollowerCount" value="${tag.tagFollowerCount}" />
        </div>
    </div>
    <#if permissions["tagUpdateTagBasic"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2>${modifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="${servePath}/admin/tag/${tag.oId}" method="POST">
                <label for="tagTitle">${tagLabel}${updateCaseOnlyLabel}</label>
                <input type="text" id="tagTitle" name="tagTitle" value="${tag.tagTitle}" />

                <label for="tagURI">URI</label>
                <input type="text" id="tagURI" name="tagURI" value="${tag.tagURI}" />

                <label for="tagDescription">${descriptionLabel}</label>
                <textarea rows="5" id="tagDescription" name="tagDescription">${tag.tagDescription}</textarea>

                <label for="tagIconPath">${iconPathLabel}</label>
                <input type="text" id="tagIconPath" name="tagIconPath" value="${tag.tagIconPath}" />

                <label>${tagStatusLabel}</label>
                <select id="tagStatus" name="tagStatus">
                    <option value="0"<#if 0 == tag.tagStatus> selected</#if>>${validLabel}</option>
                    <option value="1"<#if 1 == tag.tagStatus> selected</#if>>${banLabel}</option>
                </select>

                <label for="tagGoodCnt">${goodCntLabel}</label>
                <input type="text" id="tagGoodCnt" name="tagGoodCnt" value="${tag.tagGoodCnt}" />

                <label for="tagBadCnt">${badCntLabel}</label>
                <input type="text" id="tagBadCnt" name="tagBadCnt" value="${tag.tagBadCnt}" />

                <label for="seoTitle">${seoTitleLabel}</label>
                <input type="text" id="seoTitle" name="tagSeoTitle" value="${tag.tagSeoTitle}" />

                <label for="seoKeywords">${seoKeywordsLabel}</label>
                <input type="text" id="seoKeywords" name="tagSeoKeywords" value="${tag.tagSeoKeywords}" />

                <label for="seoDesc">${seoDescLabel}</label>
                <input type="text" id="seoDesc" name="tagSeoDesc" value="${tag.tagSeoDesc}" />

                <label for="tagCSS">CSS</label>
                <textarea rows="20" id="tagCSS" name="tagCSS">${tag.tagCSS}</textarea>

                <br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>
    </#if>
</div>
</@admin>