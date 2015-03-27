package models

import play.api.mvc.Call

case class VrmLockedViewModel(timeString: String, javascriptTimestamp: Long, tryAnother: Call, exit: Call)
