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
                            <ul class="tags-trend">
                                <#list trendTags as tag>
                                <li class="fn-clear<#if !tag_has_next> last</#if>"> 
                                    <#if tag.tagIconPath!="">
                                    <div class="avatar fn-left" style="background-image:url('${staticServePath}/images/tags/${tag.tagIconPath}')" alt="${tag.tagTitle}"></div>
                                    </#if>
                                    <div class="fn-flex-1">
                                    <h2><a class="ft-red" rel="tag" href="${servePath}/tag/${tag.tagURI}">${tag.tagTitle}</a></h2>
                                    </div>
                                    <span class="ft-gray fn-right">
                                        ${referenceLabel} ${tag.tagReferenceCount?c} &nbsp;
                                        ${cmtLabel} ${tag.tagCommentCount?c} 
                                    </span>
                                    <div class="content-reset">${tag.tagDescription}</div>
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
                                <li class="fn-clear<#if !tag_has_next> last</#if>">
                                    <#if tag.tagIconPath!="">
                                    <div class="avatar-small slogan" style="background-image: url('${staticServePath}/images/tags/${tag.tagIconPath}')" alt="${tag.tagTitle}"></div>
                                    </#if>
                                   <a rel="tag" href="${servePath}/tag/${tag.tagURI}" class="title">${tag.tagTitle}</a>
                                    <span class="ft-gray fn-right">
                                        ${referenceLabel} ${tag.tagReferenceCount?c} &nbsp;
                                        ${cmtLabel} ${tag.tagCommentCount?c} 
                                    </span>
                                    <div class="content-reset">${tag.tagDescription}</div>
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
