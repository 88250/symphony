<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${tagLabel} - ${symphonyLabel}">
        <meta name="description" content="${symphonyLabel} ${trendTagsLabel},${symphonyLabel} ${coldTagsLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
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
                                    <h2><a class="ft-red" rel="tag" href="/tag/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a></h2>
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
                        <div class="module-panel list">
                            <ul class="tags-cold">
                                <#list coldTags as tag>
                                <li class="fn-clear<#if !tag_has_next> last</#if>">
                                    <#if tag.tagIconPath!="">
                                    <div class="avatar fn-left" style="background-image: url('${staticServePath}/images/tags/${tag.tagIconPath}')" alt="${tag.tagTitle}"></div>
                                    </#if>
                                    <h2><a rel="tag" class="ft-green" href="/tag/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a></h2>
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
