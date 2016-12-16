package email

import java.text.SimpleDateFormat
import org.joda.time.DateTime
import play.api.i18n.{Lang, Messages}
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel

/**
 * The email message builder class will create the contents of the message. override the buildHtml and buildText
 * with new html and text templates respectively.
 */
object EmailMessageBuilder {
  import uk.gov.dvla.vehicles.presentation.common.services.SEND.Contents

  def buildWith(vehicleDetailsOpt: Option[VehicleAndKeeperDetailsModel],  transactionId: String,
                imagesPath: String, transactionTimestamp: DateTime, isPrivate: Boolean = true, toTrader: Boolean = false)(implicit lang: Lang): Contents = {

    val transactionTimestampStr = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(transactionTimestamp.toDate)

    val registrationNumber = vehicleDetailsOpt.map(_.registrationNumber).getOrElse("No registration number")

    // private confirmation email by default
    var contentPart1 = Messages("email.contentPart1.private")
    var contentPart1LastSentence = Messages("email.contentPart1LastSentence")
    var contentPart1LastSentenceHtml = Messages("email.contentPart1LastSentence.Html")
    var contentPart2 = Messages("email.txndetails.p1.private")
    var contentPart3 = Messages("email.contentPart3.private")
    var contentPart4Part2 = Messages("email.contentPart4.private")

    var contentPart5 = Messages("email.contentPart5")
    var contentPart5P2 = Messages("email.contentPart5.p2")

    // amend to suit trade application email(s)
    if (!isPrivate) {
      if (!toTrader) {
        contentPart1 = Messages("email.contentPart1.trade")
        contentPart1LastSentence = ""
        contentPart1LastSentenceHtml = ""
      } else {
        contentPart1 = Messages("email.contentPart1.tradeAppToTrader")

        contentPart3 = Messages("email.contentPart3.trade")
      }
      contentPart2 = Messages("email.txndetails.p1.trade")
      contentPart4Part2 = Messages("email.contentPart4.trade")
      contentPart5 = ""
      contentPart5P2 = ""
    }

    val contentPart4 = Messages("email.contentPart4.p1")
      .concat(" " + contentPart4Part2)
      .concat(" " + Messages("email.contentPart4.p3"))

    val linesPlain = Array(
      contentPart1,
      contentPart2,
      contentPart3,
      contentPart4,
      contentPart5,
      contentPart5P2)

    var subject = Messages("email.subject.private")
    if (!isPrivate) subject = Messages("email.subject.trade")

    val title = registrationNumber + " " + subject

    Contents(
      buildHtml(title,
        imagesPath,
        registrationNumber,
        transactionId,
        transactionTimestampStr,
        contentPart1LastSentenceHtml,
        linesPlain),
      buildText(
        registrationNumber,
        transactionId,
        transactionTimestampStr,
        contentPart1LastSentence,
        linesPlain, isPrivate)
    )
  }

  private def buildHtml(title: String,
                        imagesPath: String,
                        regNumber: String,
                        transactionId: String,
                        transactionTimestamp: String,
                        line1LastSentence: String,
                        lines: Array[String])(implicit lang: Lang): String =
    s"""<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
       |<html xmlns="http://www.w3.org/1999/xhtml" xmlns="http://www.w3.org/1999/xhtml">
       |<head>
       |    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
       |    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
       |    <title>$title</title>
       |</head>
       |
       |<body style="width: 100% !important; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; margin: 0; padding: 0;">
       |    <style type="text/css">
       |    p {
       |        color: #000000;
       |        font-size: 19px;
       |        line-height: 22px;
       |        font-family: Helvetica, Arial, sans, serif;
       |    }
       |    a {
       |        color: #2e3191;
       |        font-size: 19px;
       |        text-decoration: underline;
       |    }
       |    strong {
       |        font-weight: bold;
       |    }
       |    </style>
       |
       |    <table cellpadding="0" cellspacing="0" border="0" style="width: 100% !important; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt; font-family: Helvetica, Arial, sans, sans-serif; background: #fff; margin: 0; padding: 0;" bgcolor="#fff">
       |        <tr>
       |            <td id="GovUkContainer" style="border-collapse: collapse; color: #fff; background: #000; padding: 0 30px;" bgcolor="#000">
       |                <table style="border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
       |                    <tr>
       |                        <td style="border-collapse: collapse; padding: 20px 0;">
       |                            <a target="_blank" href="https://www.gov.uk/" style="color: #ffffff; text-decoration: none;">
       |                                <img src="$imagesPath/gov-uk.jpg" width="320" height="106" alt="Crown image" style="outline: none; text-decoration: none; -ms-interpolation-mode: bicubic;" />
       |                            </a>
       |                        </td>
       |                    </tr>
       |                </table>
       |            </td>
       |        </tr>
       |
       |        <tr>
       |            <td valign="top" style="border-collapse: collapse; padding: 0 30px;">
       |
       |                <table cellpadding="0" cellspacing="0" border="0" width="100%" style="border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;">
       |
       |                    <tr>
       |                        <td style="border-collapse: collapse;">
       |
       |                            <p><strong style="text-decoration: underline">${Messages("email.template.line1")}</strong></p>
       |
       |                            <p>${lines(0)} ${line1LastSentence}</p>
       |
       |                            <p>${lines(1)}</p>
       |
       |                            <p>${Messages("email.txndetails.p2")} <strong>${regNumber}</strong>
       |                            <br>${Messages("email.txndetails.p3")} <strong>${transactionId}</strong>
       |                            <br>${Messages("email.txndetails.p4")} <strong>${transactionTimestamp}</strong></p>
       |
       |                            <p>${lines(2)}</p>
       |
       |                            <p>${lines(3)}</p>
       |
       |                            <p>${lines(4)}</p>
       |
       |                            <p>${lines(5)}</p>
       |
       |                            <p>${Messages("email.template.line2Html")}</p>
       |
       |                            <p>${Messages("email.template.line3")}</p>
       |
       |                            <p>${Messages("email.signature.p1")}<br>
       |                            ${Messages("email.signature.p2")}<br>
       |                            ${Messages("email.signature.p3")}
       |                            </p>
       |
       |                            <img src="$imagesPath/dvla_logo.png" width="320" alt="DVLA logo" style="outline: none; text-decoration: none; -ms-interpolation-mode: bicubic;" />
       |
       |                        </td>
       |                    </tr>
       |
       |                </table>
       |            </td>
       |        </tr>
       |    </table>
       |    <!-- End of wrapper table -->
       |</body>
       |
       |</html>
       |""".stripMargin

  private def buildText(regNumber: String,
                        transactionId: String,
                        transactionTimestamp: String,
                        line1LastSentence: String,
                        lines: Array[String], isPrivate: Boolean)(implicit lang: Lang): String =
    s"""
        |${Messages("email.template.line1")}
        |
        |${lines(0)} ${line1LastSentence}
        |
        |${lines(1)}
        |
        |${Messages("email.txndetails.p2")}${regNumber}
        |${Messages("email.txndetails.p3")}${transactionId}
        |${Messages("email.txndetails.p4")}${transactionTimestamp}
        |
        |${lines(2)}
        |
        |${lines(3)}
        |${if (isPrivate) s"""
                             |
                             |${lines(4)}
                             |
                             |${lines(5)}
                             |""".stripMargin else ""}
        |${Messages("email.template.line2")}
        |
        |${Messages("email.template.line3")}
        |
        |${Messages("email.signature.p1")}
        |${Messages("email.signature.p2")}
        |${Messages("email.signature.p3")}""".stripMargin
}
