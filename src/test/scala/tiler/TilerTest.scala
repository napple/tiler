import org.scalatest.FlatSpec

class FirstSpec extends FlatSpec {
  def fixture = new {
	val tiler = new Tiler()
    val preppedImage = tiler.prepare("/Users/napple/Downloads/finn+shep.jpg")
  }

  "Prepped image" should " be square" in {
    val f = fixture
	assert(f.preppedImage.width === f.preppedImage.height)
  }

}
