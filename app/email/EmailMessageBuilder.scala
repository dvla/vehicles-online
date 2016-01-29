package email

import java.text.SimpleDateFormat
import org.joda.time.DateTime
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel

/**
 * The email message builder class will create the contents of the message. override the buildHtml and buildText
 * with new html and text templates respectively.
 *
 */
object EmailMessageBuilder {
  import uk.gov.dvla.vehicles.presentation.common.services.SEND.Contents

  final val contentPart1Private = s"Thank you for using DVLA’s online service to confirm you are no longer the registered keeper of this vehicle. Please destroy the original V5C/3 (yellow slip). This must not be sent to DVLA."
  final val contentPart1TradeApp = s"DVLA have been notified electronically that you have sold/transferred this vehicle into the motor trade and are no longer the keeper."
  final val contentPart1TradeAppToTrade = s"Thank you for using DVLA’s online service to confirm you have taken this vehicle into the motor trade. Please destroy the original V5C/3 (yellow slip). This must not be sent to DVLA."
  final val contentPart1PrivateHtml = s"<p>Thank you for using DVLA’s online service to confirm you are no longer the registered keeper of this vehicle. Please destroy the original V5C/3 (yellow slip). This must <strong>not</strong> be sent to DVLA.</p>"
  final val contentPart1TradeAppHtml = s"<p>DVLA have been notified electronically that you have sold/transferred this vehicle into the motor trade and are no longer the keeper.</p>"
  final val contentPart1TradeAppToTradeHtml = s"<p>Thank you for using DVLA’s online service to confirm you have taken this vehicle into the motor trade. Please destroy the original V5C/3 (yellow slip). This must <strong>not</strong> be sent to DVLA.</p>"
  final val contentPart2Private = "The"
  final val contentPart2TradeApp = s"The acknowledgement letter and"
  final val contentPart3Private = s"Your"
  final val contentPart3TradeApp = s"The"

  final val contentPart4Private = s"You"
  final val contentPart4TradeToTrade = s"The registered keeper (seller)"
  final val contentPart5PrivatePlain = s"""You may still receive a V11 tax reminder as these are pre-printed up to 6 weeks
                          | in advance. If you do receive a V11 for this vehicle after notifying the sale, please ignore it.
                          |
                          |If another payment is taken before your Direct Debit is cancelled, you’ll be automatically refunded within 10 days."""
  final val contentPart5PrivateHtml = s"""<p>You may still receive a V11 tax reminder as these are pre-printed up to 6 weeks
                          | in advance. If you do receive a V11 for this vehicle after notifying the sale, please ignore it.</p>
                          |
                          |<p>If another payment is taken before your Direct Debit is cancelled, you’ll be automatically refunded within 10 days.</p>"""

  def buildWith(vehicleDetailsOpt: Option[VehicleAndKeeperDetailsModel],  transactionId: String,
                imagesPath: String, transactionTimestamp: DateTime, isPrivate: Boolean = true, toTrader: Boolean = false): Contents = {

    val transactionTimestampStr = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(transactionTimestamp.toDate)

    val registrationNumber = vehicleDetailsOpt.map(_.registrationNumber).getOrElse("No registration number")

    // private confirmation email by default
    var contentPart1 = contentPart1Private
    var contentPart1Html = contentPart1PrivateHtml
    var contentPart2 = contentPart2Private
    var contentPart3 = contentPart3Private
    var contentPart4 = contentPart4Private
    var contentPart5Plain = contentPart5PrivatePlain
    var contentPart5Html = contentPart5PrivateHtml

    // amend to suit trade application email(s)
    if (!isPrivate) {
      if (!toTrader) {
        contentPart1 = contentPart1TradeApp
        contentPart1Html = contentPart1TradeAppHtml
      } else {
        contentPart1 = contentPart1TradeAppToTrade
        contentPart1Html = contentPart1TradeAppToTradeHtml
        contentPart4 = contentPart4TradeToTrade
      }
      contentPart2 = contentPart2TradeApp
      contentPart3 = contentPart3TradeApp
      contentPart5Plain = ""
      contentPart5Html = ""
    }

    Contents(
      buildHtml(registrationNumber, transactionId, imagesPath, transactionTimestampStr, contentPart1Html, contentPart2, contentPart3, contentPart4, contentPart5Html),
      buildText(registrationNumber, transactionId, transactionTimestampStr, contentPart1, contentPart2, contentPart3, contentPart4, contentPart5Plain)
    )
  }

  private def buildHtml(registrationNumber: String,  transactionId: String, imagesPath: String,
                        transactionTimestamp: String, contentPart1: String, contentPart2: String, contentPart3: String,
                         contentPart4: String, contentPart5: String): String =
    s"""<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
       |<html xmlns="http://www.w3.org/1999/xhtml" xmlns="http://www.w3.org/1999/xhtml">
       |<head>
       |    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
       |    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
       |    <title>$registrationNumber Confirmation of new vehicle keeper</title>
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
       |                            <p><strong style="text-decoration: underline">This is an automated email - Please do not reply as emails received at this address cannot be responded to.</strong></p>
       |
       |                            $contentPart1
       |
       |                            <p>$contentPart3 application details are:</p>
       |
       |                            <p>
       |                                Vehicle Registration Number: <strong>$registrationNumber</strong> <br />
       |                                Transaction ID: <strong>$transactionId</strong> <br />
       |                                Application Made On: <strong>$transactionTimestamp</strong>
       |
       |                            </p>
       |
       |                            <p>$contentPart4 should receive a postal acknowledgement letter within 4 weeks.</p>
       |
       |                            <p>DVLA will automatically issue a refund for any full remaining months for vehicle tax and cancel any direct debits. $contentPart2 refund will be sent to the address on the V5C log book, which was used.</p>
       |
       |                            $contentPart5
       |
       |                            <p>For more information on driving and transport go to <a href="http://www.gov.uk/browse/driving" target="_blank">www.gov.uk/browse/driving</a>.</p>
       |
       |                            <p>You may wish to save or print this email confirmation for your records.</p>
       |
       |                            <p>Yours sincerely <br />
       |                            Rohan Gye<br />
       |                            Vehicles Service Manager
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
       |
      """.stripMargin

  private def buildText(registrationNumber: String,transactionId: String,
                        transactionTimestamp: String, contentPart1: String, contentPart2: String, contentPart3: String,
                         contentPart4: String, contentPart5: String): String =

    s"""
        |THIS IS AN AUTOMATED EMAIL - Please do not reply as emails received at this address cannot be responded to.
        |
        |$contentPart1
        |
        |$contentPart3 application details are:
        |Vehicle Registration Number: $registrationNumber
        |Transaction ID: $transactionId
        |Application Made On: $transactionTimestamp
        |
        |$contentPart4 should receive a postal acknowledgement letter within 4 weeks.
        |
        |DVLA will automatically issue a refund for any full remaining months for vehicle tax and cancel any direct debits. $contentPart2 refund will be sent to the address on the V5C log book, which was used.
        |
        |$contentPart5
        |
        |For more information on driving and transport go to http://www.gov.uk/browse/driving
        |
        |You may wish to save or print this email confirmation for your records.
        |
        | Yours sincerely
        | Rohan Gye
        | Vehicles Service Manager
                                                                                                                                                                                                     |       |                            </p>
        |
        |Thank You
      """.stripMargin
}
