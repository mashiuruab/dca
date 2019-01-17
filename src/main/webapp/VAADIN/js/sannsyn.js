function calculatePipeLineWidth() {
    var calculatedWidth = getRowWidth($(".pipes-body-container .item").children());
    console.log(calculatedWidth);
    var pipesContainerWidth = $(".pipe-view-container .body-container .chain-container").width();
    if (pipesContainerWidth > calculatedWidth) {
        calculatedWidth = pipesContainerWidth;
    }
    var widthPixel = calculatedWidth + "px";
    $(".chain-container .pipes-container").css("width", widthPixel);
    $(".chain-container .pipes-container .dca-widget-title-container").css("width", widthPixel);
    $(".chain-container .pipes-container .pipes-body-container").css("width", widthPixel);

    var pipesContainerHeight = $(".pipe-view-container .body-container .chain-container .pipes-container .pipes-body-container").height();
    $(".pipe-view-container .body-container .chain-container .faucet-container").css("min-height", pipesContainerHeight + 'px');
}

function getJoinContainerWrapperWidth(joinContainerWrapperChildrenList) {
    var joinContainerWidth = 0;
    var selectionWrapperWidth = 0;
    var maxSubPipeItemWidth = 0;

    joinContainerWrapperChildrenList.each(function () {
        if ($(this).hasClass("selection-wrapper")) {
            console.log($(this).attr('class'));
            selectionWrapperWidth += $(this).outerWidth();
        } else if ($(this).hasClass("table-wrapper-layout")) {
            console.log($(this).attr('class'));

            $(this).children().each(function () {
                console.log($(this).attr('class'));
                if($(this).hasClass("table-layout")) {
                    var subPipeItemWidth;
                    $(this).children().each(function () {
                        if ($(this).hasClass("sub-pipe-item")) {
                            console.log($(this).attr('class'));
                            subPipeItemWidth = getRowWidth($(this).children());
                            if (maxSubPipeItemWidth < subPipeItemWidth) {
                                maxSubPipeItemWidth = subPipeItemWidth;
                            }
                            $(this).css("width", subPipeItemWidth + 66 + "px");
                        }
                    });

                    $(this).css("width", subPipeItemWidth + 66 + "px");
                }
            });

            $(this).css("min-width", maxSubPipeItemWidth + 66 + "px");
        }
    });

    joinContainerWidth = maxSubPipeItemWidth + 66;

    if (selectionWrapperWidth > joinContainerWidth) {
        joinContainerWidth = selectionWrapperWidth + 60;
    }

    return joinContainerWidth;
}

function getRowWidth(rowElementChildList) {
    var rowElementWidth = 0;
    var firstColumnWidth = 0;
    var secondColumnWidth = 0;

    rowElementChildList.each(function () {
        if ($(this).hasClass("pipes-component-container") || $(this).hasClass("join-container")) {
            console.log($(this).attr('class'));
            firstColumnWidth += getSingleChainsWidth($(this).children());
            $(this).css("width", firstColumnWidth + "px");
        } else if ($(this).hasClass("join-container-wrapper")) {
            console.log($(this).attr('class'));
            secondColumnWidth += getJoinContainerWrapperWidth($(this).children());
            $(this).css("min-width", secondColumnWidth + "px");
        } else if ($(this).hasClass("source-wrapper")) {
            console.log($(this).attr('class'));
            var sourceWrapperWidth = 0;
            $(this).children().each(function () {
                console.log($(this).attr('class'));
                if ($(this).hasClass('source')) {
                    sourceWrapperWidth += $(this).outerWidth();
                } else {
                    sourceWrapperWidth += $(this).width() + 50;
                }
            });

            $(this).css("min-width", sourceWrapperWidth + "px");
            secondColumnWidth += sourceWrapperWidth;
        }
    });

    rowElementWidth += firstColumnWidth + secondColumnWidth;
    return rowElementWidth;
}

function getSingleChainsWidth(childrenList) {
    var singleChainsWidth = 0;

    childrenList.each(function () {
        console.log($(this).attr('class'));
        singleChainsWidth += $(this).outerWidth();
    });

    return singleChainsWidth;
}


function adjustFooterLogo() {
    var leftPanelContainerHeight = $(".left-panel-container").outerHeight();
    var logoHolderHeight = $(".menu-image-wrapper.logo-holder").outerHeight();
    var navHolderHeight = $(".nav-holder").outerHeight();
    var footerHolderHeight = $(".footer-holder").outerHeight();

    var totalChildElementHeight = logoHolderHeight + navHolderHeight + footerHolderHeight;


    if (leftPanelContainerHeight - totalChildElementHeight < 40) {
        $(".footer-holder").hide();
    } else {
        $(".footer-holder").show();
    }
}

$(window).on('resize', function(){
    adjustFooterLogo();
});


function handleChangePasswordPlaceHolder() {
    $(".dca-change-password-component .v-textfield.current-password").attr("placeholder", "Current Password");
    $(".dca-change-password-component .v-textfield.new-password").attr("placeholder", "Enter New Password");
    $(".dca-change-password-component .v-textfield.retype-new-password").attr("placeholder", "Retype New Password");
};

function calculateFilterChainWidth() {
    var filterBranchContainer = $('.pipe-view-container .body-container .chain-container .filter-container-wrapper .filter-container .filter-branch-container');
    var filterBranchChildList = filterBranchContainer.children();

    var filterBranchContainerWidth = 0;

    filterBranchChildList.each(function () {
        if ($(this).hasClass('filter-chain-container')) {
            var filterContainerWidth = getFilterContainerWidth($(this));
            console.log("Setting the filterContainerWidth " + filterContainerWidth);
            $(this).css('min-width', filterContainerWidth + "px");
            filterBranchContainerWidth += filterContainerWidth;
        } else if ($(this).hasClass('remove-icon-wrapper')) {
            var removeIconWrapperWidth = $(this).outerWidth();
            $(this).css('min-width', removeIconWrapperWidth + "px");
            filterBranchContainerWidth += removeIconWrapperWidth;
        }
    });

    filterBranchContainerWidth += 80;
    filterBranchContainer.css('min-width', filterBranchContainerWidth + "px");

    var filterContainer = $('.pipe-view-container .body-container .chain-container .filter-container-wrapper .filter-container');
    var filterContainerLabelWidth = $('.pipe-view-container .body-container .chain-container .filter-container-wrapper .filter-container .label-name').outerWidth();

    var totalFilterContainerWidth = filterContainerLabelWidth + filterBranchContainerWidth;

    console.log("Total Filter Width Found : " + totalFilterContainerWidth);

    filterContainer.css('min-width', totalFilterContainerWidth + "px");

}

function getFilterContainerWidth(filterContainer) {
    var filterItemTotalWidth = 0;

    filterContainer.children().each(function () {
        var elementWidth = $(this).outerWidth();
        $(this).css('min-width', elementWidth + "px");
        filterItemTotalWidth += elementWidth;
    });

    return filterItemTotalWidth;
}