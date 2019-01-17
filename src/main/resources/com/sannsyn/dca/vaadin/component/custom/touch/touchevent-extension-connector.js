window.com_sannsyn_dca_vaadin_component_custom_touch_DCATouchEventExtension =
    function () {
        // Create the component
        var componentId = this.getState().identifier;

        var component =
            new dcaTouchEventExtension.Component(componentId);

        // Pass user interaction to the server-side
        var self = this;

        component.swipeLeft = function () {
            console.log("Swipe left in connector ...");
            self.swipeLeft();
        };

        component.swipeRight = function () {
            console.log("Swipe right in connector ...");
            self.swipeRight();
        };

        component.doubletap = function () {
            console.log("double tap in connector ...");
            self.doubletap();
        };
    };