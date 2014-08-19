//Set an empty function to be called on window.onunload so that Javascript runs when user returns to this page
//using the back button. This just prevents Firefox from caching the page in the Back-Forward Cache
//https://stackoverflow.com/questions/2638292/after-travelling-back-in-firefox-history-javascript-wont-run
window.onunload = function(){};