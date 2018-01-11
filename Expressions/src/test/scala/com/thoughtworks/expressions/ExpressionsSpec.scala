package com.thoughtworks.expressions

import com.thoughtworks.expressions.Builtins.{AllDifferentiableExpressions, AllOpenCLExpressions}
import org.scalatest._
import com.thoughtworks.feature.Factory
import OpenCLExpressions.generateOpenCLKernelSourceCode

/**
  * @author 杨博 (Yang Bo)
  */
class ExpressionsSpec extends FreeSpec with Matchers {

  "fill" in {

    val hyperparameters: AllOpenCLExpressions { type DebuggingInformation = Debugging.Name } = {
      Factory[AllOpenCLExpressions].newInstance()
    }

    import hyperparameters._

    val x: float.Identifier = float.Identifier()
    val sourceCode = generateOpenCLKernelSourceCode("fill", 3, Seq(x), Seq(float.Literal(42.0f))).mkString
    println(sourceCode) // FIXME: replace println to a scalatest assertion

  }

  "id" in {

    val hyperparameters: AllOpenCLExpressions { type DebuggingInformation = Debugging.Name } =
      Factory[AllOpenCLExpressions].newInstance()

    import hyperparameters._

    val dimentions = Seq(64, 32, 32)
    val floatArray3d = ArrayBufferType.newInstance(float, dimentions)
    val x: floatArray3d.Identifier = floatArray3d.Identifier()

    val sourceCode = generateOpenCLKernelSourceCode("id", dimentions.length, Seq(x), Seq(x.extract)).mkString

    println(sourceCode) // FIXME: replace println to a scalatest assertion

  }

  "differentiable id" in {

    val hyperparameters =
      Factory[AllOpenCLExpressions with AllDifferentiableExpressions].newInstance()

    import hyperparameters._

    val dimensions = Seq(64, 32, 32)
    val floatArray3d = ArrayBufferType.newInstance(float, dimensions)
    val x: floatArray3d.Identifier = floatArray3d.Identifier()

    val deltaX: floatArray3d.Identifier = floatArray3d.Identifier()

    //    x.extract.

    val f = x.extract

    val sourceCode =
      generateOpenCLKernelSourceCode("id_backward", dimensions.length, Seq(x, deltaX), Seq(delta(f, x -> deltaX))).mkString

    println(sourceCode) // FIXME: replace println to a scalatest assertion

  }

  "3x3 convolutional" in {

    val hyperparameters =
      Factory[AllOpenCLExpressions with AllDifferentiableExpressions].newInstance()

    import hyperparameters._

    val batchSize = 64
    val width = 32
    val height = 32
    val depth = 128
    // TODO: depth
    val dimensions = Seq(batchSize, width, height)
    import shapeless.syntax.singleton._
    val floatArray3d = ArrayBufferType[float.type].newInstance(float, dimensions)
    val x: floatArray3d.Identifier = floatArray3d.Identifier()
    val w: FloatTerm = float.Identifier()
    val b: FloatTerm = float.Identifier()

    val f = x.extract * w + b

    val forwardSourceCode = generateOpenCLKernelSourceCode("cnn_forward", dimensions.length, Seq(x, w, b), Seq(f)).mkString
    println(forwardSourceCode)

    val deltaX = floatArray3d.Identifier()
    val backwardSourceCode = generateOpenCLKernelSourceCode("cnn_backward",
                                                            dimensions.length,
                                                            Seq(x, w, b, deltaX),
                                                            Seq(delta(f, x -> deltaX))).mkString

    println(backwardSourceCode)

  }


}
