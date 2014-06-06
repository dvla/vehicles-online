function recordOutboundLink(e) {
    return _gat._getTrackerByName()._trackEvent(this.href, "Outbound Links"), setTimeout('document.location = "' + this.href + '"', 100), !1
}

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
var Alphagov = {
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
            t.setTime(t.getTime() - Alphagov.daysInMsec(1)), document.cookie = e + "=; expires=" + t.toGMTString() + "; domain=" + Alphagov.cookie_domain() + "; path=/"
        }
    },
    write_cookie: function (e, t) {
        var n = new Date;
        n.setTime(n.getTime() + Alphagov.daysInMsec(120)), document.cookie = e + "=" + encodeURIComponent(t) + "; expires=" + n.toGMTString() + "; domain=" + Alphagov.cookie_domain() + "; path=/"
    }
}, ReportAProblem = {
        handleErrorInSubmission: function (e) {
            var t = $.parseJSON(e.responseText);
            t.message !== "" && $(".report-a-problem-container").html(t.message)
        },
        submit: function () {
            $(".report-a-problem-container .error-notification").remove();
            var e = $(this).find(".button");
            return e.attr("disabled", !0), $.ajax({
                type: "POST",
                url: "/feedback",
                dataType: "json",
                data: $(".report-a-problem-container form").serialize(),
                success: function (e) {
                    $(".report-a-problem-container").html(e.message)
                },
                error: function (t) {
                    t.status == 422 ? (e.attr("disabled", !1), $('<p class="error-notification">Please enter details of what you were doing.</p>').insertAfter(".report-a-problem-container p:first-child")) : ReportAProblem.handleErrorInSubmission(t)
                },
                statusCode: {
                    500: ReportAProblem.handleErrorInSubmission
                }
            }), !1
        }
    };
var GOVUK = GOVUK || {};
GOVUK.Analytics = GOVUK.Analytics || {}, GOVUK.Analytics.internalSiteEvents = function () {
    var e = "GDS_successEvents",
        t = [],
        n = function () {
            var n = Alphagov.read_cookie(e);
            n ? n = jQuery.parseJSON(jQuery.base64Decode(n)) : n = [], t = n
        }, r = function () {
            n(), $(t).each(function () {
                GOVUK.sendToAnalytics(this)
            }), t = [], Alphagov.delete_cookie(e)
        }, i = function (n) {
            t.push(n), Alphagov.write_cookie(e, jQuery.base64Encode(JSON.stringify(t)))
        };
    return {
        push: i,
        sendAll: r
    }
}(), GOVUK.Analytics.entryTokens = function () {
    var e = "GDS_analyticsTokens",
        t = function (e, t) {
            return $.inArray(e, t) !== -1
        }, n = function () {
            return GOVUK.Analytics.getSlug(document.URL, GOVUK.Analytics.Trackers[GOVUK.Analytics.Format].slugLocation)
        }, r = function () {
            var r = JSON.parse(Alphagov.read_cookie(e));
            r || (r = []), t(n(), r) || (r.push(n()), Alphagov.write_cookie(e, JSON.stringify(r)))
        }, i = function () {
            var t = JSON.parse(Alphagov.read_cookie(e)),
                r = $.inArray(n(), t);
            r !== -1 && (t.splice(r, 1), Alphagov.write_cookie(e, JSON.stringify(t)))
        }, s = function () {
            var r = JSON.parse(Alphagov.read_cookie(e));
            return t(n(), r)
        };
    return {
        assignToken: r,
        revokeToken: i,
        tokenExists: s
    }
}();
var GOVUK = GOVUK || {};
GOVUK.Analytics = GOVUK.Analytics || {}, GOVUK.Analytics.Trackers = {}, GOVUK.Analytics.trackingPrefixes = {
    MAINSTREAM: "MS_",
    INSIDE_GOV: "IG_"
}, GOVUK.Analytics.Tracker = function (e, t, n) {
    var r = n;
    return r.prefix = e, r.slugLocation = t, r
}, GOVUK.Analytics.Trackers.guide = new GOVUK.Analytics.Tracker(GOVUK.Analytics.trackingPrefixes.MAINSTREAM, 0, function (e) {
    e.trackTimeBasedSuccess(7e3), e.trackInternalLinks($("#content a"))
}), GOVUK.Analytics.Trackers.transaction = new GOVUK.Analytics.Tracker(GOVUK.Analytics.trackingPrefixes.MAINSTREAM, 0, function (e) {
    e.trackInternalLinks($("#content a")), e.trackLinks($("#get-started a"))
}), GOVUK.Analytics.Trackers.programme = new GOVUK.Analytics.Tracker(GOVUK.Analytics.trackingPrefixes.MAINSTREAM, 0, function (e) {
    e.trackTimeBasedSuccess(7e3), e.trackInternalLinks($("#content a"))
}), GOVUK.Analytics.Trackers.answer = new GOVUK.Analytics.Tracker(GOVUK.Analytics.trackingPrefixes.MAINSTREAM, 0, function (e) {
    e.trackTimeBasedSuccess(7e3), e.trackInternalLinks($("#content a"))
}), GOVUK.Analytics.Trackers.smart_answer = new GOVUK.Analytics.Tracker(GOVUK.Analytics.trackingPrefixes.MAINSTREAM, 0, function (e) {
    GOVUK.Analytics.Trackers.smart_answer.isAjaxNavigation() ? $(document).bind("smartanswerOutcome", e.trackSuccessFunc(!1)) : $(function () {
        $("article.outcome").length === 1 && e.trackSuccess(!0)
    })
});
var browserSupportsHtml5HistoryApi = browserSupportsHtml5HistoryApi || function () {
        return !!(history && history.replaceState && history.pushState)
    };
