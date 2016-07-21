package views.disposal_of_vehicle

object Dispose {

  def modify(htmlArgs: Map[Symbol, Any], modify: Boolean): Map[Symbol, Any] =
    if (modify)
      htmlArgs + (Symbol("tabindex") -> -1)
    else
      htmlArgs
}
