// Define the dcaPopularity namespace
var dcaPopularity = window.dcaPopularity || {};

dcaPopularity.value = {};
dcaPopularity.metadata = [];
dcaPopularity.recName = "";

dcaPopularity.config = {
    frameHeight: 450,
    itemImageWidth: 140,
    itemImageHeight: 210,
    boundingRectPadding: 20,
    itemColors: [
        "#33B28F", "#FBB143", "#2984BF", "#C74443", "#6B7ABB", "#3B5266"
    ],
    linesColor: "rgba(100,100,100, 0.3)",
    rankLineY: 60,
    bottomLineYDiff: 40,
    fontColor: "rgb(128,128,128)",
    topLabelFont: "17px Campton",
    topLabelX: 10,
    topLabelY: 30.5,
    rankLabelFont: "12px Campton",
    itemTitleFont: "normal 13px Campton",
    bottomLabelFont: "normal 12px Campton",
    rankArcRadius: 10,
    borderRectWidth: 160.5,
    borderRectHeight: 230.5,
    itemMinGap: 20.5,
    itemWidth: 160.5
};

// The Canvas Drawing helper object, which has some primitive drawing method.
dcaPopularity.drawing = (function () {
    var drawing = {};

    drawing.drawImage = function (ctx, x, y, width, height, src) {
        var img = new Image();
        img.addEventListener("load", function () {
            ctx.drawImage(img, x, y, width, height);
        }, false);
        img.src = src;
    };

    drawing.drawCircle = function (ctx, x, y, radius, color) {
        ctx.fillStyle = color;
        ctx.beginPath();
        ctx.arc(x, y, radius, 0, Math.PI * 2);
        ctx.closePath();
        ctx.fill();
    };

    drawing.drawConnectingPath = function (ctx, x1, y1, x2, y2, s, fillColor) {
        ctx.strokeStyle = fillColor;
        ctx.beginPath();
        ctx.quadraticCurveTo(x1 + 0.5, y1 + 10.5, x1 + 0.5, y1 + s + 0.5);
        ctx.quadraticCurveTo(x1 + 0.5, y1 + s + 0.5, x2 + 0.5, y1 + s + 0.5);
        ctx.quadraticCurveTo(x2 + 0.5, y1 + s + 0.5, x2 + 0.5, y2 + 0.5);
        ctx.stroke();
    };

    drawing.drawRect = function (ctx, x, y, w, h, style) {
        ctx.strokeStyle = style;
        ctx.strokeRect(x, y, w, h);
    };

    drawing.drawText = function (ctx, text, color, font, x, y) {
        ctx.fillStyle = color;
        ctx.font = font;
        ctx.fillText(text, x, y);
    };

    drawing.drawLine = function (ctx, fromx, fromy, tox, toy, color) {
        ctx.translate(0.5, 0.5);
        ctx.moveTo(fromx, fromy);
        ctx.lineTo(tox, toy);
        ctx.strokeStyle = color;
        ctx.stroke();
        ctx.translate(-0.5, -0.5);
    };

    drawing.drawWrappedText = function (ctx, text, color, font, x, y, maxWidth, lineHeight) {
        if (text == null || text == undefined) return;
        var words = text.split(' ');
        var line = '';

        for (var n = 0; n < words.length; n++) {
            var testLine = line + words[n] + ' ';
            var metrics = ctx.measureText(testLine);
            var testWidth = metrics.width;
            if (testWidth > maxWidth && n > 0) {
                ctx.fillText(line, x, y);
                line = words[n] + ' ';
                y += lineHeight;
            }
            else {
                line = testLine;
            }
        }
        ctx.fillStyle = color;
        ctx.font = font;
        ctx.fillText(line, x, y);
    };

    return drawing;
})();

