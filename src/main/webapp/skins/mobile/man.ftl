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
                <div class="content content-reset about">
                    <h1>Hacker's Manual</h1>
                    <i class="ft-gray">Command manual for hackers, with <span class="ft-red">&hearts;</span> from <a
                            href="https://github.com/tldr-pages/tldr" target="_blank">tldr</a></i>
                    <br><br>
                    <input id="manCmd" style="width: 310px; padding: 3px;" autofocus placeholder="man">
                    <div id="manHTML">
                        ${mans[0].manHTML}
                    </div>
                </div>
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">
            <script>
        $("#manCmd").completed({
            height: 0,
            onlySelect: true,
            data: [],
            afterSelected: function ($it) {
                console.log($it);
            },
            afterKeyup: function (event) {
                $.ajax({
                    url: Label.servePath + '/man/cmd?name=' + $("#manCmd").val(),
                    error: function (jqXHR, textStatus, errorThrown) {
                        console.log(textStatus);
                    },
                    success: function (result, textStatus) {
                        if (result.sc && result.mans && result.mans.length > 0) {
                            $("#manHTML").html(result.mans[0].manHTML);
                        } else {
                            $("#manHTML").html('${mans[0].manHTML}');
                        }
                    }
                });
            }
        });

            </script>
    </body>
    </html>