function setCookie(e, t, n) {
    var r = e + "=" + t + "; path=/";
    if (n) {
        var i = new Date;
        i.setTime(i.getTime() + n * 24 * 60 * 60 * 1e3), r = r + "; expires=" + i.toGMTString()
    }
    document.location.protocol == "https:" && (r += "; Secure"), document.cookie = r
}

function getCookie(e) {
    var t = e + "=",
        n = document.cookie.split(";");
    for (var r = 0, i = n.length; r < i; r++) {
        var s = n[r];
        while (s.charAt(0) == " ") s = s.substring(1, s.length);
        if (s.indexOf(t) === 0) return s.substring(t.length, s.length)
    }
    return null
}
var vehiclesOnline = {
    daysInMsec: function (e) {
        return e * 24 * 60 * 60 * 1e3
    },
    cookie_domain: function () {
        var e = document.location.host.split(":")[0].split(".").slice(-3);
        return "." + e.join(".")
    },
    read_cookie: function (e) {
        var t = null;
        if (document.cookie && document.cookie !== "") {
            var n = document.cookie.split(";");
            for (var r = 0; r < n.length; r++) {
                var i = jQuery.trim(n[r]);
                if (i.substring(0, e.length + 1) == e + "=") {
                    t = decodeURIComponent(i.substring(e.length + 1));
                    break
                }
            }
        }
        return t
    },
    delete_cookie: function (e) {
        if (document.cookie && document.cookie !== "") {
            var t = new Date;
            t.setTime(t.getTime() - vehiclesOnline.daysInMsec(1)), document.cookie = e + "=; expires=" + t.toGMTString() + "; domain=" + vehiclesOnline.cookie_domain() + "; path=/"
        }
    },
    write_cookie: function (e, t) {
        var n = new Date;
        n.setTime(n.getTime() + vehiclesOnline.daysInMsec(120)), document.cookie = e + "=" + encodeURIComponent(t) + "; expires=" + n.toGMTString() + "; domain=" + vehiclesOnline.cookie_domain() + "; path=/"
    }
};

var GOVUK = GOVUK || {};

require(["jquery"],function($) {

    $(document).ready(function () {
        $(function () {
            function s() {
                var e = i ? "related-" + i : "related";
                r && (e += "-with-cookie"), n.length && e !== "related" && n.addClass(e)
            }
            var e, t = $("#global-cookie-message"),
                n = $("#wrapper .related-positioning"),
                r = t.length && getCookie("seen_cookie_message") === null,
                i = $(".beta-notice").length ? "beta" : null;
            s, r && (n.length && typeof GOVUK.stopScrollingAtFooter != "undefined" && GOVUK.stopScrollingAtFooter.updateFooterTop(), t.show(), setCookie("seen_cookie_message", "yes", 28)), s()
        });
    });
});