//Helper object for helping with various calculations
dcaPopularity.helper = (function () {
    var helper = {};

    helper.howManyItems = function (itemWidth, minGap, windowWidth) {
        //we have one more gap than number of items, that's why deducting it from the window width first
        return Math.floor((windowWidth - minGap) / (itemWidth + minGap));
    };

    helper.calculateGap = function (itemWidth, numItems, windowWidth) {
        // we have one more gap than number of items
        var totalSpace = windowWidth - (itemWidth * numItems);
        var gap = totalSpace / (numItems + 1);
        return Math.floor(gap);
    };

    helper.findIntervals = function (items, minGap, axisLength) {
        var result = [];
        var maxScore = Math.max.apply(null, items.map(function (item) {
            return item.score;
        }));
        var minScore = Math.min.apply(null, items.map(function (item) {
            return item.score;
        }));
        var unit = (maxScore - minScore) / axisLength;

        result.push(minGap); // for the first item
        var lastPosition = minGap;
        for (var i = 1; i < items.length - 1; i++) {
            var item = items[i];
            var currentPosition = Math.ceil((maxScore - item.score) / unit);
            if (currentPosition < (lastPosition + minGap)) {
                currentPosition = lastPosition + minGap;
            } else if (currentPosition > (axisLength - ((items.length - i) * minGap))) {
                currentPosition = (axisLength - ((items.length - i) * minGap));
            }
            lastPosition = currentPosition;
            result.push(currentPosition);
        }
        result.push(axisLength - minGap); // for the last item
        return result;
    };

    function getMetaItem(item) {
        for (var mi in dcaPopularity.metadata) {
            if (dcaPopularity.metadata.hasOwnProperty(mi)) {
                var metaItem = dcaPopularity.metadata[mi];
                var identifier = metaItem.id;
                if (identifier == item.id) {
                    return metaItem;
                }
            }
        }
    }

    helper.getThumbnail = function (item) {
        var thumbnailUrl = '';
        var metaItem = getMetaItem(item);
        if(metaItem != null) {
            thumbnailUrl = metaItem.thumbnail;
        }
        return thumbnailUrl;
    };

    helper.getItemTitle = function (item) {
        var title = item.id;
        var metaItem = getMetaItem(item);
        if(metaItem != null) {
            title = metaItem.author != null ? metaItem.author : metaItem.title;
        }

        if (!title) {
            title = "<Missing>";
        } else if (title.trim() == "") {
            title = "<Missing>";
        }
        return title;
    };

    return helper;
})();

