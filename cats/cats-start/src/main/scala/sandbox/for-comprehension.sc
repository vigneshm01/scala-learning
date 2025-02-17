for {
  p <- persons // a generator
  n = p.name // a definition
  if (n startsWith "To") // a filter
} yield n
