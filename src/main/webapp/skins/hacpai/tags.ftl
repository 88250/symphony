<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel} - ${tagLabel}">
        <meta name="description" content="${symphonyLabel} ${trendTagsLabel},${symphonyLabel} ${coldTagsLabel}"/>
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
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
                                    <img class="tag-img fn-left" src="${staticServePath}/images/tags/${tag.tagIconPath}" />
                                    </#if>
                                    <h2><a rel="tag" href="/tag/${tag.tagURI}">${tag.tagTitle}</a></h2>
                                    <span class="ft-small fn-right">
                                        ${referenceLabel} ${tag.tagReferenceCount} &nbsp;
                                        ${cmtLabel} ${tag.tagCommentCount} 
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
                        <div class="module-panel list">
                            <ul class="tags-cold">
                                <#list coldTags as tag>
                                <li class="fn-clear<#if !tag_has_next> last</#if>">
                                    <#if tag.tagIconPath!="">
                                    <img class="tag-img fn-left" src="${staticServePath}/images/tags/${tag.tagIconPath}" />
                                    </#if>
                                    <h2><a rel="tag" href="/tag/${tag.tagURI}">${tag.tagTitle}</a></h2>
                                    <span class="ft-small fn-right">
                                        ${referenceLabel} ${tag.tagReferenceCount} &nbsp;
                                        ${cmtLabel} ${tag.tagCommentCount} 
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