dcaPopularity.PopularityWidget = function (element) {
    element.innerHTML =
        "<canvas id='canvas' width='1080' height='450'>" +
        "Your browser does not support canvas!" +
        "</canvas>";

    element.style.display = "block";

    var widget = this;
    function menuChangeHandler() {
        console.log("Request for redraw due to menu change.");
        widget.drawPopularItems(widget.value, dcaPopularity.config);
    }
    $(document).on("menuChanged", menuChangeHandler);

    this.setValue = function (value) {
        if (!value || value == "") return;
        try {
            this.value = JSON.parse(value);
            this.drawPopularItems(this.value, dcaPopularity.config);
        } catch (e) {
            console.log("Error parsing value");
            console.log(value);
        }
    };

    this.updateValueWithMetadata = function (metadata) {
        console.log("Found new metadata");
        dcaPopularity.metadata.push(metadata);
    };

    this.updateRecommenderName = function(name) {
        console.log("Requesting for update of recommender name");
        dcaPopularity.recName = name;
        this.drawPopularItems(this.value, dcaPopularity.config);
    };

    this.clearMetadata = function() {
        console.log("Clearing cached metadata items.");
        dcaPopularity.metadata = [];
        var drawingObject = this;
        setTimeout(function() {
            drawingObject.drawPopularItems(drawingObject.value, dcaPopularity.config);
        }, 3000);
    };

    this.updateMetadata = function (id, body) {
        console.log("Got request for updating metadata for id " + id);
        var metadata = JSON.parse(body);
        this.updateValueWithMetadata(metadata);
        console.log(metadata);
        console.log(body);
        this.drawPopularItems(this.value, dcaPopularity.config);
    };

    this.drawPopularItems = function (popularityData, config) {
        function drawMainPanel(ctx, config) {
            function drawTopLabel() {
                dcaPopularity.drawing.drawText(
                    ctx, "Most Popular", config.fontColor, config.topLabelFont, config.topLabelX, config.topLabelY)
            }

            function drawBottomLabel() {
                var y = config.frameHeight - 15.5;
                dcaPopularity.drawing.drawCircle(ctx, config.topLabelX, y - 3, 4, config.fontColor);
                dcaPopularity.drawing.drawText(ctx, dcaPopularity.recName, config.fontColor, config.bottomLabelFont, config.topLabelX + 12, y);
            }

            function drawTwoLines() {
                dcaPopularity.drawing.drawLine(ctx, 0, config.rankLineY, config.frameWidth, config.rankLineY, config.linesColor);
                var y = config.frameHeight - config.bottomLineYDiff;
                dcaPopularity.drawing.drawLine(ctx, 0, y, config.frameWidth, y, config.linesColor);
            }

            drawTopLabel();
            drawBottomLabel();
            drawTwoLines();
        }

        function drawItems(ctx, config, items) {
            function drawRankCircle(rank, rankAxisPositions) {
                var ranksLineX = rankAxisPositions[rank - 1];
                var itemColor = config.itemColors[(rank - 1) % config.itemColors.length];
                dcaPopularity.drawing.drawCircle(ctx, ranksLineX, config.rankLineY, config.rankArcRadius, itemColor);
                dcaPopularity.drawing.drawText(ctx, rank, "#ffffff", config.rankLabelFont, ranksLineX - 4, config.rankLineY + 4);
            }

            function drawBorder(rank, fromx) {
                var rectY = config.rankLineY + 50.5;
                var itemColor = config.itemColors[(rank - 1) % config.itemColors.length];
                dcaPopularity.drawing.drawRect(ctx, fromx, rectY, config.borderRectWidth, config.borderRectHeight, itemColor);
            }

            function drawItemTitle(item, fromx) {
                var y = config.frameHeight - 80;
                var title = dcaPopularity.helper.getItemTitle(item);
                dcaPopularity.drawing.drawWrappedText(ctx, title, config.fontColor, config.itemTitleFont, fromx, y, 150, 14);
            }

            function drawItemImage(url, fromx) {
                if (url == '') return;
                var x = fromx + 10;
                var y = config.rankLineY + 60;
                dcaPopularity.drawing.drawImage(ctx, x, y, config.itemImageWidth, config.itemImageHeight, url);
            }

            function getConnectingLineHieght(rank, numItems, minY, maxY) {
                var initialHeight = 10;
                var heightDiff = (maxY - (minY + initialHeight)) / (numItems + 1);

                return Math.floor(initialHeight + rank * heightDiff);
            }

            function drawItemToRankConnection(rank, rankAxisPositions, startX, numItems) {
                var circleCenterX = rankAxisPositions[rank - 1];
                var circleCenterY = config.rankLineY;
                var rectCenterX = startX + (config.itemWidth / 2);
                var rectCenterY = config.rankLineY + 50;
                var h = getConnectingLineHieght(rank, numItems, circleCenterY, rectCenterY);
                var itemColor = config.itemColors[(rank - 1) % config.itemColors.length];
                dcaPopularity.drawing.drawConnectingPath(
                    ctx, circleCenterX, circleCenterY, rectCenterX, rectCenterY, h, itemColor);
            }

            function drawItem(item, rank, fromx, axisPositions, numItems) {
                drawBorder(rank, fromx);
                drawItemImage(dcaPopularity.helper.getThumbnail(item), fromx);
                drawItemTitle(item, fromx);
                drawRankCircle(rank, axisPositions);
                drawItemToRankConnection(rank, axisPositions, fromx, numItems);
            }

            var numItems = dcaPopularity.helper.howManyItems(config.itemWidth, config.itemMinGap, config.frameWidth);
            var gap = dcaPopularity.helper.calculateGap(config.itemWidth, numItems, config.frameWidth);
            var shownItems = items.slice(0, numItems);
            var axisPositions = dcaPopularity.helper.findIntervals(shownItems, 20, config.frameWidth);

            // Stateful drawing
            var currentX = gap;
            for (var i = 0; i < numItems; i++) {
                if (!items[i]) {
                    continue;
                }
                drawItem(items[i], i + 1, currentX, axisPositions, numItems);
                currentX = currentX + config.itemWidth + gap;
            }
        }

        function drawWidgetPanel(ctx, config, popularityData) {
            drawMainPanel(ctx, config);
            drawItems(ctx, config, popularityData);
        }

        function intializeAndDraw(popularityData) {
            var canvas = document.getElementById("canvas");
            if(canvas == null) return;
            if (!canvas.getContext) {
                console.log("Context for canvas is not available, unable to draw widget!");
                return;
            }

            var ctx = canvas.getContext("2d");
            var leftPanelWidth = $(".left-panel-container")[0].clientWidth;
            var windowWidth = window.innerWidth;
            config.frameWidth = windowWidth - (leftPanelWidth + 40);
            console.log("setting frameWidth to " + config.frameWidth);

            ctx.canvas.width = config.frameWidth;

            ctx.clearRect(0, 0, config.frameWidth, config.frameHeight);
            ctx.fillStyle = "rgb(255, 255, 255)";
            ctx.fillRect(0, 0, config.frameWidth, config.frameHeight);
            drawWidgetPanel(ctx, config, popularityData);
        }

        function resizeHandler(){
            console.log("Re drawing since window is resized");
            intializeAndDraw(popularityData);
        }

        intializeAndDraw(popularityData);
        var doOnResize;
        window.onresize = function(){
            clearTimeout(doOnResize);
            doOnResize = setTimeout(resizeHandler, 1000);
        };
    };
};