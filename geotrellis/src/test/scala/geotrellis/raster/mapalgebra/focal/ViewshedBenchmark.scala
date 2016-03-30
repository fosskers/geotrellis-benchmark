package benchmark.geotrellis.raster.op.focal

import geotrellis._
import geotrellis.raster.viewshed.Viewshed
import geotrellis.vector._
import geotrellis.raster._

import scala.util.Random

/*
 * Current times for 256x256 (ms)
 * [info] CornerRequiredHeight 1363 ==============================
 * [info] CenterRequiredHeight  680 ==============
 */
class  ViewshedBenchmark extends OperationBenchmark {
  var r: Tile = _

  //@Param(Array("512","1024","2048","4096","8192"))
  var size:Int = 256

  override def setUp() {
    r = {
      val a = Array.ofDim[Int](size * size).map(a => Random.nextInt(255))
      IntArrayTile(a, size, size)
    }
  }

  def timeCornerRequiredHeight(reps:Int) = run(reps)(cornerRequiredHeight)
  def cornerRequiredHeight() {
    Viewshed(r, 0, 0)
  }

  def timeCenterRequiredHeight(reps:Int) = run(reps)(centerRequiredHeight)
  def centerRequiredHeight() {
    Viewshed(r, size/2, size/2)
  }
}
