package test.helpers

import org.scalacheck.Shrink
import org.scalatest.prop.GeneratorDrivenPropertyChecks

trait PropertyTesting extends GeneratorDrivenPropertyChecks {
  implicit def forAllNoShrink[T]: Shrink[T] = Shrink(_ => Stream.empty[T])
}
