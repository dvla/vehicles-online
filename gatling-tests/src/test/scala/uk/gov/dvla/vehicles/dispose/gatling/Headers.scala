package uk.gov.dvla.vehicles.dispose.gatling

object Headers {

  val headers_accept_html = Map(
    "Accept" -> """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"""
  )

  val headers_accept_png = Map(
    "Accept" -> """image/png,image/*;q=0.8,*/*;q=0.5"""
  )

  val headers_x_www_form_urlencoded = Map(
    "Accept" -> """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""",
    "Content-Type" -> """application/x-www-form-urlencoded"""
  )
}
