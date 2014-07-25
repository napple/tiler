object Main extends App {
  val tiler = new Tiler()
  //tiler.prepare("/Users/napple/Downloads/not-amused.png", 0x0000ff)
  val prepped = tiler.prepare("/Users/napple/Downloads/finn+shep.jpg")
  tiler.subdivide(prepped, 0, (0, 0), (512, 512))
}
