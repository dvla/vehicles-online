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

  def buildWith(vehicleDetailsOpt: Option[VehicleAndKeeperDetailsModel],  transactionId: String,
                transactionTimestamp: DateTime = new DateTime): Contents = {

    val transactionTimestampStr = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(transactionTimestamp.toDate)

    val registrationNumber = vehicleDetailsOpt.map(_.registrationNumber).getOrElse("No registration number")

    Contents(
      buildHtml(registrationNumber, transactionId, transactionTimestampStr),
      buildText(registrationNumber, transactionId, transactionTimestampStr)
    )
  }

  private def buildHtml(registrationNumber: String,  transactionId: String,
                        transactionTimestamp: String): String =
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
       |        <!--<tr>
       |            <td id="GovUkContainer" style="border-collapse: collapse; color: #fff; background: #000; padding: 0 30px;" bgcolor="#000">
       |                <table style="border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
       |                    <tr>
       |                        <td style="border-collapse: collapse; padding: 20px 0;">
       |                            <a target="_blank" href="https://www.gov.uk/" style="color: #ffffff; text-decoration: none;">
       |                                <img src="Gov Image" width="320" height="106" alt="Crown image" style="outline: none; text-decoration: none; -ms-interpolation-mode: bicubic;" />
       |                            </a>
       |                        </td>
       |                    </tr>
       |                </table>
       |            </td>
       |        </tr>-->
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
       |                            <p>Thank you for using DVLA’s online service to confirm you are no longer the registered keeper of this vehicle.</p>
       |
       |                            <p>Your application details are:</p>
       |
       |                            <p>
       |                                Vehicle Registration Number: <strong>$registrationNumber</strong> <br />
       |                                Transaction ID: <strong>$transactionId</strong> <br />
       |                                Application Made On: <strong>$transactionTimestamp</strong>
       |
       |                            </p>
       |
       |                            <p>You should receive a postal acknowledgement letter within 4 weeks.</p>
       |
       |                            <p>DVLA will automatically issue a refund for any full remaining months for vehicle tax and cancel any direct debits. The refund will be sent to the address on the V5C log book, which was used.</p>
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
       |                            <!--<img src="DVLA logo" width="320" alt="DVLA logo" style="outline: none; text-decoration: none; -ms-interpolation-mode: bicubic;" />-->
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
                        transactionTimestamp: String): String =

    s"""
        |THIS IS AN AUTOMATED EMAIL - Please do not reply as emails received at this address cannot be responded to.
        |
        |
        |Thank you for using DVLA’s online service to confirm you are no longer the registered keeper of this vehicle.
        |
        |Your application details are:
        |Vehicle Registration Number: $registrationNumber
        |Transaction ID: $transactionId
        |Application Made On: $transactionTimestamp
        |
        |You should receive a postal acknowledgement letter within 4 weeks.
        |
        |DVLA will automatically issue a refund for any full remaining months for vehicle tax and cancel any direct debits. The refund will be sent to the address on the V5C log book, which was used.
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