GOVUK.Analytics.Trackers.smart_answer.isAjaxNavigation = browserSupportsHtml5HistoryApi, GOVUK.Analytics.Trackers.smart_answer.shouldTrackEntry = function () {
    return GOVUK.Analytics.isRootOfArtefact(document.URL, GOVUK.Analytics.Trackers.smart_answer.slugLocation)
}, GOVUK.Analytics.Trackers.smart_answer.shouldTrackSuccess = function () {
    return GOVUK.Analytics.Trackers.smart_answer.isAjaxNavigation() ? GOVUK.Analytics.entryTokens.tokenExists() && !GOVUK.Analytics.isRootOfArtefact(document.URL, GOVUK.Analytics.Trackers.smart_answer.slugLocation) : GOVUK.Analytics.entryTokens.tokenExists() && $("article.outcome").length === 1
}, GOVUK.Analytics.Trackers.policy = new GOVUK.Analytics.Tracker(GOVUK.Analytics.trackingPrefixes.INSIDE_GOV, 2, function (e) {
    e.trackTimeBasedSuccess(3e4), e.trackInternalLinks($("#page a").filter(function () {
        return !GOVUK.Analytics.isLinkToFragmentInCurrentDocument(this)
    }))
}), GOVUK.Analytics.Trackers.detailed_guidance = new GOVUK.Analytics.Tracker(GOVUK.Analytics.trackingPrefixes.INSIDE_GOV, 0, function (e) {
    e.trackTimeBasedSuccess(3e4), e.trackInternalLinks($("#page a"))
}), GOVUK.Analytics.Trackers.news = new GOVUK.Analytics.Tracker(GOVUK.Analytics.trackingPrefixes.INSIDE_GOV, 2, function (e) {
    e.trackInternalLinks($("#page a")), e.trackTimeBasedSuccess(3e4)
});
var GOVUK = GOVUK || {};
GOVUK.Analytics = GOVUK.Analytics || {};
var _gaq = _gaq || [];
GOVUK.sendToAnalytics = function (e) {
    _gaq.push(e)
}, GOVUK.Analytics.isTheSameArtefact = function (e, t, n) {
    var r = function (e) {
        return e.split("/").slice(0, 4 + n).join("/")
    }, i = r(e).replace(/#.*$/, ""),
        s = r(t).replace(/#.*$/, "");
    return i === s
}, GOVUK.Analytics.getSlug = function (e, t) {
    return e.split("/")[3 + t].split("#")[0].split("?")[0]
}, GOVUK.Analytics.isRootOfArtefact = function (e, t) {
    return e.replace(/\/$/, "").split("/").slice(3 + t).length === 1
}, GOVUK.Analytics.isLinkToFragmentInCurrentDocument = function (e) {
    var t = e.href.split("#")[0] === document.URL.split("#")[0],
        n = e.hash !== "";
    return t && n
}, GOVUK.Analytics.startAnalytics = function () {
    var e = 13,
        t = !1,
        n = "none",
        r = GOVUK.Analytics.Format,
        i = GOVUK.Analytics.Trackers[r],
        s = function (e, t) {
            return typeof e == "function" ? e() : t
        }, o = function (e, t) {
            var r = GOVUK.Analytics.getSlug(document.URL, i.slugLocation);
            return ["_trackEvent", n + GOVUK.Analytics.Format, r, e, 0, t]
        }, u = function () {
            if (t) return;
            t = !0;
            var e = encodeURIComponent(GOVUK.Analytics.getSlug(document.URL, i.slugLocation)),
                n = "/exit?slug=" + e + "&format=" + GOVUK.Analytics.Format;
            $(this).prop("href", n)
        }, a = function () {
            if (t) return;
            t = !0;
            var e = o("Success", !1);
            GOVUK.Analytics.isLinkToFragmentInCurrentDocument(this) ? GOVUK.sendToAnalytics(e) : GOVUK.Analytics.internalSiteEvents.push(e)
        }, f = function (t, n) {
            t.each(function () {
                var t = $(this),
                    r;
                this.hostname === window.location.hostname ? r = a : n && (r = u), r && (t.click(r), t.keydown(function (t) {
                    t.which === e && r.call(this, t)
                }))
            })
        }, l = {
            trackSuccessFunc: function (e) {
                return e === undefined && (e = !1),
                function () {
                    l.trackSuccess(e)
                }
            },
            trackSuccess: function (e) {
                e === undefined && (e = !1);
                if (t) return;
                t = !0, GOVUK.sendToAnalytics(o("Success", e))
            },
            trackInternalLinks: function (e) {
                f(e, !1)
            },
            trackLinks: function (e) {
                f(e, !0)
            },
            trackTimeBasedSuccess: function (e) {
                setTimeout(l.trackSuccessFunc(!0), e)
            }
        };
    GOVUK.Analytics.Trackers[r] !== undefined && (n = GOVUK.Analytics.Trackers[r].prefix);
    if (typeof i == "function") {
        var c = GOVUK.Analytics.isTheSameArtefact(document.URL, document.referrer, i.slugLocation);
        s(i.shouldTrackEntry, !c) && (GOVUK.sendToAnalytics(o("Entry", !0)), GOVUK.Analytics.entryTokens.assignToken()), s(i.shouldTrackSuccess, !c) && (i(l), GOVUK.Analytics.entryTokens.revokeToken())
    }
    return GOVUK.Analytics.internalSiteEvents.sendAll(), l
},
function () {
    var e = ("https:" == document.location.protocol ? "https://ssl" : "http://www") + ".google-analytics.com/__utm.gif";
    e += "?utmwv=5.3.9", e += "&utmn=" + Math.floor(Math.random() * parseInt("0x7fffffff", 16)), e += "&utmhn=" + encodeURIComponent(document.location.hostname), e += "&utmp=" + encodeURIComponent("/print" + document.location.pathname), e += "&utmac=UA-26179049-1", e += "&utmcc=__utma%3D999.999.999.999.999.1%3B";
    var t = document.createElement("style");
    t.setAttribute("type", "text/css");
    var n = document.createTextNode("@media print { body:after { content: url(" + e + "); } body { *background: url(" + e + ") no-repeat; }}");
    t.styleSheet ? t.styleSheet.cssText = n.nodeValue : t.appendChild(n);
    var r = document.getElementsByTagName("head")[0];
    r && r.appendChild(t)
}();

require(["jquery"],function($) {

    $(document).ready(function () {

        function t(e) {
            $(e).length == 1 && ($(e).css("top") == "auto" || "0") && $(window).scrollTop($(e).offset().top - $("#global-header").height())
        }
        $(".print-link a").attr("target", "_blank"), $(".js-header-toggle").on("click", function (e) {
            e.preventDefault(), $($(e.target).attr("href")).toggleClass("js-visible"), $(this).toggleClass("js-hidden")
        });
        var e = $(".js-search-focus");
        e.val() !== "" && e.addClass("focus"), e.on("focus", function (e) {
            $(e.target).addClass("focus")
        }), e.on("blur", function (t) {
            e.val() === "" && $(t.target).removeClass("focus")
        }), window.location.hash && $(".design-principles").length != 1 && $(".section-page").length != 1 && t(window.location.hash), $("nav").delegate("a", "click", function () {
            var e, t = $(this).attr("href");
            t.charAt(0) === "#" ? e = t : t.indexOf("#") > 0 && (e = "#" + t.split("#")[1]), $(e).length == 1 && $("html, body").animate({
                scrollTop: $(e).offset().top - $("#global-header").height()
            }, 10)
        }), $(".report-a-problem-toggle a").on("click", function () {
            return $(".report-a-problem-container").toggle(), !1
        });
        var n = $(".report-a-problem-container form, .report-a-problem form");
        n.append('<input type="hidden" name="javascript_enabled" value="true"/>'), n.append($('<input type="hidden" name="referrer">').val(document.referrer || "unknown")), $(".report-a-problem-container form").submit(ReportAProblem.submit), $.browser.msie && $.browser.version < 8 && ($(".button").not("a").on("click focus", function (e) {
            $(this).addClass("button-active")
        }).on("blur", function (e) {
            $(this).removeClass("button-active")
        }), $(".button").on("mouseover", function (e) {
            $(this).addClass("button-hover")
        }).on("mouseout", function (e) {
            $(this).removeClass("button-hover")
        })),
        function () {
            var e = window.navigator.userAgent.match(/(\(Windows[\s\w\.]+\))[\/\(\s\w\.\,\)]+(Version\/[\d\.]+)\s(Safari\/[\d\.]+)/) !== null,
                t;
            e && (t = $("<style type='text/css' media='print'>@font-face {font-family: nta !important;src: local('Arial') !important;}</style>"), document.getElementsByTagName("head")[0].appendChild(t[0]))
        }(), window.GOVUK && GOVUK.userSatisfaction && GOVUK.userSatisfaction.randomlyShowSurveyBar()
    })

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

    $(GOVUK.Analytics.startAnalytics)
});
