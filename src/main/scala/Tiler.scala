import java.io.File
import com.sksamuel.scrimage._

class Tiler {
  val CHATTY_DEFAULT = false
  val BACKGROUND_DEFAULT = Color.White.toInt
  val EFFICIENT_DEFAULT = true
  val SCALING_FILTER = ScaleMethod.Bicubic

  var newSize = (1, 1)
  var origSize = (1, 1)
  var maxZoom = 0
  var tileSize = 256
  
  def findLeastPow2GreaterThan(x:Int): Int = Math.pow(2, Math.ceil(Math.log10(x) / Math.log10(2))).toInt

  def prepare(fileName: String, bgColor: Int = BACKGROUND_DEFAULT, chatty: Boolean = CHATTY_DEFAULT): Image = {
    val file = new File(fileName)
    val src = Image(file)
    origSize = (src.width, src.height)
    
    val size = Math.max(findLeastPow2GreaterThan(origSize._1), findLeastPow2GreaterThan(origSize._2))
    newSize = (size, size)

    val preppedImg = src.copy.padTo(newSize._1, newSize._2, RGBColor(0, 0, 255, 255));
    // Testing... remove following two lines
    //val outFile = new File("newImage.jpg")
    //preppedImg.write(outFile, Format.JPEG)
    
    return preppedImg
  }

  def tile(image: Image, zoomLevel: Int, quadrant: (Int,Int) = (0, 0), size: (Int, Int) = (512, 512), efficient: Boolean = EFFICIENT_DEFAULT, chatty: Boolean = CHATTY_DEFAULT): Image = {
    var zoomedImage = image.copy;
    val imageSize = (image.width, image.height)
    val scale = Math.pow(2, zoomLevel).toInt
    if (efficient) {
      // efficient: crop out the area of interest first, then scale and copy it

      val inverseSize = (imageSize._1.toFloat / size._1 * scale, imageSize._2.toFloat / size._2 * scale)
      val topLeft = ((quadrant._1 * size._1 * inverseSize._1).toInt, (quadrant._2 *  size._2 * inverseSize._2).toInt)
      val bottomRight = ((topLeft._1 + (size._1 * inverseSize._1)).toInt, (topLeft._2 + (size._2 * inverseSize._2)).toInt)

      if (inverseSize._1 < 1.0 || inverseSize._2 < 1.0)
        throw new Exception(s"Requested zoom level ($zoomLevel) is too high")

      if (chatty)
        println("crop(%s,%s).resize(%s)".format(topLeft.toString, bottomRight.toString, size.toString))

      //zoomedImage = image.crop(top_left + bottom_right).resize(size, scaling_filter).copy()
      //zoomedImage = image.trim(topLeft._1, topLeft._2, imageSize._1 - bottomRight._1, imageSize._2 - bottomRight._2).
    }
    
    return zoomedImage
  }
}