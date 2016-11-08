package com.thoughtworks.deepLearning

import com.thoughtworks.deepLearning.Ast._
import com.thoughtworks.deepLearning.Batch._
import com.thoughtworks.deepLearning.hlist._
import com.thoughtworks.deepLearning.boolean._
import com.thoughtworks.deepLearning.seq2D._
import com.thoughtworks.deepLearning.double._
import com.thoughtworks.deepLearning.array2D._
import com.thoughtworks.deepLearning.any._
import com.thoughtworks.deepLearning.any.ast.Identity
import com.thoughtworks.deepLearning.coproduct._
import org.scalatest._

import scala.language.implicitConversions
import scala.language.existentials

/**
  * @author 杨博 (Yang Bo) &lt;pop.atry@gmail.com&gt;
  */
final class VectorizeSpec extends FreeSpec with Matchers {

  import VectorizeSpec._

  "Convert HMatrix to Array2D" in {
    /*
     TODO: 最终目标是生成一个预测神经网络和一个训练神经网络
     为了生成这两个网络，需要生成若干处理Array2D的全连接层、InputData到Array2D的转换、Array2D到Row的转换、Array2D到Double loss的转换

     InputData到Array2D的转换可以从InputData到若干Double的转换做起

     目前可以暂时使用HList而不是直接用case class的神经网络，将来可以直接使用case class

     */

    implicit val learningRate = new LearningRate {
      override def apply() = 0.0003
    }

    def loss(implicit rowAndExpectedLabel: InputAst[Array2D :: ExpectedLabel :: HNil])
      : (Array2D :: ExpectedLabel :: HNil)#ToWidenAst[Double] = {
      type NN[TypePair <: Batch] = WidenAst[(Array2D :: ExpectedLabel :: HNil)#Widen, TypePair#Widen]

      val row = rowAndExpectedLabel.head
      val expectedLabel = rowAndExpectedLabel.tail.head
      val rowSeq = row.toSeq

      // 暂时先在CPU上计算

      val expectedLabelField0 = expectedLabel.head
      val expectedLabelRest1 = expectedLabel.tail
      val expectedLabelField1 = expectedLabelRest1.head
      val expectedLabelRest2 = expectedLabelRest1.tail
      val expectedLabelField2 = expectedLabelRest2.head
      val expectedLabelRest3 = expectedLabelRest2.tail
      val expectedLabelField3 = expectedLabelRest3.head

      val loss0 = expectedLabelField0.choice { _ =>
        0.0 // Drop out
      } {
        _.head.choice { _ =>
          1.0 - log(rowSeq(0, 0))
        } { expectedValue =>
          (log(rowSeq(0, 0)) + abs(log(rowSeq(0, 1)) - log(expectedValue.head)))
        }
      }

      val loss1 = expectedLabelField1.choice { _ =>
        0.0 // Drop out
      } { expectedEnum =>
        val score0 = rowSeq(0, 2)
        val score1 = rowSeq(0, 3)
        val sum = score0 + score1
        val probability0 = score0 / sum
        val probability1 = score1 / sum
        expectedEnum.head.choice { _ =>
          1.0 - probability0
        } { _ =>
          1.0 - probability1
        }
      }

      val loss2 = expectedLabelField2.choice { _ =>
        0.0 // Drop out
      } { expectedDouble =>
        abs(log(expectedDouble.head) - log(rowSeq(0, 4)))
      }

      val loss3 = expectedLabelField3.choice { _ =>
        0.0 // Drop out
      } {  expectedEnum =>
        val score0 = rowSeq(0, 5)
        val score1 = rowSeq(0, 6)
        val score2 = rowSeq(0, 7)
        val sum = score0 + score1 + score2
        val probability0 = score0 / sum
        val probability1 = score1 / sum
        val probability2 = score2 / sum
        expectedEnum.head.choice { _ =>
          1.0 - probability0
        } {
          _.choice { _ =>
            1.0 - probability1
          } { _ =>
            1.0 - probability2
          }
        }
      }

      loss0 + loss1 + loss2 + loss3
    }

    def Array2DToRow(implicit row: InputAst[Array2D]): row.Input#ToWidenAst[PredictionResult] = {
      type NN[TypePair <: Batch] = WidenAst[Array2D#Widen, TypePair#Widen]
      val rowSeq = row.toSeq
      val n: WidenAst[Array2D#Widen, HNil#Widen] = hnil
      val n2: NN[HNil] = hnil
      val field0: NN[Double :: Double :: HNil] = log(rowSeq(0, 0)) :: rowSeq(0, 1) :: n
      val field1: NN[Enum0Prediction] = rowSeq(0, 2) :: rowSeq(0, 3) :: n2
      val field2: NN[Double] = rowSeq(0, 4)
      val field3 = rowSeq(0, 5) :: rowSeq(0, 6) :: rowSeq(0, 7) :: hnil
      field0 :: field1 :: field2 :: field3 :: hnil
    }

    def rowToArray2D(implicit row: InputAst[InputTypePair]): InputTypePair#ToWidenAst[Array2D] = {
      type NN[OutputTypePair <: Batch] = InputTypePair#ToWidenAst[OutputTypePair]
      val field0 = row.head
      val rest0 = row.tail
      val field1 = rest0.head
      val rest1 = rest0.tail
      val field2 = rest1.head
      val rest2 = rest1.tail
      val field3 = rest2.head
      val rest3 = rest2.tail

      val field0Flag0: NN[Double] = field0.choice { _ =>
        1.0
      } { _ =>
        0.0
      }

      val field0Flag1 = field0.choice { unknown =>
        0.5.toWeight
      } {
        _.choice { knownField0 =>
          knownField0.choice { unset =>
            1.0
          } { someValue =>
            0.0
          }
        } { cnil =>
          `throw`(new IllegalArgumentException)
        }
      }

      val field0Value0: NN[Double] = field0.choice { unknown: NN[HNil] =>
        0.5.toWeight: NN[Double]
      } {
        _.choice { knownField0 =>
          knownField0.choice { unset: NN[HNil] =>
            0.5.toWeight: NN[Double]
          } {
            _.choice { nativeDouble: NN[Double] =>
              nativeDouble: NN[Double]
            } { cnil: NN[CNil] =>
              `throw`(new IllegalArgumentException): NN[Double]
            }: NN[Double]
          }: NN[Double]
        } { cnil: NN[CNil] =>
          `throw`(new IllegalArgumentException): NN[Double]
        }: NN[Double]

      }

      val isField1Unknown = field1.isInl
      val field1Enum = field1.tail.head
      val isField1Case0 = field1Enum.isInl
      val isField1Case1 = field1Enum.tail.isInl

      val field1Flag0 = isField1Unknown.`if` {
        1.0
      } {
        0.0
      }

      val field1Value0: NN[Double] = isField1Unknown.`if` {
        0.5.toWeight
      } {
        isField1Case0.`if` {
          1.0
        } {
          0.0
        }
      }

      val field1Value1 = isField1Unknown.`if` {
        0.5.toWeight: NN[Double]
      } {
        isField1Case0.`if` {
          0.0: NN[Double]
        } {
          1.0: NN[Double]
        }: NN[Double]
      }

      val isField2Unknown = field2.isInl
      val field2Flag0 = isField2Unknown.`if` {
        1.0
      } {
        0.0
      }

      val field2Value0 = isField2Unknown.`if` {
        0.5.toWeight
      } {
        field2.tail.head
      }

      val isField3Unknown = field3.isInl
      val field3Enum = field3.tail.head
      val isField3Case0 = field3Enum.isInl
      val isField3Case1 = field3Enum.tail.isInl
      val field3Flag0 = isField3Unknown.`if` {
        1.0
      } {
        0.0
      }

      val field3Value0 = isField3Unknown.`if` {
        0.5.toWeight
      } {
        isField3Case0.`if` {
          1.0
        } {
          0.0
        }
      }

      val field3Value1 = isField3Unknown.`if` {
        0.5.toWeight
      } {
        isField3Case0.`if` {
          0.0
        } {
          1.0
        }
      }

      val field3Value2 = isField3Unknown.`if` {
        0.5.toWeight
      } {
        isField3Case0.`if` {
          0.0
        } {
          isField3Case1.`if` {
            0.0
          } {
            1.0
          }
        }
      }

      val encodedAstRow0 = Vector(field0Flag0,
                                  field0Flag1,
                                  field0Value0,
                                  field1Flag0,
                                  field1Value0,
                                  field1Value1,
                                  field2Flag0,
                                  field2Value0,
                                  field3Flag0,
                                  field3Value0,
                                  field3Value1,
                                  field3Value2)

      Vector(encodedAstRow0).toArray2D
    }

    //    val predict: Ast.WidenBatch[WidenBatch[InputData, _], WidenBatch[Row, _]] = ???
    //
    //    val train: Ast.WidenBatch[WidenBatch[InputData :: ExpectedLabelData :: HNil, _], WidenBatch[Eval[Double], _]] = ???

  }

}

object VectorizeSpec {

  type Nullable[A <: Any] = HNil :+: A :+: CNil

  type InputField[A <: Any] = HNil :+: A :+: CNil

  type LabelField[A <: Any] = HNil :+: A :+: CNil

  type Enum0 = HNil :+: HNil :+: CNil
  type Enum1 = HNil :+: HNil :+: HNil :+: CNil

  type Row = Nullable[Double] :: Enum0 :: Double :: Enum1 :: HNil

  type InputTypePair =
    InputField[Nullable[Double]] :: InputField[Enum0] :: InputField[Double] :: InputField[Enum1] :: HNil

  type ExpectedLabel =
    LabelField[Nullable[Double]] :: LabelField[Enum0] :: LabelField[Double] :: LabelField[Enum1] :: HNil

  type UnsetProbability = Double
  type NullableFieldPrediction[Value <: Any] = UnsetProbability :: Value :: HNil

  type Enum0Prediction = Double :: Double :: HNil
  type Enum1Prediction = Double :: Double :: Double :: HNil

  type PredictionResult = NullableFieldPrediction[Double] :: Enum0Prediction :: Double :: Enum1Prediction :: HNil

}
