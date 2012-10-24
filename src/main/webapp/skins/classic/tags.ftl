<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="B3log 社区">
        <meta name="keywords" content=""/>
        <meta name="description" content=""/>
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
                            <li>
                                <img src="${tag.tagIconPath}" />
                                <a href="/tags/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                                <div>${tag.tagDescription}sssssssssssss</div>
                                <div class="ft-small">
                                    引用 ${tag.tagReferenceCount}
                                    评论 ${tag.tagCommentCount} 
                                </div>
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
                            <li>
                                <img src="${tag.tagIconPath}" />
                                <a href="/tags/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                                <div>${tag.tagDescription}</div>
                                <div class="ft-small">
                                    引用 ${tag.tagReferenceCount}
                                    评论 ${tag.tagCommentCount} 
                                </div>
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
