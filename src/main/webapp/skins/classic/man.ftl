<#include "macro-head.ftl">
    <!DOCTYPE html>
    <html>
    <head>
        <@head title="Manual for Hackers - ${symphonyLabel}" />
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}"/>
        <link rel="canonical" href="${servePath}/man">
    </head>
    <body>
    <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <div class="module article-module content-reset">
                        <h1 class="article-title"><a class="ft-a-title" href="http://b3log.org" target="_blank">Hacker's
                            Manual</a></h1>
                        <i class="ft-gray">Command manual for hackers, with &hearts; from <a
                                href="https://github.com/tldr-pages/tldr" target="_blank">tldr</a></i>
                        <br><br>
                        <input id="cmd" style="width: 410px;" autofocus>
                        <div id="html">
                        </div>
                    </div>
                </div>
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">

            <script>
        $("#cmd").completed({
            height: 170,
            onlySelect: true,
            data: [],
            afterSelected: function ($it) {
                console.log($it);
            },
            afterKeyup: function (event) {
                $.ajax({
                    url: Label.servePath + '/man/cmd?name=' + $("#cmd").val(),
                    error: function (jqXHR, textStatus, errorThrown) {
                        console.log(textStatus);
                    },
                    success: function (result, textStatus) {
                        if (result.sc) {
                            $("#cmd").completed('updateData', result.mans[0].html);
                        } else {
                            console.log(result);
                        }
                    }
                });
            }
        });




            </script>
    </body>
    </html>