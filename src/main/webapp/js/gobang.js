/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/**
 * @fileOverview
 *
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.0.0.2, Apr 6, 2017
 * @since 2.1.0
 */
var Gobang = {
    unitSize: 30,
    chessLength: 600,
    initGobang:function(wsurl){
        if (!confirm(Label.activityStartGobangTipLabel)) {
            return;
        }

        $.ajax({
            url: Label.servePath + "/activity/gobang/start",
            type: "POST",
            cache: false,
            success: function (result) {
                if (result.sc) {
                    GobangChannel.init(wsurl + "/gobang-game-channel?player=" + Label.currentUserName);
                    $(".side button.green").hide();
                    $(".side button.red, #chatInput").show();
                } else {
                    $(".side ul").prepend('<li>' + result.msg + '</ul>');
                }
            }
        });

        $('#chatInput').keyup(function (event) {
            if ($.trim($("#chatInput").val()) === '') {
                return false;
            }
            if (event.keyCode === 13) {
                Gobang.chatSend();
            }
        });
    },
    drawChessBoard:function(){
        Gobang.chessCanvas.fillStyle = "rgb(255,229,143)";
        Gobang.chessCanvas.fillRect(0,0,Gobang.chessLength,Gobang.chessLength);
        Gobang.chessCanvas.strokeStyle = "black";
        Gobang.chessCanvas.lineWidth = 10;
        Gobang.chessCanvas.strokeRect(0,0,Gobang.chessLength,Gobang.chessLength);
        Gobang.chessCanvas.lineWidth = 1;
        for(var i = 1;i < Gobang.chessLength / Gobang.unitSize;i++){
            Gobang.drawChessLine(i);
        }
        Gobang.drawChessMan(5 * Gobang.unitSize,5 * Gobang.unitSize,5,"black");
        Gobang.drawChessMan(15 * Gobang.unitSize,5 * Gobang.unitSize,5,"black");
        Gobang.drawChessMan(5 * Gobang.unitSize,15 * Gobang.unitSize,5,"black");
        Gobang.drawChessMan(15 * Gobang.unitSize,15 * Gobang.unitSize,5,"black");
    },
    drawChessLine:function(i){
        Gobang.chessCanvas.moveTo(0, i * Gobang.unitSize);
        Gobang.chessCanvas.lineTo(Gobang.chessLength, i * Gobang.unitSize);
        Gobang.chessCanvas.stroke();

        Gobang.chessCanvas.moveTo(i * Gobang.unitSize,0);
        Gobang.chessCanvas.lineTo(i * Gobang.unitSize,Gobang.chessLength);
        Gobang.chessCanvas.stroke();
    },
    drawChessMan:function(x,y,raidus,color){
        Gobang.chessCanvas.fillStyle = color;
        Gobang.chessCanvas.beginPath();
        Gobang.chessCanvas.arc(x,y,raidus, 0, Math.PI * 2, true);
        Gobang.chessCanvas.fill();
    },
    getChessManPoint:function(mouse,player){
        var xo = mouse.x;
        var yo = mouse.y;
        mouse.x = Math.floor(mouse.x / 10) * 10;//取整
        mouse.y = Math.floor(mouse.y / 10) * 10;
        var xm = mouse.x;
        var xn = mouse.x;
        var ym = mouse.y;
        var yn = mouse.y;//定义任意落点相邻的四个坐标，应能整除unitSize
        var posX = 0;
        var posY = 0;
        while(xn % 30 != 0){
            xn = xn-10;
        };
        while(xm % 30 != 0){
            xm = xm+10;
        };
        while(yn % 30 != 0){
            yn = yn-10;
        };
        while(ym % 30 != 0){
            ym = ym+10;
        };
        var radius = new Array();
        var result = new Array();
        radius[0] = (xm - xo) * (xm - xo) + (ym - yo) * (ym-yo);
        result[radius[0]] = {x:xm,y:ym};
        radius[1] = (xm-xo) * (xm - xo) + (yn - yo) * (yn - yo);
        result[radius[1]] = {x:xm,y:yn};
        radius[2] = (xn-xo) * (xn - xo) + (ym - yo) * (ym - yo);
        result[radius[2]] = {x:xn,y:ym};
        radius[3] = (xn-xo) * (xn - xo) + (yn - yo) * (yn - yo);
        result[radius[3]] = {x:xn,y:yn};
        radius.sort(function(a,b){
            return a - b;
        });
        posX = result[radius[0]].x;
        posY = result[radius[0]].y;
        if(posX == 0 || posX == 600 || posY == 0 || posY == 600)
            return;
        var message = {
            type:2,
            x:posX,
            y:posY,
            size:Gobang.unitSize,
            player:player
        };
        GobangChannel.ws.send(JSON.stringify(message));
    },
    initCanvas: function (oMarkId, chessCanvasId) {
        Gobang.chessCanvas = document.getElementById(chessCanvasId).getContext('2d');
        Gobang.drawChessBoard();
    },
    getMousePos:function(canvas, evt){
        var rect = canvas.getBoundingClientRect();
        return {
            x: evt.clientX - rect.left * (canvas.width / rect.width),
            y: evt.clientY - rect.top * (canvas.height / rect.height)
        }
    },
    chatSend:function(){
        var message = {
            type: 1,
            player: $("#gobangCanvas").data('player'),
            message: $("#chatInput").val()
        }
        $(".side ul").prepend('<li>' + Label.currentUserName + ": " + $("#chatInput").val() + '</li>');
        GobangChannel.ws.send(JSON.stringify(message));
        $("#chatInput").val('');
    },
    moveChess:function(evt){
        var mousePos = Gobang.getMousePos(document.getElementById("gobangCanvas"), evt);
        Gobang.getChessManPoint(mousePos,$("#gobangCanvas").data('player'));
    },
    drawChess:function(chess){
        for(var i=1;i<chess.length;i++){
            for(var j=1;j<chess.length;j++){
                if(chess[i][j]==1){
                    Gobang.drawChessMan(i*Gobang.unitSize,j*Gobang.unitSize,Gobang.unitSize/2,"black");
                }else if(chess[i][j]==2){
                    Gobang.drawChessMan(i*Gobang.unitSize,j*Gobang.unitSize,Gobang.unitSize/2,"white");
                }
            }
        }
    },
    requestDraw:function(){
        var message = {
            type:7,
            player:$("#gobangCanvas").data('player'),
            drawType:"request"
        };
        GobangChannel.ws.send(JSON.stringify(message));
    }
};

