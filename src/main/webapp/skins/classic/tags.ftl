<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="B3log ${symphonyLabel} - ${tagLabel}">
        <meta name="keywords" content="${trendTagsLabel},${coldTagsLabel}"/>
        <meta name="description" content="B3log ${symphonyLabel} ${trendTagsLabel},B3log ${symphonyLabel} ${coldTagsLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/index.css" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper fn-clear">
                <div class="content fn-clear">
                    <div class="tags-trend list fn-left">
                        <h2>
                            ${trendTagsLabel}
                        </h2> 
                        <ul>
                            <#list trendTags as tag>
                            <li <#if tag_index%2==1>class="even"</#if>>
                                <div class="fn-clear">
                                    <div class="fn-left">
                                        <#if tag.tagIconPath!="">
                                        <img width="16" height="16" src="${staticServePath}/images/tags/${tag.tagIconPath}" />
                                        </#if>
                                        <a rel="tag" href="/tags/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                                    </div>
                                    <span class="ft-small fn-right">
                                        ${referenceLabel} ${tag.tagReferenceCount}
                                        ${cmtLabel} ${tag.tagCommentCount} 
                                    </span>
                                </div>
                                <div>${tag.tagDescription}</div>
                            </li>
                            </#list>
                        </ul>
                    </div>
                    <div class="tags-cold list fn-right">
                        <h2>
                            ${coldTagsLabel}
                        </h2>
                        <ul>
                            <#list coldTags as tag>
                            <li <#if tag_index%2==1>class="even"</#if>>
                                <div class="fn-clear">
                                    <div class="fn-left">
                                        <#if tag.tagIconPath!="">
                                        <img width="16" height="16" src="${staticServePath}/images/tags/${tag.tagIconPath}" />
                                        </#if>
                                        <a rel="tag" href="/tags/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                                    </div>
                                    <span class="ft-small fn-right">
                                        ${referenceLabel} ${tag.tagReferenceCount}
                                        ${cmtLabel} ${tag.tagCommentCount} 
                                    </span>
                                </div>
                                <div>${tag.tagDescription}</div>
                            </li>
                            </#list>
                        </ul>
                    </div>
                </div>  
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">
    </body>
</html>
