<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="B3log ${symphonyLabel} - ${tagLabel}">
        <meta name="keywords" content="${trendTagsLabel},${coldTagsLabel}"/>
        <meta name="description" content="B3log ${symphonyLabel} ${trendTagsLabel},B3log ${symphonyLabel} ${coldTagsLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/index.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper fn-clear">
                    <div class="content tags-trend list fn-left">
                        <h2>
                            ${trendTagsLabel}
                        </h2> 
                        <ul>
                            <#list trendTags as tag>
                            <li <#if tag_index%2==1>class="even"</#if>>
                                <div class="fn-clear">
                                    <div class="fn-left">
                                        <img src="http://b3logsymphony.cdn.duapp.com:80/images/tags/opensource.jpg"/>
                                        <#if tag.tagIconPath!="">
                                        <img src="${staticServePath}/images/tags/${tag.tagIconPath}" />
                                        </#if>
                                        <a rel="tag" href="/tags/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                                    </div>
                                    <span class="ft-small fn-right">
                                        ${referenceLabel} ${tag.tagReferenceCount}
                                        ${cmtLabel} ${tag.tagCommentCount} 
                                    </span>
                                </div>
                                <div>${tag.tagDescription}Java 是一种可以撰写跨平台应用软件的面向对象的程序设计语言，是由 Sun Microsystems 公司于 1995 年 5 月推出的。Java 技术具有卓越的通用性、高效性、平台移植性和安全性。</div>
                            </li>
                            </#list>
                        </ul>
                    </div>
                    <div class="tags-cold list fn-right side">
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
        </div>
        <#include "footer.ftl">
    </body>
</html>
