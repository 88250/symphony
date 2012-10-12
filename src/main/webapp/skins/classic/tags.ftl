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
                <div class="content">
                    <div class="index-module">
                        <h2>
                            ${trendTagsLabel}
                        </h2> 
                        <ul class="tags fn-clear">
                            <#list trendTags as tag>
                            <li>
                                <a class="fn-left" href="/tags/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                                <div class="fn-left ft-small">
                                    引用 ${tag.tagReferenceCount}<br/>
                                    评论 ${tag.tagCommentCount} 
                                </div>
                            </li>
                            </#list>
                        </ul>
                    </div>
                    <div class="index-module">
                        <h2>
                            ${coldTagsLabel}
                        </h2>
                        <#list coldTags as tag>
                        <a href="/tags/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                        </#list>
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
