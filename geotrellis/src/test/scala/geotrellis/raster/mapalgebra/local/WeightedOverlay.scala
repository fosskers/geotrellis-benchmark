package benchmark.geotrellis.raster.local

import geotrellis.engine._
import geotrellis.engine.op.local._
import geotrellis.engine.op.global._
import geotrellis.engine.render._
import geotrellis.raster._
import geotrellis.raster.mapalgebra._
import geotrellis.raster.mapalgebra.local._
import geotrellis.raster.summary._
import geotrellis.raster.render._

import benchmark.geotrellis.util._

import scala.util.Random
import scaliper._

trait WeightedOverlaySetup {
  val cellType: String
  val size: Int
  val layerCount: Int

  val layers =
    Map(
      ("bit","wm_DevelopedLand"),
      ("byte", "SBN_car_share"),
      ("short","travelshed-int16"),
      ("int","travelshed-int32"),
      ("float","aspect"),
      ("double","aspect-double")
    )


  val colors = Array(0x0000FF, 0x0080FF, 0x00FF80, 0xFFFF00, 0xFF8000, 0xFF0000)

  var source:ValueSource[Png] = null
  var sourceSeq:ValueSource[Png] = null

  override def setUp() {
    val weights = (0 until layerCount).map(i => Random.nextInt).toArray
    val re = getRasterExtent(layers(cellType), size, size)
    val total = weights.sum
    source =
      (0 until layerCount).map(i => RasterSource(layers(cellType),re) * weights(i))
                          .reduce(_+_)
                          .localDivide(total)
                          .renderPng(colors)

    sourceSeq =
      (0 until layerCount).map(i => RasterSource(layers(cellType),re) * weights(i))
                          .localAdd
                          .localDivide(total)
                          .renderPng(colors)
  }
}

class WeightedOverlayBenchmarks extends Benchmarks {
  for (ct <- Array("bit", "byte", "short", "int", "float", "double")) {
    benchmark("Weighted Overlay - ${ct} cells") {
      for (s <- Array(256, 512, 1024, 2048, 4096)) {
        for (lc <- Array(4, 8, 16)) {
          run("from folding addition - size: ${size}; layers: ${lc}") {
            new Benchmark with WeightedOverlaySetup {
              val cellType = ct
              val size = s
              val layerCount = lc

              def run = get(source)
            }
          }
          run("from localAdd on Seq - size: ${size}; layers: ${lc}") {
            new Benchmark with WeightedOverlaySetup {
              val cellType = ct
              val size = s
              val layerCount = lc

              def run = get(sourceSeq)
            }
          }
        }
      }
    }
  }
}

