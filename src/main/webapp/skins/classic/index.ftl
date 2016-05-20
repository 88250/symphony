<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel}">
        <meta name="description" content="${symDescriptionLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="domains fn-clear">
            <div class="wrapper fn-clear">
                <#list domains as domain>
                <a href='/domain/${domain.domainURI}'>${domain.domainIconPath}&nbsp;${domain.domainTitle}</a>
                </#list>
                <a href="/" class="selected">
                    <svg height="16" viewBox="0 0 14 16" width="14"><path d="M8 8h3v2H7c-0.55 0-1-0.45-1-1V4h2v4z m-1-5.7c3.14 0 5.7 2.56 5.7 5.7S10.14 13.7 7 13.7 1.3 11.14 1.3 8s2.56-5.7 5.7-5.7m0-1.3C3.14 1 0 4.14 0 8s3.14 7 7 7 7-3.14 7-7S10.86 1 7 1z"></path></svg>&nbsp;${latestLabel}</a>
                <a href="/hot">
                    <svg height="16" viewBox="0 0 14 16" width="16"><path d="M10 1c-0.17 0-0.36 0.05-0.52 0.14-1.44 0.88-4.98 3.44-6.48 3.86-1.38 0-3 0.67-3 2.5s1.63 2.5 3 2.5c0.3 0.08 0.64 0.23 1 0.41v4.59h2V11.55c1.34 0.86 2.69 1.83 3.48 2.31 0.16 0.09 0.34 0.14 0.52 0.14 0.52 0 1-0.42 1-1V2c0-0.58-0.48-1-1-1z m0 12c-0.38-0.23-0.89-0.58-1.5-1-0.16-0.11-0.33-0.22-0.5-0.34V3.31c0.16-0.11 0.31-0.2 0.47-0.31 0.61-0.41 1.16-0.77 1.53-1v11z m2-6h4v1H12v-1z m0 2l4 2v1L12 10v-1z m4-6v1L12 6v-1l4-2z"></path></svg>&nbsp;${hotLabel}</a>
                <#if isLoggedIn && "" != currentUser.userCity>
                <a href="/city/my">
                    <svg height="16" viewBox="0 0 12 16" width="12"><path d="M6 0C2.69 0 0 2.5 0 5.5c0 4.52 6 10.5 6 10.5s6-5.98 6-10.5C12 2.5 9.31 0 6 0z m0 14.55C4.14 12.52 1 8.44 1 5.5 1 3.02 3.25 1 6 1c1.34 0 2.61 0.48 3.56 1.36 0.92 0.86 1.44 1.97 1.44 3.14 0 2.94-3.14 7.02-5 9.05z m2-9.05c0 1.11-0.89 2-2 2s-2-0.89-2-2 0.89-2 2-2 2 0.89 2 2z"></path></svg>&nbsp;${currentUser.userCity}</a>
                </#if>
                <a href="/timeline">
                    <svg height="14" viewBox="0 0 16 14" width="16"><path d="M8.06 2C3 2 0 8 0 8s3 6 8.06 6c4.94 0 7.94-6 7.94-6S13 2 8.06 2z m-0.06 10c-2.2 0-4-1.78-4-4 0-2.2 1.8-4 4-4 2.22 0 4 1.8 4 4 0 2.22-1.78 4-4 4z m2-4c0 1.11-0.89 2-2 2s-2-0.89-2-2 0.89-2 2-2 2 0.89 2 2z"></path></svg>&nbsp;${timelineLabel}</a>
                <a href="/community">
                    <svg height="16" viewBox="0 0 12 16" width="12"><path d="M5.05 0.31c0.81 2.17 0.41 3.38-0.52 4.31-0.98 1.05-2.55 1.83-3.63 3.36-1.45 2.05-1.7 6.53 3.53 7.7-2.2-1.16-2.67-4.52-0.3-6.61-0.61 2.03 0.53 3.33 1.94 2.86 1.39-0.47 2.3 0.53 2.27 1.67-0.02 0.78-0.31 1.44-1.13 1.81 3.42-0.59 4.78-3.42 4.78-5.56 0-2.84-2.53-3.22-1.25-5.61-1.52 0.13-2.03 1.13-1.89 2.75 0.09 1.08-1.02 1.8-1.86 1.33-0.67-0.41-0.66-1.19-0.06-1.78 1.25-1.23 1.75-4.09-1.88-6.22l-0.02-0.02z"></path></svg>&nbsp;${communityGroupLabel}</a>
            </div>
        </div>
        <div class="main">
            <div class="wrapper">
                <div class="content fn-clear">
                    <@list listData=latestArticles/>
                    <@pagination url="/"/>
                    <#if domains?size != 0>
                    <br/> <br/> <br/> <br/>

                    <div class="module">
                        <div class="module-header">
                            <h2>${domainLabel}${navigationLabel}</h2>
                            <a href="/domains" class="ft-gray fn-right">All Domains</a>
                        </div>
                        <div class="module-panel">
                            <ul class="module-list domain">
                                <#list domains as domain>
                                <#if domain.domainTags?size gt 0>
                                <li>
                                    <a rel="nofollow" class="slogan" href="/domain/${domain.domainURI}">${domain.domainTitle}</a>
                                    <div class="title">
                                        <#list domain.domainTags as tag>
                                        <a class="tag" rel="nofollow" href="/tag/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                                        </#list>
                                    </div>
                                </li>
                                </#if>
                                </#list>
                            </ul>
                        </div>
                    </div>
                    </#if>
                </div>

                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <script>
            Util.initArticlePreview();
        </script>
    </body>
</html>
