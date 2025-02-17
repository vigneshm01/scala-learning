package calculator

object Polynomial extends PolynomialInterface:
  def computeDelta(a: Signal[Double], b: Signal[Double],
      c: Signal[Double]): Signal[Double] =
    Signal{
      (b() * b()) - 4 * a() * c()
    }

  def computeSolutions(a: Signal[Double], b: Signal[Double],
      c: Signal[Double], delta: Signal[Double]): Signal[Set[Double]] =
    // (-b ± √Δ) / (2a)
    val negB = Signal(-1 * b())
    val twoA = Signal(2 * a())
    val sqrtDelta = Signal(math.sqrt(delta()))

    Signal {
      if (delta() < 0) Set()
      else {
        Set(
          (negB() + sqrtDelta()) / twoA(),
          (negB() - sqrtDelta()) / twoA())
      }
    }