/**
 * @description gobang game channel.
 * @static
 */
var GobangChannel = {
    /**
     * WebSocket instance.
     *
     * @type WebSocket
     */
    ws: undefined,
    /**
     * @description Initializes message channel
     */
    init: function (channelServer) {
        GobangChannel.ws = new ReconnectingWebSocket(channelServer);
        GobangChannel.ws.reconnectInterval = 10000;

        GobangChannel.ws.onopen = function () {
            // GobangChannel.ws.send('zephyr test');
        };

        GobangChannel.ws.onmessage = function (evt) {
            var resp = JSON.parse(evt.data);
            // 1：聊天，2：下子，3：创建游戏，等待加入，4：加入游戏，游戏开始，5：断线重连，恢复棋盘，6：系统通知
            switch(resp.type){
                case 1:
                    $(".side ul").prepend('<li>' + resp.player + ": " + resp.message + '</li>');
                    break;
                case 2:
                    Gobang.drawChess(resp.chess);
                    Gobang.drawChessMan(resp.posX,resp.posY,5,"red");
                    if(resp.result != null && resp.result != ""){
                        alert(resp.result);
                        document.getElementById("gobangCanvas").removeEventListener("click",Gobang.moveChess);
                        $(".side button.green").show();
                        $(".side button.red").hide();
                        $(".side ul").prepend('<li>GG</li>');
                    }
                    break;
                case 3:
                    $(".side ul").prepend('<li>' + resp.message + '</li>');
                    break;
                case 4:
                    $(".side ul").prepend('<li>' + resp.message + '</li>');
                    $("#gobangCanvas").data('player', resp.player);
                    break;
                case 5:
                    $(".side ul").prepend('<li>' + resp.message + '</li>');
                    $("#gobangCanvas").data('player', resp.player);
                    Gobang.drawChess(resp.chess);
                    break;
                case 6:
                    $(".side ul").prepend('<li>' + resp.message + '</li>');
                    break;
                case 7:
                    var message = {
                        type:7,
                        player:$("#gobangCanvas").data('player'),
                        drawType:""
                    };
                    if (confirm(Label.activityAskForDrawLabel)) {
                        message.drawType="yes";
                    }else{
                        message.drawType="no";
                    }
                    GobangChannel.ws.send(JSON.stringify(message));
                    break;
            }
        };

        GobangChannel.ws.onclose = function () {
            GobangChannel.ws.close();
        };

        GobangChannel.ws.onerror = function (err) {
            console.log("ERROR", err);
        };
    }
};
