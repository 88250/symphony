/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2017,  b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * @fileOverview
 *
 * @author Zephyr
 * @version 1.0.0 Mar 14, 2017
 */
var Gobang = {
    unitSize: 30,
    chessLength: 600,
    chess:new Array(),
/////upNew downOld///
    dir: null,
    lastDir: null,
    map: null,
    food: null,
    R: 10, // 圆半径或者外接正方形尺寸的一半
    
    snake: null,
    oMark: null, // 分数显示框
    isPause: false, // 是否暂停
    chessCanvas: null,
    interval: null,
    currTime: 200,
    stepTime: 5,
    baseLen: 6,
    startTime: null,
    endTime: null,
    countTime: null,
    snakeColor: 0,
    appleColor: 255,
    drawChessBoard:function(){
        Gobang.chessCanvas.fillStyle = "darkorange";
        Gobang.chessCanvas.fillRect(0,0,Gobang.chessLength,Gobang.chessLength);
        Gobang.chessCanvas.strokeStyle = "black";
        Gobang.chessCanvas.lineWidth=10;
        Gobang.chessCanvas.strokeRect(0,0,Gobang.chessLength,Gobang.chessLength);
        Gobang.chessCanvas.lineWidth=1;
        for(var i=1;i<Gobang.chessLength/Gobang.unitSize;i++){
            Gobang.drawChessLine(i);
        }
        Gobang.drawChessMan(5*Gobang.unitSize,5*Gobang.unitSize,5,"black");
        Gobang.drawChessMan(15*Gobang.unitSize,5*Gobang.unitSize,5,"black");
        Gobang.drawChessMan(5*Gobang.unitSize,15*Gobang.unitSize,5,"black");
        Gobang.drawChessMan(15*Gobang.unitSize,15*Gobang.unitSize,5,"black");
        //初始化记录落子的数组
        for(var i=0;i<Gobang.chessLength/Gobang.unitSize;i++){
            Gobang.chess[i]=new Array();
            for(var j=0;j<Gobang.chessLength/Gobang.unitSize;j++){
                Gobang.chess[i][j]=0;
            }
        }
    },
    drawChessLine:function(i){
        Gobang.chessCanvas.moveTo(0, i*Gobang.unitSize);
        Gobang.chessCanvas.lineTo(Gobang.chessLength, i*Gobang.unitSize);
        Gobang.chessCanvas.stroke();

        Gobang.chessCanvas.moveTo(i*Gobang.unitSize,0);
        Gobang.chessCanvas.lineTo(i*Gobang.unitSize,Gobang.chessLength);
        Gobang.chessCanvas.stroke();
    },
    drawChessMan:function(x,y,raidus,color){
        Gobang.chessCanvas.fillStyle = color;
        Gobang.chessCanvas.beginPath();
        Gobang.chessCanvas.arc(x,y,raidus, 0, Math.PI*2, true);
        Gobang.chessCanvas.fill();
    },
    getChessManPoint:function(mouse,player){
        // console.log("<ZephyrTest>鼠标相对于canvas位置："+mouse.x+","+mouse.y);
        var xo=mouse.x;
        var yo=mouse.y;
        mouse.x=Math.floor(mouse.x/10)*10;//取整
        mouse.y=Math.floor(mouse.y/10)*10;
        var xm=mouse.x;
        var xn=mouse.x;
        var ym=mouse.y;
        var yn=mouse.y;//定义任意落点相邻的四个坐标，应能整除unitSize
        while(xn%30!=0){
            xn=xn-10;
        };
        while(xm%30!=0){
            xm=xm+10;
        };
        while(yn%30!=0){
            yn=yn-10;
        };
        while(ym%30!=0){
            ym=ym+10;
        };
        // console.log("<ZephyrTest>相邻接点坐标：xm:"+xm+",xn:"+xn+",ym:"+ym+",yn:"+yn);
        var radius=new Array();
        var result=new Array();
        radius[0]=(xm-xo)*(xm-xo)+(ym-yo)*(ym-yo);
        result[radius[0]]={x:xm,y:ym};
        radius[1]=(xm-xo)*(xm-xo)+(yn-yo)*(yn-yo);
        result[radius[1]]={x:xm,y:yn};
        radius[2]=(xn-xo)*(xn-xo)+(ym-yo)*(ym-yo);
        result[radius[2]]={x:xn,y:ym};
        radius[3]=(xn-xo)*(xn-xo)+(yn-yo)*(yn-yo);
        result[radius[3]]={x:xn,y:yn};
        radius.sort(function(a,b){
            return a-b;
        });
        // console.log("<ZephyrTest>数组排序结果："+radius[0]+","+radius[1]+","+radius[2]+","+radius[3]);
        // console.log("<ZephyrTest>半径计算结果："+result[radius[0]].x+","+result[radius[0]].y);
        if(result[radius[0]].x==0||result[radius[0]].x==600||result[radius[0]].y==0||result[radius[0]].y==600)
            return;
        if(player==1 && Gobang.chess[result[radius[0]].x/Gobang.unitSize][result[radius[0]].y/Gobang.unitSize]==0){
            Gobang.chess[result[radius[0]].x/Gobang.unitSize][result[radius[0]].y/Gobang.unitSize]=2;
            Gobang.drawChessMan(result[radius[0]].x,result[radius[0]].y,Gobang.unitSize/2,"black");
            Gobang.checkChessMan(2);
        }else if(player==2 && Gobang.chess[result[radius[0]].x/Gobang.unitSize][result[radius[0]].y/Gobang.unitSize]==0){
            Gobang.chess[result[radius[0]].x/Gobang.unitSize][result[radius[0]].y/Gobang.unitSize]=1;
            Gobang.drawChessMan(result[radius[0]].x,result[radius[0]].y,Gobang.unitSize/2,"white");
            Gobang.checkChessMan(1);
        }
    },
    checkChessMan:function(num){
        //横向检查
        for(var i=0;i<Gobang.chess.length;i++){
            var count=0;
            for(var j=0;j<Gobang.chess[i].length;j++){
                if(Gobang.chess[i][j]==num){
                    count++;
                }else if(Gobang.chess[i][j]!=num && count<5){
                    count=0;
                }
            }
            if(count>=5){
                console.log("五子连星:"+num);
                return;
            }
        }
        //纵向检查
        for(var j=0;j<Gobang.chess[0].length;j++){
            var count=0;
            for(var i=0;i<Gobang.chess.length;i++){
                if(Gobang.chess[i][j]==num){
                    count++;
                }else if(Gobang.chess[i][j]!=num && count<5){
                    count=0;
                }
            }
            if(count>=5){
                console.log("五子连星:"+num);
                return;
            }
        }
        //左上右下检查，下一个检查点时上一个检查点横纵坐标均＋1
        //横向增长，横坐标先行出局
        for(var x=0,y=0;x<Gobang.chess.length;x++){
            var count=0;
            for(i=x,j=y;i<Gobang.chess.length;i++,j++){
                if(Gobang.chess[i][j]==num){
                    count++;
                }else if(Gobang.chess[i][j]!=num && count<5){
                    count=0;
                }
            }
            if(count>=5){
                console.log("五星连珠："+num);
                return;
            }
        }
        //纵向增长，纵坐标先出局
        for(var x=0,y=0;y<Gobang.chess[0].length;y++){
            var count=0;
            for(i=x,j=y;j<Gobang.chess.length;i++,j++){
                if(Gobang.chess[i][j]==num){
                    count++;
                }else if(Gobang.chess[i][j]!=num && count<5){
                    count=0;
                }
            }
            if(count>=5){
                console.log("五星连珠："+num);
                return;
            }
        }
        //左下右上检查x-1,y+1
        //横向增长，横坐标先行出局
        for(var x=0,y=0;x<Gobang.chess.length;x++){
            var count=0;
            for(i=x,j=y;i>=0;i--,j++){
                if(Gobang.chess[i][j]==num){
                    count++;
                }else if(Gobang.chess[i][j]!=num && count<5){
                    count=0;
                }
            }
            if(count>=5){
                console.log("五星连珠："+num);
                return;
            }
        }
        //纵向增长，纵坐标先出局
        for(var x=Gobang.chess.length-1,y=0;y<Gobang.chess[0].length;y++){
            var count=0;
            for(i=x,j=y;j<Gobang.chess.length;i--,j++){
                if(Gobang.chess[i][j]==num){
                    count++;
                }else if(Gobang.chess[i][j]!=num && count<5){
                    count=0;
                }
            }
            if(count>=5){
                console.log("五星连珠："+num);
                return;
            }
        }

    },
    initCanvas: function (oMarkId, chessCanvasId) {
        // Gobang.oMark = document.getElementById(oMarkId);
        Gobang.chessCanvas = document.getElementById(chessCanvasId).getContext('2d');
        Gobang.drawChessBoard();
    }
};
