import java.io.File
import com.sksamuel.scrimage._

class Tiler {
  val CHATTY_DEFAULT = false
  val BACKGROUND_DEFAULT = Color.White.toInt
  val EFFICIENT_DEFAULT = true
  val SCALING_FILTER = ScaleMethod.Bicubic
  val PNG_PATTERN = ".png$".r
  val GIF_PATTERN = ".gif$".r
  val JPG_PATTERN = ".jpg$|.jpeg$".r

  var newSize = (1, 1)
  var origSize = (1, 1)
  var maxZoom = 0
  var tileSize = (1, 1)

  def findLeastPow2GreaterThan(x: Int): Int = Math.pow(2, Math.ceil(Math.log10(x) / Math.log10(2))).toInt

  def prepare(fileName: String, bgColor: Int = BACKGROUND_DEFAULT, chatty: Boolean = CHATTY_DEFAULT): Image = {
    val file = new File(fileName)
    val src = Image(file)
    origSize = (src.width, src.height)

    val size = Math.max(findLeastPow2GreaterThan(origSize._1), findLeastPow2GreaterThan(origSize._2))
    newSize = (size, size)

    val preppedImg = src.copy.padTo(newSize._1, newSize._2, RGBColor(0, 0, 255, 255));

    return preppedImg
  }

  def tile(image: Image, zoomLevel: Int, quadrant: (Int, Int) = (0, 0), size: (Int, Int) = (512, 512), efficient: Boolean = EFFICIENT_DEFAULT, chatty: Boolean = CHATTY_DEFAULT): Image = {
    var zoomedImage = image.copy
    val imageSize = (image.width, image.height)
    val scale = Math.pow(2, zoomLevel).toInt
    if (chatty) println((image.width, image.height), zoomLevel, quadrant, size)
    if (chatty) println(scale)
    if (efficient) {
      // efficient: crop out the area of interest first, then scale and copy it

      val inverseSize = (imageSize._1.toFloat / (size._1 * scale).toFloat, imageSize._2.toFloat / (size._2 * scale).toFloat)
      if (chatty) println(inverseSize)
      val topLeft = ((quadrant._1 * size._1 * inverseSize._1).toInt, (quadrant._2 * size._2 * inverseSize._2).toInt)
      val bottomRight = ((topLeft._1 + (size._1 * inverseSize._1)).toInt, (topLeft._2 + (size._2 * inverseSize._2)).toInt)

      if (inverseSize._1 < 1.0 || inverseSize._2 < 1.0)
        throw new Exception(s"Requested zoom level ($zoomLevel) is too high")

      if (chatty)
        println("crop(%s,%s).resize(%s)".format(topLeft.toString, bottomRight.toString, size.toString))

      zoomedImage = image.trim(topLeft._1, topLeft._2, imageSize._1 - bottomRight._1, imageSize._2 - bottomRight._2).scaleTo(size._1, size._2, SCALING_FILTER)
    }

    return zoomedImage
  }
  
  def getWriter(image: Image, filename: String, quality: Int): io.ImageWriter = {
    if ((PNG_PATTERN findFirstIn filename) != None) {
      // 0 -> no compressions, 9 -> max compression
      return image.writer(Format.PNG).withCompression(9 - (9 * (quality / 100.0)).round.toInt)
    }
    if ((JPG_PATTERN findFirstIn filename) != None) {
      // 0 -> low quality, 100 -> high quality
      return image.writer(Format.JPEG).withCompression(quality)
    }
    if ((GIF_PATTERN findFirstIn filename) != None) {
      return image.writer(Format.GIF)
    }
    return null
  }

  def subdivide(image: Image, level: Int = 0, quadrant: (Int, Int) = (0, 0), size: (Int, Int) = (512, 512), filename: String = "tile-%d-%d-%d.jpg", quality: Int = -1, chatty: Boolean = CHATTY_DEFAULT): Image = {
    var outputImage = image.copy
    
    require(quality == -1 || (quality >= 0 && quality <= 100))
    
    if (image.width <= size._1 * Math.pow(2, level)) {
      outputImage = this.tile(image, level, quadrant, size)
      
      outputImage.write(filename.format(level, quadrant._1, quadrant._2));
      
      if (level > this.maxZoom) this.maxZoom = level
      
      this.tileSize = size
      
      if (chatty) println(". " + ("  " * level) + " " + filename.format(level, quadrant._1, quadrant._2))
      
      return outputImage
    }
    
    outputImage = Image.empty(size._1 * 2, size._2 * 2)
    val quadShifts = Array((0, 0), (0, 1), (1, 0), (1, 1))
    quadShifts.foreach(shift => {
      val subImage = this.subdivide(image, level + 1, (quadrant._1 * 2 + shift._1, quadrant._2 * 2 + shift._2), size, filename, quality, chatty)
      outputImage = outputImage.overlay(subImage, shift._1 * size._1, shift._2 * size._2)
    })

    outputImage = outputImage.scaleTo(size._1, size._2, SCALING_FILTER)
    
    if (quality < 0) {
      outputImage.write(filename.format(level, quadrant._1, quadrant._2))
    } else {
      this.getWriter(outputImage, filename, quality).write(filename.format(level, quadrant._1, quadrant._2))
    }
    
    if (chatty) println("- " + ("  " * level) + " " + filename.format(level, quadrant._1, quadrant._2))
    
    return outputImage
  }
}
