window.com_sannsyn_dca_vaadin_widgets_popularity_DCAPopularityWidgetComponent =
    function () {
        // Create the component
        var widgetUi =
            new dcaPopularity.PopularityWidget(this.getElement());

        // Handle changes from the server-side
        var self = this;
        this.onStateChange = function () {
            // Pass user interaction to the server-side
            widgetUi.setValue(self.getState().value);
        };

        widgetUi.click = function (itemId) {
            self.onClick(itemId.toString());
        };

        this.updateRecommenderName = function(name) {
            console.log("Should update recommender name");
            widgetUi.updateRecommenderName(name);
        };

        this.clearMetadata = function() {
            console.log("Clearing cached metadata...");
            widgetUi.clearMetadata();
        };

        this.updateMetadata = function(id, body) {
            console.log("Should update metadata for " + id);
            widgetUi.updateMetadata(id, body);
        }
    };