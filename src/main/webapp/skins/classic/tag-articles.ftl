<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
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
                    <div class="fn-clear" style="margin-bottom:20px">
                        <div class="fn-left">
                            <h1><a href="/tags/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a></h1>
                            <span class="ft-small">
                                ${referenceCountLabel} ${tag.tagReferenceCount} &nbsp;
                                ${commentCountLabel} ${tag.tagCommentCount}
                            </span>
                        </div>
                        <div class="fn-left">
                            <a class="ft-noline" title="${creatorLabel}:88250" href="/88250">
                                <img style="margin-left:20px" class="avatar fn-left" src="http://secure.gravatar.com/avatar/59a5e8209c780307dbe9c9ba728073f5?s=140&d=http://localhost:8080/images/user-thumbnail.png">
                            </a>
                        </div>
                        <div class="fn-right">
                            <a class="ft-noline" title="${contributorLabel}:88250" href="/88250">
                                <img class="avatar fn-left" src="http://secure.gravatar.com/avatar/59a5e8209c780307dbe9c9ba728073f5?s=140&d=http://localhost:8080/images/user-thumbnail.png">
                            </a>
                        </div>
                    </div>
                    <@list listData=articles/>
                    <@pagination url="/tags/${tag.tagTitle?url('utf-8')}"/>
                </div> 
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">
    </body>
</html>
