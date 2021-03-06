// Copyright 2014,2015 Jun Tsai. All rights reserved.
// site: http://www.ganshane.com
package nirvana.hall.image.jni

import java.io.File

import monad.support.services.LoggerSupport
import nirvana.hall.image.HallImageConstants
import org.fusesource.hawtjni.runtime

/**
 * JNI Loader
 */
object JniLoader extends LoggerSupport {
  private final val IMAGE_LIBRARY = new runtime.Library("hall-image4j", getClass)

  def loadJniLibrary(serverHome: String, logFile: String) {
    val file = new File(serverHome)
    val path = System.getProperty(HallImageConstants.HALL_IMAGE_PATH_KEY)
    if (path == null)
      System.setProperty(HallImageConstants.HALL_IMAGE_PATH_KEY, file.getAbsolutePath + "/dll")
    IMAGE_LIBRARY.load()
  }
}
