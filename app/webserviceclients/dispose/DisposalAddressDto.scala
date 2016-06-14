package webserviceclients.dispose

import scala.annotation.tailrec
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.views.models.AddressLinesViewModel.Form.LineMaxLength

case class DisposalAddressDto(line: Seq[String], postTown: Option[String], postCode: String)

object DisposalAddressDto {
  import play.api.libs.json.Json
  implicit val addressDto = Json.writes[DisposalAddressDto]
  final val BuildingNameOrNumberIndex = 0
  final val BuildingNameOrNumberHolder = "No building name/num supplied"
  final val Line2Index = 1
  final val Line3Index = 2
  final val emptyLine = ""

  def from(addressViewModel: AddressModel): DisposalAddressDto = {
    val trimRequired = linesOverMaxLength(addressViewModel.address)
    val addressMandatoryLines =
      if (addressViewModel.address.size == 2)
        AddressModel(
          Seq(BuildingNameOrNumberHolder) ++ addressViewModel.address
        )
      else addressViewModel

    if (trimRequired) rebuildDisposalAddressDto(addressMandatoryLines)
    else buildStandardDisposalAddressDto(addressMandatoryLines)
  }

  @tailrec
  private def linesOverMaxLength(address: Seq[String]): Boolean =
    if (address.isEmpty) false
    else if (address.head.length > LineMaxLength) true
    else linesOverMaxLength(address.tail)

  private def buildStandardDisposalAddressDto(addressViewModel: AddressModel): DisposalAddressDto = {
    val postcode = addressViewModel.address.last.replace(" ","")
    val postTown = Some(addressViewModel.address.takeRight(2).head)
    DisposalAddressDto(addressViewModel.address.dropRight(2), postTown , postcode)
  }

  private def rebuildDisposalAddressDto(addressViewModel: AddressModel): DisposalAddressDto = {
    val address = assignEmptyLines(addressViewModel.address)
    val isLine2Empty = address(Line2Index) == emptyLine
    val isLine3Empty = address(Line3Index) == emptyLine
    val isBuildingNameOrNumberOverMax = address(BuildingNameOrNumberIndex).length > LineMaxLength
    val isLine2OverMax = address(Line2Index).length > LineMaxLength

    val amendedAddressLines = addressLinesDecider(
      isBuildingNameOrNumberOverMax,
      isLine2OverMax,
      isLine2Empty,
      isLine3Empty,
      address
    )
    val legacyAddressLines = trimLines(amendedAddressLines.dropRight(1), Nil)

    val postcode = addressViewModel.address.last.replaceAll(" ","")
    DisposalAddressDto(legacyAddressLines.dropRight(1), Some(legacyAddressLines.last), postcode)
  }

  private def assignEmptyLines(address: Seq[String]) : Seq[String] = {
    address.size match { //every address returned by OS contains at least one address line and a postcode
      case 3 => Seq(address(BuildingNameOrNumberIndex)) ++ Seq(emptyLine) ++ address.tail
      case 4 => Seq(address(BuildingNameOrNumberIndex)) ++ Seq(address(Line2Index)) ++ Seq(emptyLine) ++ address.drop(2)
      case _ => address
    }
  }

  private def addressLinesDecider(isBuildingNameOrNumberOverMax: Boolean,
                                  isLine2OverMax: Boolean,
                                  isLine2Empty: Boolean,
                                  isLine3Empty: Boolean,
                                  address: Seq[String]) : Seq[String]= {
    (isBuildingNameOrNumberOverMax, isLine2OverMax, isLine2Empty, isLine3Empty) match {
      case (true, _, true, _) => Seq(address(BuildingNameOrNumberIndex).substring(0, LineMaxLength)) ++
                                 Seq(address(BuildingNameOrNumberIndex).substring(LineMaxLength)) ++
                                 address.drop(2)
      case (true, _, false, true) => Seq(address(BuildingNameOrNumberIndex).substring(0, LineMaxLength)) ++
                                 Seq(address(BuildingNameOrNumberIndex).substring(LineMaxLength)) ++
                                 Seq(address(Line2Index)) ++
                                 address.drop(3)
      case (false, true, false, true) => Seq(address(BuildingNameOrNumberIndex)) ++
                                         Seq(address(Line2Index).substring(0, LineMaxLength)) ++
                                         Seq(address(Line2Index).substring(LineMaxLength)) ++
                                         address.drop(3)
      case (_) => address
    }
  }
  
  @tailrec
  private def trimLines(address: Seq[String], accumulatedAddress: Seq[String]) : Seq[String] = {
    if (address.isEmpty) accumulatedAddress
    else if (address.head.length > LineMaxLength)
      trimLines(address.tail, accumulatedAddress :+ address.head.substring(0, LineMaxLength))
    else trimLines(address.tail, accumulatedAddress :+ address.head)
  }
}
