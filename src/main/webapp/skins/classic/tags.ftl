<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-present, b3log.org

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
<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${tagLabel} - ${symphonyLabel}">
        <meta name="description" content="${symphonyLabel} ${trendTagsLabel},${symphonyLabel} ${coldTagsLabel}"/>
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
        <link rel="canonical" href="${servePath}/tags">
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <div class="module">
                        <div class="module-header">  
                            <h2>
                                ${trendTagsLabel}
                            </h2>
                        </div>
                        <div class="module-panel list">
                            <ul>
                                <#list trendTags as tag>
                                <li class="fn-flex">
                                    <#if tag.tagIconPath!="">
                                    <div class="avatar" style="background-image:url('${tag.tagIconPath}')" alt="${tag.tagTitle}"></div>
                                    </#if>
                                    <div class="fn-flex-1">
                                        <div class="fn-clear">
                                            <h3 class="fn-left"><a rel="tag" class="ft-a-title" href="${servePath}/tag/${tag.tagURI}">${tag.tagTitle}</a></h3>
                                            <span class="ft-gray fn-right">
                                                ${referenceLabel} ${tag.tagReferenceCount?c} &nbsp;
                                                ${cmtLabel} ${tag.tagCommentCount?c}
                                            </span>
                                        </div>
                                        <div class="vditor-reset">${tag.tagDescription}</div>
                                    </div>
                                </li>
                                </#list>
                            </ul>
                        </div>
                    </div>
                </div>
                <div class="side">
                    <#include 'common/person-info.ftl'/>
                    <div class="module">
                        <div class="module-header">  
                            <h2>
                                ${coldTagsLabel}
                            </h2>
                        </div>
                        <div class="module-panel">
                            <ul class="module-list">
                                <#list coldTags as tag>
                                <li>
                                    <#if tag.tagIconPath!="">
                                    <div class="avatar-small" style="background-image: url('${tag.tagIconPath}')" alt="${tag.tagTitle}"></div>
                                    </#if>
                                    <a class="ft-a-title" rel="tag" href="${servePath}/tag/${tag.tagURI}">${tag.tagTitle}</a>
                                    <div class="vditor-reset">${tag.tagDescription}</div>
                                </li>
                                </#list>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <#include "footer.ftl">
    </body>
</html>
