package views.disposal_of_vehicle

import org.mockito.Mockito.{times, when}
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.mock.MockitoSugar
import org.slf4j.Logger
import play.api.LoggerLike

class MockLogger extends LoggerLike with MockitoSugar {
  override val logger = mock[Logger]

  when(logger.isTraceEnabled).thenReturn(true)
  when(logger.isDebugEnabled).thenReturn(true)
  when(logger.isInfoEnabled).thenReturn(true)
  when(logger.isWarnEnabled).thenReturn(true)
  when(logger.isErrorEnabled).thenReturn(true)

  def captureLogInfo(): String = {
    val captor = ArgumentCaptor.forClass(classOf[String])
    Mockito.verify(logger).info(captor.capture())
    captor.getValue
  }

  def captureLogInfos(times1: Int): java.util.List[String] = {
    val captor = ArgumentCaptor.forClass(classOf[String])
    Mockito.verify(logger, times(times1)).info(captor.capture())
    captor.getAllValues
  }

  def reset(): Unit = Mockito.reset(logger)
}