val x = Range(1, 1000)


x.toList.fold(0){ (acc, item) =>
  item match {
    case item if (item%3 == 0 || item%5 == 0) => acc + item
    case _ => acc
  }
}