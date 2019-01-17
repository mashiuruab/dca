// Define the namespace
var dcaTouchEventExtension = dcaTouchEventExtension || {};

dcaTouchEventExtension.Component = function (elementId) {
    console.log("Creating js component");

    var element = document.getElementById(elementId);

    // create a simple instance
    var mc = new Hammer.Manager(element);

    // Tap recognizer with minimal 2 taps
    mc.add(new Hammer.Tap({event: 'doubletap', taps: 2}));
    // Single tap recognizer
    mc.add(new Hammer.Tap({event: 'singletap'}));
    mc.add(new Hammer.Swipe({event: 'swipe'}));

    mc.get('swipe').set({direction: Hammer.DIRECTION_HORIZONTAL});

    mc.get('doubletap').recognizeWith('singletap');
    // we only want to trigger a tap, when we don't have detected a doubletap
    mc.get('singletap').requireFailure('doubletap');

    var self = this; // Can't use this inside the function

    mc.on("doubletap", function (ev) {
        console.log("double tap dected ....");
        self.doubletap();
    });

    mc.on("swipeleft", function (ev) {
        console.log("swipe left dected ....");
        self.swipeLeft();
    });

    mc.on("swiperight", function (ev) {
        console.log("swipe right detected ....");
        self.swipeRight();
    });
};